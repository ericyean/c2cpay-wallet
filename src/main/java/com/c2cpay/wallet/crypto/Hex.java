package com.c2cpay.wallet.crypto;

/**
 * 十六进制编解码工具。
 */
public final class Hex {

    /** 工具类，禁止实例化。 */
    private Hex() {
    }

    /**
     * 将字节数组编码为小写十六进制字符串（无 0x 前缀）。
     *
     * @param data 原始字节
     * @return 十六进制字符串
     */
    public static String encode(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串解码为字节数组。
     * <p>允许带 {@code 0x}/{@code 0X} 前缀；长度必须为偶数。</p>
     *
     * @param hex 十六进制字符串
     * @return 解码后的字节数组
     * @throws IllegalArgumentException 格式非法时
     */
    public static byte[] decode(String hex) {
        String clean = normalize(hex);
        if ((clean.length() & 1) != 0) {
            throw new IllegalArgumentException("Odd-length hex string");
        }
        byte[] out = new byte[clean.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(clean.charAt(i * 2), 16);
            int lo = Character.digit(clean.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("Invalid hex character");
            }
            out[i] = (byte) ((hi << 4) + lo);
        }
        return out;
    }

    /**
     * 规范化十六进制字符串：去空白、去掉可选 0x 前缀并转小写。
     *
     * @param hex 原始十六进制字符串
     * @return 规范化后的纯十六进制文本
     * @throws IllegalArgumentException 为空时
     */
    public static String normalize(String hex) {
        if (hex == null || hex.isBlank()) {
            throw new IllegalArgumentException("hex required");
        }
        String clean = hex.trim();
        if (clean.startsWith("0x") || clean.startsWith("0X")) {
            clean = clean.substring(2);
        }
        return clean.toLowerCase();
    }
}
