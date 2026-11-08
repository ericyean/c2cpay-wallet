package com.c2cpay.wallet.crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * secp256k1 私钥封装。
 * <p>提供公钥推导与 ECDSA 签名（low-S 规范化）。</p>
 */
public final class PrivateKey {

    private static final X9ECParameters CURVE_PARAMS = SECNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(),
            CURVE_PARAMS.getG(),
            CURVE_PARAMS.getN(),
            CURVE_PARAMS.getH());

    private final byte[] key;

    /**
     * 使用 32 字节私钥材料构造。
     *
     * @param key 私钥字节，必须在 secp256k1 合法范围内
     * @throws IllegalArgumentException 长度非法或数值越界时
     */
    public PrivateKey(byte[] key) {
        if (key == null || key.length != 32) {
            throw new IllegalArgumentException("Private key must be 32 bytes");
        }
        BigInteger d = new BigInteger(1, key);
        if (d.signum() <= 0 || d.compareTo(CURVE.getN()) >= 0) {
            throw new IllegalArgumentException("Private key out of secp256k1 range");
        }
        this.key = Arrays.copyOf(key, key.length);
    }

    /**
     * 从十六进制字符串构造私钥。
     *
     * @param hex 私钥十六进制（可带 0x）
     * @return 私钥实例
     */
    public static PrivateKey fromHex(String hex) {
        return new PrivateKey(Hex.decode(hex));
    }

    /**
     * 返回私钥字节副本。
     *
     * @return 32 字节私钥
     */
    public byte[] getBytes() {
        return Arrays.copyOf(key, key.length);
    }

    /**
     * 由私钥推导公钥。
     *
     * @param compressed true 返回压缩公钥，false 返回未压缩公钥
     * @return 公钥
     */
    public PublicKey getPublicKey(boolean compressed) {
        BigInteger d = new BigInteger(1, key);
        ECPoint q = CURVE.getG().multiply(d).normalize();
        return new PublicKey(q.getEncoded(compressed));
    }

    /**
     * 对 32 字节消息哈希做 ECDSA 签名。
     * <p>返回 {@code r||s} 共 64 字节，并对 s 做 low-S 规范化。</p>
     *
     * @param hash32 待签哈希，必须为 32 字节
     * @return 64 字节签名（r||s）
     * @throws IllegalArgumentException 哈希长度不是 32 时
     */
    public byte[] sign(byte[] hash32) {
        if (hash32.length != 32) {
            throw new IllegalArgumentException("Hash must be 32 bytes");
        }
        ECDSASigner signer = new ECDSASigner();
        BigInteger d = new BigInteger(1, key);
        signer.init(true, new ECPrivateKeyParameters(d, CURVE));
        BigInteger[] rs = signer.generateSignature(hash32);
        BigInteger r = rs[0];
        BigInteger s = rs[1];
        BigInteger halfN = CURVE.getN().shiftRight(1);
        if (s.compareTo(halfN) > 0) {
            s = CURVE.getN().subtract(s);
        }
        return concat32(r, s);
    }

    /**
     * 将 r、s 拼为固定 64 字节大端数组。
     *
     * @param r 签名 r
     * @param s 签名 s
     * @return r||s
     */
    private static byte[] concat32(BigInteger r, BigInteger s) {
        byte[] out = new byte[64];
        System.arraycopy(toFixed32(r), 0, out, 0, 32);
        System.arraycopy(toFixed32(s), 0, out, 32, 32);
        return out;
    }

    /**
     * 将 BigInteger 编码为固定 32 字节大端表示。
     *
     * @param value 整数值
     * @return 32 字节数组
     */
    private static byte[] toFixed32(BigInteger value) {
        byte[] raw = value.toByteArray();
        byte[] out = new byte[32];
        if (raw.length <= 32) {
            System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
        } else {
            System.arraycopy(raw, raw.length - 32, out, 0, 32);
        }
        return out;
    }
}
