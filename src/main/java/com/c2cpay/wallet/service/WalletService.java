package com.c2cpay.wallet.service;

import com.c2cpay.wallet.coin.CoinDispatcher;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.crypto.HdWallet;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.KeyResolver;
import com.c2cpay.wallet.crypto.PrivateKey;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.CoinInfoResponse;
import com.c2cpay.wallet.dto.DeriveAddressRequest;
import com.c2cpay.wallet.dto.DeriveAddressResponse;
import com.c2cpay.wallet.dto.MnemonicResponse;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import com.c2cpay.wallet.dto.ValidateAddressRequest;
import com.c2cpay.wallet.dto.ValidateAddressResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 无状态钱包核心服务。
 * <p>负责助记词、地址派生/校验、交易签名；密钥仅在请求期内使用。</p>
 */
@Service
public class WalletService {

    private final CoinDispatcher dispatcher;
    private final KeyResolver keyResolver;

    /** 注入币种分发器与密钥解析器。 */
    public WalletService(CoinDispatcher dispatcher, KeyResolver keyResolver) {
        this.dispatcher = dispatcher;
        this.keyResolver = keyResolver;
    }

    /**
     * 生成 BIP39 助记词。
     *
     * @param strengthBits 熵强度，128 或 256
     * @return 助记词及单词数
     */
    public MnemonicResponse createMnemonic(int strengthBits) {
        HdWallet wallet = HdWallet.create(strengthBits);
        return new MnemonicResponse(wallet.mnemonic(), wallet.words().size());
    }

    /**
     * 校验 BIP39 助记词是否合法。
     *
     * @param mnemonic 助记词
     * @return true 表示合法
     */
    public boolean validateMnemonic(String mnemonic) {
        return HdWallet.isValidMnemonic(mnemonic);
    }

    /** 列出已注册支持的币种信息。 */
    public List<CoinInfoResponse> listCoins() {
        return dispatcher.supportedCoins().stream()
                .map(c -> new CoinInfoResponse(
                        c.getCoinId(),
                        c.getId(),
                        c.getSymbol(),
                        c.getDecimals(),
                        c.getDefaultDerivationPath()))
                .toList();
    }

    /**
     * 按币种校验链上地址。
     *
     * @param request 币种 + 地址
     * @return 校验结果
     */
    public ValidateAddressResponse validateAddress(ValidateAddressRequest request) {
        CoinType coin = resolveCoin(request.coin());
        boolean valid = dispatcher.validateAddress(coin, request.address());
        return new ValidateAddressResponse(coin.getId(), request.address(), valid);
    }

    /**
     * 从助记词或私钥派生地址。
     *
     * @param request 派生参数
     * @return 地址、公钥与实际路径
     */
    public DeriveAddressResponse deriveAddress(DeriveAddressRequest request) {
        CoinType coin = resolveCoin(request.coin());
        PrivateKey privateKey = keyResolver.resolve(coin, request);
        boolean compressed = coin == CoinType.BITCOIN;
        PublicKey publicKey = privateKey.getPublicKey(compressed);
        String path = keyResolver.effectivePath(coin, request.derivationPath());
        String address = dispatcher.deriveAddress(coin, publicKey);
        return new DeriveAddressResponse(coin.getId(), address, Hex.encode(publicKey.getBytes()), path);
    }

    /**
     * 按币种签名交易。
     *
     * @param request 统一签名入参
     * @return 已签名交易
     */
    public SignResponse sign(SignRequest request) {
        CoinType coin = resolveCoin(request.coin());
        return dispatcher.sign(coin, request);
    }

    /**
     * 将币种字符串或 coinId 解析为 {@link CoinType}。
     *
     * @param raw 币种 id / 符号 / coinId
     * @return 币种枚举
     */
    private CoinType resolveCoin(String raw) {
        return CoinType.fromId(raw)
                .or(() -> {
                    try {
                        return CoinType.fromCoinId(Integer.parseInt(raw.trim()));
                    } catch (NumberFormatException e) {
                        return java.util.Optional.empty();
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown coin '" + raw + "'. Supported: " +
                                Arrays.toString(dispatcher.supportedCoins().toArray())));
    }
}
