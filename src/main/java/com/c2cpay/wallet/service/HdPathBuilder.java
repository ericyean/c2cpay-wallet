package com.c2cpay.wallet.service;

import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.config.WalletProperties;
import org.springframework.stereotype.Component;

/**
 * BIP44 path builder: m/44'/coin'/account'/change/index
 * BTC uses BIP84 default in CoinType for signing demos; deposit HD stays BIP44 for C2C Pay.
 */
@Component
public class HdPathBuilder {

    private final WalletProperties properties;

    public HdPathBuilder(WalletProperties properties) {
        this.properties = properties;
    }

    public String build(CoinType coin, int index) {
        int coinType = switch (coin) {
            case BITCOIN -> 0;
            case ETHEREUM, BINANCE_SMART_CHAIN -> 60;
            case TRON -> 195;
        };
        int account = properties.getHd().getAccount();
        int change = properties.getHd().getChange();
        return String.format("m/44'/%d'/%d'/%d/%d", coinType, account, change, index);
    }
}
