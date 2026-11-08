package com.c2cpay.wallet.crypto;

import java.util.Arrays;

/**
 * Bitcoin 风格 Base58Check 编解码。
 * <p>用于 BTC / TRON 等地址的 payload + 4 字节 checksum（双 SHA-256）编码。</p>
 */
public final class Base58Check {

    private static final char[] ALPHABET =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int[] INDEXES = new int[128];

    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }

    /** 工具类，禁止实例化。 */
    private Base58Check() {
    }

    /**
     * Base58Check 编码：对 payload 追加 4 字节 checksum 后再做 Base58。
     *
     * @param payload 原始载荷（如版本前缀 + 地址哈希）
     * @return Base58Check 字符串
     */
    public static String encode(byte[] payload) {
        byte[] checksum = Hashing.sha256d(payload);
        byte[] data = new byte[payload.length + 4];
        System.arraycopy(payload, 0, data, 0, payload.length);
        System.arraycopy(checksum, 0, data, payload.length, 4);
        return encodeRaw(data);
    }

    /**
     * Base58Check 解码并校验 checksum。
     *
     * @param input Base58Check 字符串
     * @return 去掉 checksum 后的 payload
     * @throws IllegalArgumentException 字符非法或 checksum 不匹配时
     */
    public static byte[] decode(String input) {
        byte[] decoded = decodeRaw(input);
        if (decoded.length < 4) {
            throw new IllegalArgumentException("Input too short");
        }
        byte[] payload = Arrays.copyOfRange(decoded, 0, decoded.length - 4);
        byte[] checksum = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);
        byte[] expected = Arrays.copyOfRange(Hashing.sha256d(payload), 0, 4);
        if (!Arrays.equals(checksum, expected)) {
            throw new IllegalArgumentException("Invalid Base58Check checksum");
        }
        return payload;
    }

    /**
     * 纯 Base58 编码（不含 checksum 逻辑）。
     *
     * @param input 待编码字节
     * @return Base58 字符串
     */
    private static String encodeRaw(byte[] input) {
        int zeros = 0;
        while (zeros < input.length && input[zeros] == 0) {
            zeros++;
        }
        byte[] input58 = Arrays.copyOf(input, input.length);
        char[] encoded = new char[input.length * 2];
        int outputStart = encoded.length;
        for (int inputStart = zeros; inputStart < input58.length; ) {
            encoded[--outputStart] = ALPHABET[divmod(input58, inputStart, 256, 58)];
            if (input58[inputStart] == 0) {
                inputStart++;
            }
        }
        while (outputStart < encoded.length && encoded[outputStart] == ALPHABET[0]) {
            outputStart++;
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ALPHABET[0];
        }
        return new String(encoded, outputStart, encoded.length - outputStart);
    }

    /**
     * 纯 Base58 解码（不含 checksum 校验）。
     *
     * @param input Base58 字符串
     * @return 解码字节
     */
    private static byte[] decodeRaw(String input) {
        if (input.isEmpty()) {
            return new byte[0];
        }
        byte[] input58 = new byte[input.length()];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int digit = c < 128 ? INDEXES[c] : -1;
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid Base58 character: " + c);
            }
            input58[i] = (byte) digit;
        }
        int zeros = 0;
        while (zeros < input58.length && input58[zeros] == 0) {
            zeros++;
        }
        byte[] decoded = new byte[input.length()];
        int outputStart = decoded.length;
        for (int inputStart = zeros; inputStart < input58.length; ) {
            decoded[--outputStart] = divmod(input58, inputStart, 58, 256);
            if (input58[inputStart] == 0) {
                inputStart++;
            }
        }
        while (outputStart < decoded.length && decoded[outputStart] == 0) {
            outputStart++;
        }
        return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
    }

    /**
     * 对大端数字数组做一次除法，返回余数，并原位写回商。
     *
     * @param number     数字数组（会被原地修改）
     * @param firstDigit 有效最高位下标
     * @param base       当前进制
     * @param divisor    除数
     * @return 余数
     */
    private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            int digit = number[i] & 0xFF;
            int temp = remainder * base + digit;
            number[i] = (byte) (temp / divisor);
            remainder = temp % divisor;
        }
        return (byte) remainder;
    }
}
