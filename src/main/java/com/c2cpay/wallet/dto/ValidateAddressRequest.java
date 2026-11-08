package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "地址校验请求")
public record ValidateAddressRequest(
        @Schema(description = "币种：bitcoin / ethereum / smartchain / tron", example = "tron", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String coin,
        @Schema(description = "待校验地址", example = "TXYZopYRdj2D9XRtbG411XZZ3kM1Vk1HPQ", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String address
) {
}
