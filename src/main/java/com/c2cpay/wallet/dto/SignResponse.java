package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
    public static SignResponse of(String coin, String encoded, String txId) {
        return new SignResponse(coin, encoded, txId, null);
    }

    public static SignResponse of(String coin, String encoded, String txId, String rawJson) {
        return new SignResponse(coin, encoded, txId, rawJson);
    }
}
