package com.c2cpay.wallet.coin;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Subset of Trust Wallet Core TWCoinType / registry.json coin IDs.
 */
public enum CoinType {
    BITCOIN(0, "bitcoin", "BTC", 8, "m/84'/0'/0'/0/0", Curve.SECP256K1),
    ETHEREUM(60, "ethereum", "ETH", 18, "m/44'/60'/0'/0/0", Curve.SECP256K1),
    BINANCE_SMART_CHAIN(20000714, "smartchain", "BNB", 18, "m/44'/60'/0'/0/0", Curve.SECP256K1),
  /** tron */
    TRON(195, "tron", "TRX", 6, "m/44'/195'/0'/0/0", Curve.SECP256K1);

    public enum Curve {
        SECP256K1
    }

    private final int coinId;
    private final String id;
    private final String symbol;
    private final int decimals;
    private final String defaultDerivationPath;
    private final Curve curve;

    CoinType(int coinId, String id, String symbol, int decimals, String defaultDerivationPath, Curve curve) {
        this.coinId = coinId;
        this.id = id;
        this.symbol = symbol;
        this.decimals = decimals;
        this.defaultDerivationPath = defaultDerivationPath;
        this.curve = curve;
    }

    /** 获取coin ID */
    public int getCoinId() {
        return coinId;
    }

    /** 获取id */
    public String getId() {
        return id;
    }

    /** 获取symbol */
    public String getSymbol() {
        return symbol;
    }

    /** 获取decimals */
    public int getDecimals() {
        return decimals;
    }

    /** 获取default 派生路径 */
    public String getDefaultDerivationPath() {
        return defaultDerivationPath;
    }

    /** 获取curve */
    public Curve getCurve() {
        return curve;
    }

    /** 按币种 id/符号解析枚举 */
    public static Optional<CoinType> fromId(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String key = raw.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(c -> c.id.equals(key) || c.name().equalsIgnoreCase(key) || c.symbol.equalsIgnoreCase(key))
                .findFirst();
    }

    /** 从参数构造coin ID */
    public static Optional<CoinType> fromCoinId(int coinId) {
        return Arrays.stream(values()).filter(c -> c.coinId == coinId).findFirst();
    }
}
