package com.c2cpay.wallet.chain.tron;

import com.c2cpay.wallet.crypto.Base58Check;
import com.c2cpay.wallet.crypto.Hashing;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.PublicKey;

import java.util.Arrays;

/** TronAddress 类 */
public final class TronAddress {

    public static final byte PREFIX = 0x41;

    /** 构造 TronAddress */
    private TronAddress() {
    }

    /** 由公钥派生地址 */
    public static String fromPublicKey(PublicKey publicKey) {
        byte[] uncompressed = publicKey.uncompressed().getBytes();
        byte[] pub = Arrays.copyOfRange(uncompressed, 1, uncompressed.length);
        byte[] hash = Hashing.keccak256(pub);
        byte[] address20 = Arrays.copyOfRange(hash, 12, 32);
        byte[] payload = new byte[21];
        payload[0] = PREFIX;
        System.arraycopy(address20, 0, payload, 1, 20);
        return Base58Check.encode(payload);
    }

    /** 是否valid */
    public static boolean isValid(String address) {
        try {
            byte[] payload = Base58Check.decode(address.trim());
            return payload.length == 21 && payload[0] == PREFIX;
        } catch (Exception e) {
            return false;
        }
    }

    /** 地址转字节 */
    public static byte[] toBytes(String address) {
        byte[] payload = Base58Check.decode(address.trim());
        if (payload.length != 21 || payload[0] != PREFIX) {
            throw new IllegalArgumentException("Invalid TRON address");
        }
        return payload;
    }

    /** 地址转十六进制 */
    public static String toHex(String address) {
        return Hex.encode(toBytes(address));
    }
}
