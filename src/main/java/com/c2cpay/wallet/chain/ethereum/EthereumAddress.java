package com.c2cpay.wallet.chain.ethereum;

import com.c2cpay.wallet.crypto.Hashing;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.PublicKey;

import java.util.Arrays;
import java.util.Locale;

/** EthereumAddress 类 */
public final class EthereumAddress {

    /** 构造 EthereumAddress */
    private EthereumAddress() {
    }

    /** 由公钥派生地址 */
    public static String fromPublicKey(PublicKey publicKey) {
        byte[] uncompressed = publicKey.uncompressed().getBytes();
        // drop 0x04 prefix
        byte[] pub = Arrays.copyOfRange(uncompressed, 1, uncompressed.length);
        byte[] hash = Hashing.keccak256(pub);
        byte[] address = Arrays.copyOfRange(hash, 12, 32);
        return toChecksumAddress("0x" + Hex.encode(address));
    }

    /** 是否valid */
    public static boolean isValid(String address) {
        if (address == null) {
            return false;
        }
        String a = address.trim();
        if (!a.matches("^0x[0-9a-fA-F]{40}$")) {
            return false;
        }
        // If mixed case, verify EIP-55 checksum
        String body = a.substring(2);
        if (body.equals(body.toLowerCase(Locale.ROOT)) || body.equals(body.toUpperCase(Locale.ROOT))) {
            return true;
        }
        return a.equals(toChecksumAddress(a));
    }

    /** 转换为EIP-55 校验和地址 */
    public static String toChecksumAddress(String address) {
        String lower = address.substring(2).toLowerCase(Locale.ROOT);
        byte[] hash = Hashing.keccak256(lower.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        StringBuilder sb = new StringBuilder("0x");
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            int nibble = (hash[i / 2] >> (i % 2 == 0 ? 4 : 0)) & 0xF;
            sb.append(nibble >= 8 ? Character.toUpperCase(c) : c);
        }
        return sb.toString();
    }
}
