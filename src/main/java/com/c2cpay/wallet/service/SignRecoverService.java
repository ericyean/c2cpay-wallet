package com.c2cpay.wallet.service;

import com.c2cpay.wallet.chain.recover.BitcoinSignRecoverer;
import com.c2cpay.wallet.chain.recover.EvmSignRecoverer;
import com.c2cpay.wallet.chain.recover.TronSignRecoverer;
import com.c2cpay.wallet.coin.CoinDispatcher;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.dto.RecoverSignRequest;
import com.c2cpay.wallet.dto.RecoverSignResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/** 签名恢复验签门面 */
@Service
public class SignRecoverService {

    private final CoinDispatcher dispatcher;
    private final EvmSignRecoverer evmSignRecoverer;
    private final TronSignRecoverer tronSignRecoverer;
    private final BitcoinSignRecoverer bitcoinSignRecoverer;

    public SignRecoverService(
            CoinDispatcher dispatcher,
            EvmSignRecoverer evmSignRecoverer,
            TronSignRecoverer tronSignRecoverer,
            BitcoinSignRecoverer bitcoinSignRecoverer
    ) {
        this.dispatcher = dispatcher;
        this.evmSignRecoverer = evmSignRecoverer;
        this.tronSignRecoverer = tronSignRecoverer;
        this.bitcoinSignRecoverer = bitcoinSignRecoverer;
    }

    /** 从已签名交易恢复付款地址并验签 */
    public RecoverSignResponse recover(RecoverSignRequest request) {
        CoinType coin = resolveCoin(request.coin());
        return switch (coin) {
            case ETHEREUM, BINANCE_SMART_CHAIN -> evmSignRecoverer.recover(coin, request);
            case TRON -> tronSignRecoverer.recover(request);
            case BITCOIN -> bitcoinSignRecoverer.recover(request);
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
