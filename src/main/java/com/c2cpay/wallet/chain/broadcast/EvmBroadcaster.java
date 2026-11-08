package com.c2cpay.wallet.chain.broadcast;

import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.config.WalletProperties;
import com.c2cpay.wallet.dto.BroadcastResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

/** EvmBroadcaster 类 */
@Component
public class EvmBroadcaster {

    private final WalletProperties properties;

    /** 构造 EvmBroadcaster */
    public EvmBroadcaster(WalletProperties properties) {
        this.properties = properties;
    }

    /** 广播已签名交易到对应链 */
    public BroadcastResponse broadcast(CoinType coin, String encodedHex) {
        String rpc = rpcUrl(coin);
        if (!StringUtils.hasText(rpc)) {
            return BroadcastResponse.fail(coin.getId(),
                    "未配置 RPC：" + (coin == CoinType.ETHEREUM
                            ? "c2cpay.wallet.ethereum.rpc-url"
                            : "c2cpay.wallet.smartchain.rpc-url"),
                    null);
        }
        try {
            Web3j web3j = Web3j.build(new HttpService(rpc));
            EthSendTransaction resp = web3j.ethSendRawTransaction(Numeric.prependHexPrefix(encodedHex)).send();
            if (resp.hasError()) {
                return BroadcastResponse.fail(coin.getId(), resp.getError().getMessage(), resp.getError().toString());
            }
            return BroadcastResponse.ok(coin.getId(), resp.getTransactionHash(), resp.getResult());
        } catch (Exception e) {
            return BroadcastResponse.fail(coin.getId(), e.getMessage(), null);
        }
    }

    /** 解析 RPCurl */
    private String rpcUrl(CoinType coin) {
        return switch (coin) {
            case ETHEREUM -> properties.getEthereum().getRpcUrl();
            case BINANCE_SMART_CHAIN -> properties.getSmartchain().getRpcUrl();
            default -> null;
        };
    }
}
