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

    public int getCoinId() {
        return coinId;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    public String getDefaultDerivationPath() {
        return defaultDerivationPath;
    }

    public Curve getCurve() {
        return curve;
    }

    public static Optional<CoinType> fromId(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String key = raw.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(c -> c.id.equals(key) || c.name().equalsIgnoreCase(key) || c.symbol.equalsIgnoreCase(key))
                .findFirst();
    }

    public static Optional<CoinType> fromCoinId(int coinId) {
        return Arrays.stream(values()).filter(c -> c.coinId == coinId).findFirst();
    }
}
