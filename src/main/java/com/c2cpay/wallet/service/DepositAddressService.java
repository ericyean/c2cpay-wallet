package com.c2cpay.wallet.service;

import com.c2cpay.wallet.coin.CoinDispatcher;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.config.WalletProperties;
import com.c2cpay.wallet.crypto.AesGcmEncryptor;
import com.c2cpay.wallet.crypto.HdWallet;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.PrivateKey;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.domain.AssetType;
import com.c2cpay.wallet.domain.DepositAddressEntity;
import com.c2cpay.wallet.domain.DepositAddressStatus;
import com.c2cpay.wallet.domain.HdIndexCounterEntity;
import com.c2cpay.wallet.domain.RiskLevel;
import com.c2cpay.wallet.dto.business.AllocateDepositRequest;
import com.c2cpay.wallet.dto.business.DepositAddressResponse;
import com.c2cpay.wallet.dto.business.MarkDepositRequest;
import com.c2cpay.wallet.repository.DepositAddressRepository;
import com.c2cpay.wallet.repository.HdIndexCounterRepository;
import com.c2cpay.wallet.risk.RiskCheckResult;
import com.c2cpay.wallet.risk.RiskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 代收地址服务。
 * <p>负责 HD 一单一址分配、入账确认与黑 U 风控标记。</p>
 */
@Service
public class DepositAddressService {

    private final WalletProperties properties;
    private final HdPathBuilder pathBuilder;
    private final CoinDispatcher dispatcher;
    private final AesGcmEncryptor encryptor;
    private final DepositAddressRepository depositAddressRepository;
    private final HdIndexCounterRepository hdIndexCounterRepository;
    private final RiskService riskService;

    /** 注入配置、HD、加密与仓储依赖。 */
    public DepositAddressService(
            WalletProperties properties,
            HdPathBuilder pathBuilder,
            CoinDispatcher dispatcher,
            AesGcmEncryptor encryptor,
            DepositAddressRepository depositAddressRepository,
            HdIndexCounterRepository hdIndexCounterRepository,
            RiskService riskService
    ) {
        this.properties = properties;
        this.pathBuilder = pathBuilder;
        this.dispatcher = dispatcher;
        this.encryptor = encryptor;
        this.depositAddressRepository = depositAddressRepository;
        this.hdIndexCounterRepository = hdIndexCounterRepository;
        this.riskService = riskService;
    }

    /**
     * 按业务单号分配 HD 代收地址（幂等）。
     *
     * @param request 业务单号 + 资产类型
     * @return 代收地址信息
     */
    @Transactional
    public DepositAddressResponse allocate(AllocateDepositRequest request) {
        AssetType asset = AssetType.fromCode(request.asset())
                .orElseThrow(() -> new IllegalArgumentException("Unknown asset: " + request.asset()));
        CoinType coin = asset.getCoin();

        return depositAddressRepository.findByBizOrderId(request.bizOrderId())
                .map(DepositAddressResponse::from)
                .orElseGet(() -> createNew(request.bizOrderId(), asset, coin));
    }

    /**
     * 派生新地址并加密落库。
     *
     * @param bizOrderId 业务单号
     * @param asset      资产
     * @param coin       链
     * @return 新建代收地址
     */
    private DepositAddressResponse createNew(String bizOrderId, AssetType asset, CoinType coin) {
        if (!properties.getHd().isConfigured()) {
            throw new IllegalStateException("HD mnemonic not configured (c2cpay.wallet.hd.mnemonic)");
        }

        HdIndexCounterEntity counter = hdIndexCounterRepository.findById(coin.getId())
                .orElseGet(() -> hdIndexCounterRepository.save(new HdIndexCounterEntity(coin.getId(), 0)));
        int index = counter.allocate();
        hdIndexCounterRepository.save(counter);

        String path = pathBuilder.build(coin, index);
        HdWallet hd = HdWallet.fromMnemonic(properties.getHd().getMnemonic());
        PrivateKey privateKey = hd.getKey(path);
        boolean compressed = coin == CoinType.BITCOIN;
        PublicKey publicKey = privateKey.getPublicKey(compressed);
        String address = dispatcher.deriveAddress(coin, publicKey);

        DepositAddressEntity entity = new DepositAddressEntity();
        entity.setBizOrderId(bizOrderId);
        entity.setAsset(asset);
        entity.setCoin(coin.getId());
        entity.setAddress(address);
        entity.setDerivationPath(path);
        entity.setHdIndex(index);
        entity.setEncryptedPrivateKey(encryptor.encrypt(Hex.encode(privateKey.getBytes())));
        entity.setStatus(DepositAddressStatus.ALLOCATED);
        depositAddressRepository.save(entity);
        return DepositAddressResponse.from(entity);
    }

    /**
     * 按业务单号查询代收地址。
     *
     * @param bizOrderId 业务单号
     * @return 代收地址信息
     */
    @Transactional(readOnly = true)
    public DepositAddressResponse getByBizOrderId(String bizOrderId) {
        return depositAddressRepository.findByBizOrderId(bizOrderId)
                .map(DepositAddressResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Deposit address not found: " + bizOrderId));
    }

    /**
     * 确认入账并执行黑 U 风险检测，更新地址状态。
     *
     * @param request 业务单号 + 链上 txId
     * @return 更新后的代收地址（含风控结果）
     */
    @Transactional
    public DepositAddressResponse markDeposited(MarkDepositRequest request) {
        DepositAddressEntity entity = depositAddressRepository.findByBizOrderId(request.bizOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Deposit address not found: " + request.bizOrderId()));

        RiskCheckResult risk = riskService.check(entity.getCoin(), entity.getAddress(), request.txId());
        entity.setRiskLevel(risk.level());

        if (risk.level() == RiskLevel.HIGH) {
            entity.setStatus(DepositAddressStatus.ISOLATED);
        } else if (risk.level() == RiskLevel.MEDIUM || !risk.allowCredit()) {
            entity.setStatus(DepositAddressStatus.DEPOSITED);
        } else {
            entity.setStatus(DepositAddressStatus.COLLECTIBLE);
        }
        depositAddressRepository.save(entity);
        return DepositAddressResponse.from(entity, risk);
    }
}
