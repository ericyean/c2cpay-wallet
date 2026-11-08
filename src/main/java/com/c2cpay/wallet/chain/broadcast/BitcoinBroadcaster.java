package com.c2cpay.wallet.chain.broadcast;

import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.config.WalletProperties;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.dto.BroadcastResponse;
import org.bitcoinj.base.Sha256Hash;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** BitcoinBroadcaster 类 */
@Component
public class BitcoinBroadcaster {

    private final WalletProperties properties;
    private final RestClient restClient = RestClient.create();

    /** 构造 BitcoinBroadcaster */
    public BitcoinBroadcaster(WalletProperties properties) {
        this.properties = properties;
    }

    /** 广播已签名交易到对应链 */
    public BroadcastResponse broadcast(String encodedHex) {
        String base = properties.getBitcoin().getApiBaseUrl();
        if (!StringUtils.hasText(base)) {
            return BroadcastResponse.fail(CoinType.BITCOIN.getId(),
                    "未配置 c2cpay.wallet.bitcoin.api-base-url", null);
        }
        try {
            byte[] raw = Hex.decode(encodedHex);
            // 本地先算 txid（无见证双 SHA256）方便返回；节点成功时通常回显同样的 txid
            String localTxId = Hex.encode(reverse(Sha256Hash.hashTwice(stripWitnessBestEffort(raw))));

            String body = restClient.post()
                    .uri(trimSlash(base) + "/tx")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Hex.normalize(encodedHex))
                    .retrieve()
                    .body(String.class);
            String txId = StringUtils.hasText(body) ? body.trim() : localTxId;
            return BroadcastResponse.ok(CoinType.BITCOIN.getId(), txId, body);
        } catch (RestClientResponseException e) {
            return BroadcastResponse.fail(CoinType.BITCOIN.getId(),
                    e.getResponseBodyAsString(), e.getMessage());
        } catch (Exception e) {
            return BroadcastResponse.fail(CoinType.BITCOIN.getId(), e.getMessage(), null);
        }
    }

    /** 若已是无见证序列化则原样返回；segwit 时交给节点，本地 txid 仅作兜底。 */
    private static byte[] stripWitnessBestEffort(byte[] raw) {
        return raw;
    }

    /** 反转 */
    private static byte[] reverse(byte[] in) {
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[in.length - 1 - i];
        }
        return out;
    }

    /** 去掉 URL 末尾斜杠 */
    private static String trimSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
