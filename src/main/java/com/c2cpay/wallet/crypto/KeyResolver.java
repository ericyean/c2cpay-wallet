package com.c2cpay.wallet.crypto;

import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.dto.DeriveAddressRequest;
import com.c2cpay.wallet.dto.SignRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 密钥解析器。
 * <p>
 * 从请求中的私钥十六进制或 BIP39 助记词解析出 {@link PrivateKey}，
 * 供地址派生、交易签名等无状态接口复用。
 * </p>
 * <p>优先使用私钥；未提供私钥时再使用助记词 + 派生路径。</p>
 */
@Component
public class KeyResolver {

    /**
     * 根据私钥或助记词解析私钥对象。
     * <ul>
     *   <li>若 {@code privateKeyHex} 非空：直接按十六进制还原私钥</li>
     *   <li>否则若 {@code mnemonic} 非空：按派生路径从 HD 钱包导出私钥</li>
     *   <li>两者皆空：抛出 {@link IllegalArgumentException}</li>
     * </ul>
     *
     * @param coinType         币种（用于取默认派生路径）
     * @param privateKeyHex    私钥十六进制，可带 {@code 0x} 前缀；与助记词二选一
     * @param mnemonic         BIP39 助记词；与私钥二选一
     * @param derivationPath   HD 派生路径；为空时使用币种默认路径
     * @return 解析得到的私钥
     * @throws IllegalArgumentException 未提供私钥且未提供助记词，或材料非法时
     */
    public PrivateKey resolve(CoinType coinType, String privateKeyHex, String mnemonic, String derivationPath) {
        if (StringUtils.hasText(privateKeyHex)) {
            return PrivateKey.fromHex(privateKeyHex.trim());
        }
        if (StringUtils.hasText(mnemonic)) {
            String path = StringUtils.hasText(derivationPath) ? derivationPath : coinType.getDefaultDerivationPath();
            return HdWallet.fromMnemonic(mnemonic).getKey(path);
        }
        throw new IllegalArgumentException("Provide either privateKeyHex or mnemonic");
    }

    /**
     * 从签名请求中解析私钥。
     * <p>读取 {@link SignRequest} 的 {@code privateKeyHex} / {@code mnemonic} / {@code derivationPath}。</p>
     *
     * @param coinType 币种
     * @param request  交易签名请求
     * @return 解析得到的私钥
     * @throws IllegalArgumentException 请求中未提供可用密钥材料时
     */
    public PrivateKey resolve(CoinType coinType, SignRequest request) {
        return resolve(coinType, request.privateKeyHex(), request.mnemonic(), request.derivationPath());
    }

    /**
     * 从地址派生请求中解析私钥。
     * <p>读取 {@link DeriveAddressRequest} 的 {@code privateKeyHex} / {@code mnemonic} / {@code derivationPath}。</p>
     *
     * @param coinType 币种
     * @param request  地址派生请求
     * @return 解析得到的私钥
     * @throws IllegalArgumentException 请求中未提供可用密钥材料时
     */
    public PrivateKey resolve(CoinType coinType, DeriveAddressRequest request) {
        return resolve(coinType, request.privateKeyHex(), request.mnemonic(), request.derivationPath());
    }

    /**
     * 计算实际使用的 HD 派生路径。
     * <p>调用方传入路径则用之；否则回退到该币种的默认派生路径。</p>
     *
     * @param coinType       币种
     * @param derivationPath 调用方指定的路径，可为 {@code null} 或空串
     * @return 非空的派生路径字符串，例如 {@code m/44'/60'/0'/0/0}
     */
    public String effectivePath(CoinType coinType, String derivationPath) {
        return StringUtils.hasText(derivationPath) ? derivationPath : coinType.getDefaultDerivationPath();
    }
}
