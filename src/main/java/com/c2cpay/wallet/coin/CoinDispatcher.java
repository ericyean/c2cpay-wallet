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

    /** 构造 CoinDispatcher */
    public CoinDispatcher(List<CoinEntry> coinEntries) {
        for (CoinEntry entry : coinEntries) {
            entries.put(entry.coinType(), entry);
        }
    }

    /** 获取已注册的 CoinEntry，不存在则抛错 */
    public CoinEntry require(CoinType coinType) {
        CoinEntry entry = entries.get(coinType);
        if (entry == null) {
            throw new IllegalArgumentException("Unsupported coin: " + coinType);
        }
        return entry;
    }

    /** 校验地址 */
    public boolean validateAddress(CoinType coinType, String address) {
        return require(coinType).validateAddress(address);
    }

    /** 派生地址 */
    public String deriveAddress(CoinType coinType, PublicKey publicKey) {
        return require(coinType).deriveAddress(publicKey);
    }

    /** 按币种签名交易 */
    public SignResponse sign(CoinType coinType, SignRequest request) {
        return require(coinType).sign(request);
    }

    /** 返回已注册支持的币种列表 */
    public List<CoinType> supportedCoins() {
        return List.copyOf(entries.keySet());
    }
}
