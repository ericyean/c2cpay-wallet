package com.c2cpay.wallet.service;

import com.c2cpay.wallet.chain.broadcast.BitcoinBroadcaster;
import com.c2cpay.wallet.chain.broadcast.EvmBroadcaster;
import com.c2cpay.wallet.chain.broadcast.TronBroadcaster;
import com.c2cpay.wallet.coin.CoinDispatcher;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.dto.BroadcastRequest;
import com.c2cpay.wallet.dto.BroadcastResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/** 链上广播门面 */
@Service
public class BroadcastService {

    private final CoinDispatcher dispatcher;
    private final EvmBroadcaster evmBroadcaster;
    private final TronBroadcaster tronBroadcaster;
    private final BitcoinBroadcaster bitcoinBroadcaster;

    public BroadcastService(
            CoinDispatcher dispatcher,
            EvmBroadcaster evmBroadcaster,
            TronBroadcaster tronBroadcaster,
            BitcoinBroadcaster bitcoinBroadcaster
    ) {
        this.dispatcher = dispatcher;
        this.evmBroadcaster = evmBroadcaster;
        this.tronBroadcaster = tronBroadcaster;
        this.bitcoinBroadcaster = bitcoinBroadcaster;
    }

    /** 广播已签名交易到对应链 */
    public BroadcastResponse broadcast(BroadcastRequest request) {
        CoinType coin = resolveCoin(request.coin());
        return switch (coin) {
            case ETHEREUM, BINANCE_SMART_CHAIN -> {
                if (!StringUtils.hasText(request.encoded())) {
                    yield BroadcastResponse.fail(coin.getId(), "EVM 广播需要 encoded", null);
                }
                yield evmBroadcaster.broadcast(coin, request.encoded());
            }
            case TRON -> {
                if (StringUtils.hasText(request.rawJson())) {
                    yield tronBroadcaster.broadcast(request.rawJson());
                }
                yield BroadcastResponse.fail(coin.getId(), "TRON 广播需要 rawJson（签名接口返回）", null);
            }
            case BITCOIN -> {
                if (!StringUtils.hasText(request.encoded())) {
                    yield BroadcastResponse.fail(coin.getId(), "BTC 广播需要 encoded", null);
                }
                yield bitcoinBroadcaster.broadcast(request.encoded());
            }
        };
    }

    /** 解析币种 */
    private CoinType resolveCoin(String raw) {
        return CoinType.fromId(raw)
                .or(() -> {
                    try {
                        return CoinType.fromCoinId(Integer.parseInt(raw.trim()));
                    } catch (NumberFormatException e) {
                        return java.util.Optional.empty();
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException(
                        "未知币种 '" + raw + "'，支持: " +
                                Arrays.toString(dispatcher.supportedCoins().toArray())));
    }
}
