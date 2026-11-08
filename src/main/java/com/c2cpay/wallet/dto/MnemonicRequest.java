package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "生成助记词请求")
public record MnemonicRequest(
        @Schema(description = "熵强度（bit）：128=12 词，256=24 词", example = "128")
        @Min(128) @Max(256) Integer strengthBits
) {
    public int strengthOrDefault() {
        return strengthBits == null ? 128 : strengthBits;
    }
}
