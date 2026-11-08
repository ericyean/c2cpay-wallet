package com.c2cpay.wallet.dto.business;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** AllocateDepositRequest 类 */
@Schema(description = "分配代收地址请求")
public record AllocateDepositRequest(
        @Schema(description = "业务单号（幂等键）", example = "ORD-10001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String bizOrderId,
        @Schema(description = "资产类型：USDT_TRC20 / USDT_ERC20 / USDT_BEP20 / TRX / ETH / BNB / BTC",
                example = "USDT_TRC20", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String asset
) {
}
