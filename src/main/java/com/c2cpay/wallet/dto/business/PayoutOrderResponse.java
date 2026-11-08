package com.c2cpay.wallet.dto.business;

import com.c2cpay.wallet.domain.PayoutOrderEntity;
import com.c2cpay.wallet.risk.RiskCheckResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/** PayoutOrderResponse 类 */
@Schema(description = "代付订单信息")
public record PayoutOrderResponse(
        @Schema(description = "订单 ID")
        Long id,
        @Schema(description = "业务单号")
        String bizOrderId,
        @Schema(description = "资产类型")
        String asset,
        @Schema(description = "打款目标地址")
        String toAddress,
        @Schema(description = "金额")
        BigDecimal amount,
        @Schema(description = "状态：PENDING_REVIEW/APPROVED/SIGNING/BROADCASTED/CONFIRMED/FAILED/REJECTED")
        String status,
        @Schema(description = "风险等级")
        String riskLevel,
        @Schema(description = "交易哈希")
        String txId,
        @Schema(description = "已签名原始交易 hex")
        String rawTxHex,
        @Schema(description = "失败原因")
        String failReason,
        @Schema(description = "创建时间")
        Instant createdAt,
        @Schema(description = "风控检测结果（创建时返回）")
        RiskCheckResult risk
) {
    /** 从参数构造 */
    public static PayoutOrderResponse from(PayoutOrderEntity e) {
        return from(e, null);
    }

    /** 从参数构造 */
    public static PayoutOrderResponse from(PayoutOrderEntity e, RiskCheckResult risk) {
        return new PayoutOrderResponse(
                e.getId(),
                e.getBizOrderId(),
                e.getAsset().name(),
                e.getToAddress(),
                e.getAmount(),
                e.getStatus().name(),
                e.getRiskLevel() == null ? null : e.getRiskLevel().name(),
                e.getTxId(),
                e.getRawTxHex(),
                e.getFailReason(),
                e.getCreatedAt(),
                risk
        );
    }
}
