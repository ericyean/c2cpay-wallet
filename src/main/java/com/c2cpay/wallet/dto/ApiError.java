package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "统一错误响应")
public record ApiError(
        @Schema(description = "时间")
        Instant timestamp,
        @Schema(description = "HTTP 状态码")
        int status,
        @Schema(description = "错误短语")
        String error,
        @Schema(description = "错误详情")
        String message,
        @Schema(description = "请求路径")
        String path
) {
}
