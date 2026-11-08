package com.c2cpay.wallet.domain;

import com.c2cpay.wallet.coin.CoinType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Business assets used by C2C Pay (USDT channels first).
 */
public enum AssetType {
    TRX(CoinType.TRON, null, "TRX", 6),
    USDT_TRC20(CoinType.TRON, "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t", "USDT", 6),
    ETH(CoinType.ETHEREUM, null, "ETH", 18),
    USDT_ERC20(CoinType.ETHEREUM, "0xdAC17F958D2ee523a2206206994597C13D831ec7", "USDT", 6),
    BNB(CoinType.BINANCE_SMART_CHAIN, null, "BNB", 18),
    USDT_BEP20(CoinType.BINANCE_SMART_CHAIN, "0x55d398326f99059fF775485246999027B3197955", "USDT", 18),
    BTC(CoinType.BITCOIN, null, "BTC", 8);

    private final CoinType coin;
    private final String contract;
    private final String symbol;
    private final int decimals;

    AssetType(CoinType coin, String contract, String symbol, int decimals) {
        this.coin = coin;
        this.contract = contract;
        this.symbol = symbol;
        this.decimals = decimals;
    }

    public CoinType getCoin() {
        return coin;
    }

    public String getContract() {
        return contract;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    public boolean isToken() {
        return contract != null;
    }

    public static Optional<AssetType> fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String key = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return Arrays.stream(values()).filter(a -> a.name().equals(key)).findFirst();
    }
}
