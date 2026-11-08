package com.c2cpay.wallet.dto.business;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** CreatePayoutRequest 类 */
@Schema(description = "创建代付单请求")
public record CreatePayoutRequest(
        @Schema(description = "业务单号（幂等键）", example = "PAY-20001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String bizOrderId,
        @Schema(description = "资产类型：USDT_TRC20 / USDT_ERC20 / ...", example = "USDT_TRC20",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String asset,
        @Schema(description = "打款目标地址", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String toAddress,
        @Schema(description = "打款金额", example = "50.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @DecimalMin("0.000001") BigDecimal amount
) {
}
