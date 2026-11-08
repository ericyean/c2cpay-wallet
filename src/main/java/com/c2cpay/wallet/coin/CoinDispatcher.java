package com.c2cpay.wallet.coin;

import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Analogous to C++ {@code coinDispatcher(coinType)} in Coin.cpp.
 */
@Component
public class CoinDispatcher {

    private final Map<CoinType, CoinEntry> entries = new EnumMap<>(CoinType.class);

    public CoinDispatcher(List<CoinEntry> coinEntries) {
        for (CoinEntry entry : coinEntries) {
            entries.put(entry.coinType(), entry);
        }
    }

    public CoinEntry require(CoinType coinType) {
        CoinEntry entry = entries.get(coinType);
        if (entry == null) {
            throw new IllegalArgumentException("Unsupported coin: " + coinType);
        }
        return entry;
    }

    public boolean validateAddress(CoinType coinType, String address) {
        return require(coinType).validateAddress(address);
    }

    public String deriveAddress(CoinType coinType, PublicKey publicKey) {
        return require(coinType).deriveAddress(publicKey);
    }

    public SignResponse sign(CoinType coinType, SignRequest request) {
        return require(coinType).sign(request);
    }

    public List<CoinType> supportedCoins() {
        return List.copyOf(entries.keySet());
    }
}
