package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** RecoverSignResponse 类 */
@Schema(description = "签名恢复 / 验签响应")
public record RecoverSignResponse(
        @Schema(description = "币种 id")
        String coin,
        @Schema(description = "是否通过（未传 expectedAddress 时，只要能成功 recover 即为 true）")
        boolean valid,
        @Schema(description = "从签名恢复出的付款地址")
        String recoveredAddress,
        @Schema(description = "交易哈希 / txId")
        String txId,
        @Schema(description = "恢复出的公钥 hex（未压缩或压缩，视链而定）")
        String publicKeyHex,
        @Schema(description = "说明")
        String message
) {
}
