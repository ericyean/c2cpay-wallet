package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** MnemonicResponse 类 */
@Schema(description = "助记词响应")
public record MnemonicResponse(
        @Schema(description = "BIP39 助记词")
        String mnemonic,
        @Schema(description = "单词数量", example = "12")
        int wordCount
) {
}
