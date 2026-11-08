package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "地址派生响应")
public record DeriveAddressResponse(
        @Schema(description = "币种 id")
        String coin,
        @Schema(description = "派生出的地址")
        String address,
        @Schema(description = "公钥十六进制")
        String publicKeyHex,
        @Schema(description = "实际使用的派生路径")
        String derivationPath
) {
}
