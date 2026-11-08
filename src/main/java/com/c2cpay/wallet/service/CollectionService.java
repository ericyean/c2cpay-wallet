package com.c2cpay.wallet.service;

import com.c2cpay.wallet.chain.tron.TronAddress;
import com.c2cpay.wallet.coin.CoinDispatcher;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.config.WalletProperties;
import com.c2cpay.wallet.crypto.AesGcmEncryptor;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.PrivateKey;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.domain.CollectionTaskEntity;
import com.c2cpay.wallet.domain.DepositAddressEntity;
import com.c2cpay.wallet.domain.DepositAddressStatus;
import com.c2cpay.wallet.domain.RiskLevel;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import com.c2cpay.wallet.dto.business.CollectionTaskResponse;
import com.c2cpay.wallet.dto.business.CreateCollectionRequest;
import com.c2cpay.wallet.repository.CollectionTaskRepository;
import com.c2cpay.wallet.repository.DepositAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Sweep deposit child addresses into hot (or isolation) wallet.
 * Signs locally; broadcast is left to the caller / chain gateway.
 */
@Service
public class CollectionService {

    private final WalletProperties properties;
    private final CoinDispatcher dispatcher;
    private final AesGcmEncryptor encryptor;
    private final DepositAddressRepository depositAddressRepository;
    private final CollectionTaskRepository collectionTaskRepository;

    public CollectionService(
            WalletProperties properties,
            CoinDispatcher dispatcher,
            AesGcmEncryptor encryptor,
            DepositAddressRepository depositAddressRepository,
            CollectionTaskRepository collectionTaskRepository
    ) {
        this.properties = properties;
        this.dispatcher = dispatcher;
        this.encryptor = encryptor;
        this.depositAddressRepository = depositAddressRepository;
        this.collectionTaskRepository = collectionTaskRepository;
    }

    /** 查询待归集任务列表 */
    @Transactional(readOnly = true)
    public List<CollectionTaskResponse> listPending() {
        return collectionTaskRepository.findByStatus(CollectionTaskEntity.Status.PENDING).stream()
                .map(CollectionTaskResponse::from)
                .toList();
    }

    /** 创建归集任务并签名 */
    @Transactional
    public CollectionTaskResponse createAndSign(CreateCollectionRequest request) {
        DepositAddressEntity deposit = depositAddressRepository.findById(request.depositAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Deposit address not found: " + request.depositAddressId()));

        if (deposit.getStatus() == DepositAddressStatus.ISOLATED
                || deposit.getRiskLevel() == RiskLevel.HIGH) {
            CollectionTaskEntity skipped = newTask(deposit, request.amount(), isolationOrHot(true));
            skipped.setStatus(CollectionTaskEntity.Status.SKIPPED_RISK);
            skipped.setFailReason("High risk — route manually to isolation wallet");
            collectionTaskRepository.save(skipped);
            return CollectionTaskResponse.from(skipped);
        }
        if (deposit.getStatus() != DepositAddressStatus.COLLECTIBLE
                && deposit.getStatus() != DepositAddressStatus.DEPOSITED) {
            throw new IllegalStateException("Deposit status not collectible: " + deposit.getStatus());
        }
        if (!StringUtils.hasText(deposit.getEncryptedPrivateKey())) {
            throw new IllegalStateException("Deposit address has no encrypted private key");
        }

        boolean isolate = deposit.getRiskLevel() == RiskLevel.MEDIUM;
        String toAddress = isolationOrHot(isolate);

        CollectionTaskEntity task = newTask(deposit, request.amount(), toAddress);
        collectionTaskRepository.save(task);

        try {
            SignResponse signed = signNativeTransfer(deposit, toAddress, request.amount());
            task.setTxId(signed.txId());
            task.setStatus(CollectionTaskEntity.Status.DONE);
            deposit.setStatus(isolate ? DepositAddressStatus.ISOLATED : DepositAddressStatus.COLLECTED);
            depositAddressRepository.save(deposit);
        } catch (Exception e) {
            task.setStatus(CollectionTaskEntity.Status.FAILED);
            task.setFailReason(e.getMessage());
        }
        collectionTaskRepository.save(task);
        return CollectionTaskResponse.from(task);
    }

    /** 构建归集任务实体 */
    private CollectionTaskEntity newTask(DepositAddressEntity deposit, BigDecimal amount, String to) {
        CollectionTaskEntity task = new CollectionTaskEntity();
        task.setDepositAddressId(deposit.getId());
        task.setAsset(deposit.getAsset());
        task.setFromAddress(deposit.getAddress());
        task.setToAddress(to);
        task.setAmount(amount);
        return task;
    }

    /** 是否olation or hot */
    private String isolationOrHot(boolean isolate) {
        if (isolate) {
            String isolation = properties.getIsolation().getAddress();
            if (!StringUtils.hasText(isolation)) {
                throw new IllegalStateException("Isolation wallet address not configured");
            }
            return isolation;
        }
        if (!properties.getHot().isConfigured()) {
            throw new IllegalStateException("Hot wallet private key not configured");
        }
        PrivateKey hotKey = new PrivateKey(Hex.decode(properties.getHot().getPrivateKeyHex()));
        CoinType coin = CoinType.TRON;
        PublicKey pub = hotKey.getPublicKey(false);
        return dispatcher.deriveAddress(coin, pub);
    }

    /** 签名native transfer */
    private SignResponse signNativeTransfer(DepositAddressEntity deposit, String to, BigDecimal amount) {
        CoinType coin = CoinType.fromId(deposit.getCoin())
                .orElseThrow(() -> new IllegalArgumentException("Unsupported coin: " + deposit.getCoin()));
        String privateKeyHex = encryptor.decrypt(deposit.getEncryptedPrivateKey());

        return switch (coin) {
            case TRON -> {
                if (!TronAddress.isValid(to)) {
                    throw new IllegalArgumentException("Invalid TRON to address: " + to);
                }
                long sun = amount.movePointRight(6).setScale(0, RoundingMode.DOWN).longValueExact();
                SignRequest req = new SignRequest(
                        coin.getId(), privateKeyHex, null, null,
                        null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null,
                        to, sun, null, null, null, null
                );
                yield dispatcher.sign(coin, req);
            }
            case ETHEREUM, BINANCE_SMART_CHAIN -> {
                long chainId = coin == CoinType.ETHEREUM ? 1L : 56L;
                String wei = amount.movePointRight(18).setScale(0, RoundingMode.DOWN).toPlainString();
                SignRequest req = new SignRequest(
                        coin.getId(), privateKeyHex, null, null,
                        to, wei, null, 0L, "20000000000", "21000", chainId, null, null,
                        null, null, null, null, null,
                        null, null, null, null, null, null
                );
                yield dispatcher.sign(coin, req);
            }
            default -> throw new UnsupportedOperationException("Collection signing not implemented for " + coin);
        };
    }
}
