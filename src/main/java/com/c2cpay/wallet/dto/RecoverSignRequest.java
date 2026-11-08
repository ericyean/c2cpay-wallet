package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** RecoverSignRequest 类 */
@Schema(description = "签名恢复 / 验签请求")
public record RecoverSignRequest(
        @Schema(description = "币种：bitcoin / ethereum / smartchain / tron",
                example = "ethereum", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String coin,

        @Schema(description = "已签名原始交易 hex（EVM/BTC 必填；TRON 可填 raw_data_hex）",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String encoded,

        @Schema(description = "【TRON】签名接口返回的 rawJson；有则优先用其中的 signature + raw_data_hex")
        String rawJson,

        @Schema(description = "期望付款地址（可选）。传入则与恢复出的地址比对，写入 valid 字段")
        String expectedAddress
) {
}
