package com.c2cpay.wallet.crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.util.Arrays;

/**
 * secp256k1 公钥封装。
 * <p>支持压缩（33 字节）与未压缩（65 字节，含 0x04 前缀）两种编码。</p>
 */
public final class PublicKey {

    private final byte[] data;

    /**
     * 使用原始公钥字节构造。
     *
     * @param data 33 字节压缩公钥，或 65 字节未压缩公钥
     * @throws IllegalArgumentException 长度非法时
     */
    public PublicKey(byte[] data) {
        if (data == null || !(data.length == 33 || data.length == 65)) {
            throw new IllegalArgumentException("Public key must be 33 or 65 bytes");
        }
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * 从十六进制字符串构造公钥。
     *
     * @param hex 公钥十六进制（可带 0x）
     * @return 公钥实例
     */
    public static PublicKey fromHex(String hex) {
        return new PublicKey(Hex.decode(hex));
    }

    /**
     * 返回公钥字节副本。
     *
     * @return 压缩或未压缩编码的字节数组
     */
    public byte[] getBytes() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * 是否为压缩公钥。
     *
     * @return 长度为 33 时返回 true
     */
    public boolean isCompressed() {
        return data.length == 33;
    }

    /**
     * 转为压缩公钥（若已是压缩则返回自身）。
     *
     * @return 压缩公钥
     */
    public PublicKey compressed() {
        if (isCompressed()) {
            return this;
        }
        return new PublicKey(decodePoint().getEncoded(true));
    }

    /**
     * 转为未压缩公钥（若已是未压缩则返回自身）。
     *
     * @return 未压缩公钥
     */
    public PublicKey uncompressed() {
        if (!isCompressed()) {
            return this;
        }
        return new PublicKey(decodePoint().getEncoded(false));
    }

    /**
     * 将当前编码解码为 secp256k1 曲线点。
     *
     * @return 归一化后的椭圆曲线点
     */
    private ECPoint decodePoint() {
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        return params.getCurve().decodePoint(data).normalize();
    }
}
