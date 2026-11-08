package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** SignResponse 类 */
@Schema(description = "交易签名响应")
public record SignResponse(
        @Schema(description = "币种 id")
        String coin,
        @Schema(description = "已签名原始交易（hex）")
        String encoded,
        @Schema(description = "交易哈希 / txId")
        String txId,
        @Schema(description = "链特定 JSON 原文（如 TRON）")
        String rawJson
) {
    /** 工厂方法 */
    public static SignResponse of(String coin, String encoded, String txId) {
        return new SignResponse(coin, encoded, txId, null);
    }

    /** 工厂方法 */
    public static SignResponse of(String coin, String encoded, String txId, String rawJson) {
        return new SignResponse(coin, encoded, txId, rawJson);
    }
}
