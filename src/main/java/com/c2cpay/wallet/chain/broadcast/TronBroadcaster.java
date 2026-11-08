package com.c2cpay.wallet.chain.broadcast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.config.WalletProperties;
import com.c2cpay.wallet.dto.BroadcastResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/** TronBroadcaster 类 */
@Component
public class TronBroadcaster {

    private final WalletProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    /** 构造 TronBroadcaster */
    public TronBroadcaster(WalletProperties properties) {
        this.properties = properties;
    }

    /** 广播已签名交易到对应链 */
    public BroadcastResponse broadcast(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return BroadcastResponse.fail(CoinType.TRON.getId(), "TRON 广播需要 rawJson", null);
        }
        String base = properties.getTron().getApiBaseUrl();
        if (!StringUtils.hasText(base)) {
            return BroadcastResponse.fail(CoinType.TRON.getId(), "未配置 c2cpay.wallet.tron.api-base-url", null);
        }
        try {
            RestClient.RequestBodySpec spec = restClient.post()
                    .uri(trimSlash(base) + "/wallet/broadcasttransaction")
                    .contentType(MediaType.APPLICATION_JSON);
            String apiKey = properties.getTron().getApiKey();
            if (StringUtils.hasText(apiKey)) {
                spec = spec.header("TRON-PRO-API-KEY", apiKey);
            }
            String body = spec.body(rawJson).retrieve().body(String.class);
            JsonNode root = objectMapper.readTree(body == null ? "{}" : body);
            boolean ok = root.path("result").asBoolean(false);
            String txId = root.path("txid").asText(null);
            if (txId == null && root.has("transaction")) {
                txId = root.path("transaction").path("txID").asText(null);
            }
            if (ok) {
                return BroadcastResponse.ok(CoinType.TRON.getId(), txId, body);
            }
            String err = root.path("message").asText(root.path("code").asText("broadcast failed"));
            return BroadcastResponse.fail(CoinType.TRON.getId(), err, body);
        } catch (Exception e) {
            return BroadcastResponse.fail(CoinType.TRON.getId(), e.getMessage(), null);
        }
    }

    /** 去掉 URL 末尾斜杠 */
    private static String trimSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
