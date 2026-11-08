package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** BroadcastResponse 类 */
@Schema(description = "广播响应")
public record BroadcastResponse(
        @Schema(description = "币种 id")
        String coin,
        @Schema(description = "是否广播成功")
        boolean success,
        @Schema(description = "链上交易哈希")
        String txId,
        @Schema(description = "节点/网关原始返回（便于排错）")
        String rawResult,
        @Schema(description = "失败原因")
        String error
) {
    /** ok */
    public static BroadcastResponse ok(String coin, String txId, String rawResult) {
        return new BroadcastResponse(coin, true, txId, rawResult, null);
    }

    /** fail */
    public static BroadcastResponse fail(String coin, String error, String rawResult) {
        return new BroadcastResponse(coin, false, null, rawResult, error);
    }
}
