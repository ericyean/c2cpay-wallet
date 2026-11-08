package com.c2cpay.wallet.crypto;

public final class Hex {

    private Hex() {
    }

    public static String encode(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

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

    /** Strip optional 0x prefix and lowercase. */
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
