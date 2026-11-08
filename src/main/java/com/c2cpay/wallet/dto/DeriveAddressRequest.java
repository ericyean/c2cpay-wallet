package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "地址派生请求（助记词与私钥二选一）")
public record DeriveAddressRequest(
        @Schema(description = "币种：bitcoin / ethereum / smartchain / tron", example = "tron", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String coin,
        @Schema(description = "BIP39 助记词")
        String mnemonic,
        @Schema(description = "私钥十六进制（可带 0x）")
        String privateKeyHex,
        @Schema(description = "派生路径，不传则用币种默认路径", example = "m/44'/195'/0'/0/0")
        String derivationPath
) {
}
