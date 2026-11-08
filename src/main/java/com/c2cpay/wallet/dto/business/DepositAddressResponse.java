package com.c2cpay.wallet.dto.business;

import com.c2cpay.wallet.domain.DepositAddressEntity;
import com.c2cpay.wallet.risk.RiskCheckResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "代收地址信息")
public record DepositAddressResponse(
        @Schema(description = "主键 ID")
        Long id,
        @Schema(description = "业务单号")
        String bizOrderId,
        @Schema(description = "资产类型")
        String asset,
        @Schema(description = "链 / 币种 id")
        String coin,
        @Schema(description = "收款地址")
        String address,
        @Schema(description = "HD 派生路径")
        String derivationPath,
        @Schema(description = "HD 下标")
        int hdIndex,
        @Schema(description = "状态：ALLOCATED/DEPOSITED/COLLECTIBLE/COLLECTED/ISOLATED/DISABLED")
        String status,
        @Schema(description = "风险等级：LOW/MEDIUM/HIGH/UNKNOWN")
        String riskLevel,
        @Schema(description = "创建时间")
        Instant createdAt,
        @Schema(description = "风控检测结果（确认入账时返回）")
        RiskCheckResult risk
) {
    public static DepositAddressResponse from(DepositAddressEntity e) {
        return from(e, null);
    }

    public static DepositAddressResponse from(DepositAddressEntity e, RiskCheckResult risk) {
        return new DepositAddressResponse(
                e.getId(),
                e.getBizOrderId(),
                e.getAsset().name(),
                e.getCoin(),
                e.getAddress(),
                e.getDerivationPath(),
                e.getHdIndex(),
                e.getStatus().name(),
                e.getRiskLevel() == null ? null : e.getRiskLevel().name(),
                e.getCreatedAt(),
                risk
        );
    }
}
