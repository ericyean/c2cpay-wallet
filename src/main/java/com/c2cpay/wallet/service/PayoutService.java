package com.c2cpay.wallet.service;

import com.c2cpay.wallet.coin.CoinDispatcher;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.config.WalletProperties;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.domain.AssetType;
import com.c2cpay.wallet.domain.PayoutOrderEntity;
import com.c2cpay.wallet.domain.PayoutStatus;
import com.c2cpay.wallet.domain.RiskLevel;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import com.c2cpay.wallet.dto.business.CreatePayoutRequest;
import com.c2cpay.wallet.dto.business.PayoutOrderResponse;
import com.c2cpay.wallet.repository.PayoutOrderRepository;
import com.c2cpay.wallet.risk.RiskCheckResult;
import com.c2cpay.wallet.risk.RiskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

/**
 * Hot-wallet payout: merchant payout / member withdraw / merchant withdraw USDT.
 * Review → risk check → sign. Broadcast is external.
 */
@Service
public class PayoutService {

    private final WalletProperties properties;
    private final CoinDispatcher dispatcher;
    private final PayoutOrderRepository payoutOrderRepository;
    private final RiskService riskService;

    public PayoutService(
            WalletProperties properties,
            CoinDispatcher dispatcher,
            PayoutOrderRepository payoutOrderRepository,
            RiskService riskService
    ) {
        this.properties = properties;
        this.dispatcher = dispatcher;
        this.payoutOrderRepository = payoutOrderRepository;
        this.riskService = riskService;
    }

    @Transactional
    public PayoutOrderResponse create(CreatePayoutRequest request) {
        AssetType asset = AssetType.fromCode(request.asset())
                .orElseThrow(() -> new IllegalArgumentException("Unknown asset: " + request.asset()));

        return payoutOrderRepository.findByBizOrderId(request.bizOrderId())
                .map(PayoutOrderResponse::from)
                .orElseGet(() -> {
                    if (!dispatcher.validateAddress(asset.getCoin(), request.toAddress())) {
                        throw new IllegalArgumentException("Invalid toAddress for " + asset);
                    }
                    RiskCheckResult risk = riskService.check(asset.getCoin().getId(), request.toAddress(), null);
                    PayoutOrderEntity order = new PayoutOrderEntity();
                    order.setBizOrderId(request.bizOrderId());
                    order.setAsset(asset);
                    order.setToAddress(request.toAddress());
                    order.setAmount(request.amount());
                    order.setRiskLevel(risk.level());
                    if (!risk.allowPayout() || risk.level() == RiskLevel.HIGH) {
                        order.setStatus(PayoutStatus.REJECTED);
                        order.setFailReason("Risk rejected: " + risk.reason());
                    } else {
                        order.setStatus(PayoutStatus.PENDING_REVIEW);
                    }
                    payoutOrderRepository.save(order);
                    return PayoutOrderResponse.from(order, risk);
                });
    }

    @Transactional
    public PayoutOrderResponse approveAndSign(String bizOrderId) {
        PayoutOrderEntity order = payoutOrderRepository.findByBizOrderId(bizOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found: " + bizOrderId));
        if (order.getStatus() != PayoutStatus.PENDING_REVIEW
                && order.getStatus() != PayoutStatus.APPROVED) {
            throw new IllegalStateException("Payout not approvable: " + order.getStatus());
        }
        if (!properties.getHot().isConfigured()) {
            throw new IllegalStateException("Hot wallet private key not configured");
        }

        order.setStatus(PayoutStatus.SIGNING);
        payoutOrderRepository.save(order);

        try {
            SignResponse signed = signFromHot(order);
            order.setRawTxHex(signed.encoded());
            order.setTxId(signed.txId());
            order.setStatus(PayoutStatus.BROADCASTED);
        } catch (Exception e) {
            order.setStatus(PayoutStatus.FAILED);
            order.setFailReason(e.getMessage());
        }
        payoutOrderRepository.save(order);
        return PayoutOrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PayoutOrderResponse get(String bizOrderId) {
        return payoutOrderRepository.findByBizOrderId(bizOrderId)
                .map(PayoutOrderResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found: " + bizOrderId));
    }

    private SignResponse signFromHot(PayoutOrderEntity order) {
        CoinType coin = order.getAsset().getCoin();
        String privateKeyHex = Hex.normalize(properties.getHot().getPrivateKeyHex());

        return switch (coin) {
            case TRON -> {
                long sun = order.getAmount().movePointRight(6).setScale(0, RoundingMode.DOWN).longValueExact();
                SignRequest req = new SignRequest(
                        coin.getId(), privateKeyHex, null, null,
                        null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null,
                        order.getToAddress(), sun, null, null, null, null
                );
                yield dispatcher.sign(coin, req);
            }
            case ETHEREUM, BINANCE_SMART_CHAIN -> {
                long chainId = coin == CoinType.ETHEREUM ? 1L : 56L;
                String wei = order.getAmount().movePointRight(18).setScale(0, RoundingMode.DOWN).toPlainString();
                SignRequest req = new SignRequest(
                        coin.getId(), privateKeyHex, null, null,
                        order.getToAddress(), wei, null, 0L, "20000000000", "21000", chainId, null, null,
                        null, null, null, null, null,
                        null, null, null, null, null, null
                );
                yield dispatcher.sign(coin, req);
            }
            default -> throw new UnsupportedOperationException("Payout signing not implemented for " + coin);
        };
    }
}
