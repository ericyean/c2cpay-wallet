package com.c2cpay.wallet.dto.business;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "确认入账请求")
public record MarkDepositRequest(
        @Schema(description = "业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String bizOrderId,
        @Schema(description = "链上交易哈希", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String txId,
        @Schema(description = "到账金额（展示用，可选）", example = "100.5")
        String amount
) {
}
