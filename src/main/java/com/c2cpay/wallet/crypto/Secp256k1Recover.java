package com.c2cpay.wallet.crypto;

import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * secp256k1 ECDSA 公钥恢复工具。
 * <p>与以太坊 / TRON 可恢复签名（含 recovery id）兼容，用于签名验签（recover 付款地址）。</p>
 */
public final class Secp256k1Recover {

    /** 工具类，禁止实例化。 */
    private Secp256k1Recover() {
    }

    /**
     * 从消息哈希与 65 字节可恢复签名还原公钥。
     *
     * @param hash32      32 字节消息哈希（已是最终待签 hash，不再二次 keccak）
     * @param signature65 {@code r(32) || s(32) || v(1)}，v 可为 0/1 或 27/28
     * @return 未压缩公钥（65 字节，含 0x04 前缀）
     * @throws IllegalArgumentException 参数长度非法、v 非法或无法恢复时
     */
    public static PublicKey recoverPublicKey(byte[] hash32, byte[] signature65) {
        if (hash32 == null || hash32.length != 32) {
            throw new IllegalArgumentException("hash 必须为 32 字节");
        }
        if (signature65 == null || signature65.length != 65) {
            throw new IllegalArgumentException("signature 必须为 65 字节 (r||s||v)");
        }
        byte[] r = new byte[32];
        byte[] s = new byte[32];
        System.arraycopy(signature65, 0, r, 0, 32);
        System.arraycopy(signature65, 32, s, 0, 32);
        int v = signature65[64] & 0xFF;
        if (v >= 27) {
            v -= 27;
        }
        if (v != 0 && v != 1) {
            throw new IllegalArgumentException("无效的 recovery id v=" + (signature65[64] & 0xFF));
        }

        ECDSASignature sig = new ECDSASignature(Numeric.toBigInt(r), Numeric.toBigInt(s));
        BigInteger pub = Sign.recoverFromSignature(v, sig, hash32);
        if (pub == null) {
            throw new IllegalArgumentException("无法从签名恢复公钥");
        }
        byte[] encoded = Numeric.toBytesPadded(pub, 64);
        byte[] uncompressed = new byte[65];
        uncompressed[0] = 0x04;
        System.arraycopy(encoded, 0, uncompressed, 1, 64);
        return new PublicKey(uncompressed);
    }
}
