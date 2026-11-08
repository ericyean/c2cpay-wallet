package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** BroadcastRequest 类 */
@Schema(description = "广播已签名交易请求")
public record BroadcastRequest(
        @Schema(description = "币种：bitcoin / ethereum / smartchain / tron",
                example = "ethereum", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String coin,

        @Schema(description = "已签名原始交易 hex（EVM/BTC）；TRON 也可传 raw_data_hex，但建议同时带 rawJson")
        String encoded,

        @Schema(description = "【TRON】签名接口返回的完整 rawJson（含 signature），广播优先使用")
        String rawJson
) {
}
