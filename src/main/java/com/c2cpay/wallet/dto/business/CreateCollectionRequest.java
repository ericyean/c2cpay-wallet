package com.c2cpay.wallet.dto.business;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "创建归集任务请求")
public record CreateCollectionRequest(
        @Schema(description = "代收地址表主键 ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Long depositAddressId,
        @Schema(description = "归集金额", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @DecimalMin("0.000001") BigDecimal amount
) {
}
