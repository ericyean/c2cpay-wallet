package com.c2cpay.wallet.risk;

import com.c2cpay.wallet.domain.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "风控检测结果")
public record RiskCheckResult(
        @Schema(description = "风险等级：LOW/MEDIUM/HIGH/UNKNOWN")
        RiskLevel level,
        @Schema(description = "风控服务商名称")
        String provider,
        @Schema(description = "原因说明")
        String reason,
        @Schema(description = "是否允许入账")
        boolean allowCredit,
        @Schema(description = "是否允许出款")
        boolean allowPayout
) {
    public static RiskCheckResult low(String provider) {
        return new RiskCheckResult(RiskLevel.LOW, provider, "ok", true, true);
    }

    public static RiskCheckResult medium(String provider, String reason) {
        return new RiskCheckResult(RiskLevel.MEDIUM, provider, reason, false, false);
    }

    public static RiskCheckResult high(String provider, String reason) {
        return new RiskCheckResult(RiskLevel.HIGH, provider, reason, false, false);
    }

    public static RiskCheckResult unknown(String provider, String reason, boolean failClosed) {
        return new RiskCheckResult(
                RiskLevel.UNKNOWN,
                provider,
                reason,
                !failClosed,
                !failClosed
        );
    }
}
