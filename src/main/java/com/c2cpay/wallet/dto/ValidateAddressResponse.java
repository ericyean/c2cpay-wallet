package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "地址校验响应")
public record ValidateAddressResponse(
        @Schema(description = "币种 id")
        String coin,
        @Schema(description = "地址")
        String address,
        @Schema(description = "是否合法")
        boolean valid
) {
}
