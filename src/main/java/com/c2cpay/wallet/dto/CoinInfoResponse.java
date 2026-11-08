package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "币种信息")
public record CoinInfoResponse(
        @Schema(description = "SLIP44 / 内部 coinId")
        int coinId,
        @Schema(description = "币种 id，如 ethereum")
        String id,
        @Schema(description = "符号，如 ETH")
        String symbol,
        @Schema(description = "小数位")
        int decimals,
        @Schema(description = "默认派生路径")
        String defaultDerivationPath
) {
}
