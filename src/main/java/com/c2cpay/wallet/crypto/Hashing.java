package com.c2cpay.wallet.crypto;

import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 常用摘要算法工具（SHA-256 / RIPEMD160 / Keccak-256 / SHA-512）。
 */
public final class Hashing {

    /** 工具类，禁止实例化。 */
    private Hashing() {
    }

    /**
     * 计算 SHA-256 摘要。
     *
     * @param data 输入数据
     * @return 32 字节摘要
     */
    public static byte[] sha256(byte[] data) {
        SHA256Digest digest = new SHA256Digest();
        digest.update(data, 0, data.length);
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return out;
    }

    /**
     * 计算双 SHA-256（Bitcoin 风格 checksum / txid 常用）。
     *
     * @param data 输入数据
     * @return 32 字节摘要
     */
    public static byte[] sha256d(byte[] data) {
        return sha256(sha256(data));
    }

    /**
     * 计算 RIPEMD-160 摘要。
     *
     * @param data 输入数据
     * @return 20 字节摘要
     */
    public static byte[] ripemd160(byte[] data) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(data, 0, data.length);
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return out;
    }

    /**
     * 计算 HASH160：{@code RIPEMD160(SHA256(data))}，常用于 BTC 地址。
     *
     * @param data 输入数据
     * @return 20 字节摘要
     */
    public static byte[] hash160(byte[] data) {
        return ripemd160(sha256(data));
    }

    /**
     * 计算 Keccak-256 摘要（以太坊 / TRON 地址派生使用）。
     *
     * @param data 输入数据
     * @return 32 字节摘要
     */
    public static byte[] keccak256(byte[] data) {
        KeccakDigest digest = new KeccakDigest(256);
        digest.update(data, 0, data.length);
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return out;
    }

    /**
     * 计算 SHA-512 摘要。
     *
     * @param data 输入数据
     * @return 64 字节摘要
     */
    public static byte[] sha512(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-512").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
