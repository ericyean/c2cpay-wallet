package com.c2cpay.wallet.dto.business;

import com.c2cpay.wallet.domain.CollectionTaskEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "归集任务信息")
public record CollectionTaskResponse(
        @Schema(description = "任务 ID")
        Long id,
        @Schema(description = "代收地址 ID")
        Long depositAddressId,
        @Schema(description = "资产类型")
        String asset,
        @Schema(description = "转出地址（子地址）")
        String fromAddress,
        @Schema(description = "转入地址（热钱包/隔离钱包）")
        String toAddress,
        @Schema(description = "金额")
        BigDecimal amount,
        @Schema(description = "状态：PENDING/DONE/FAILED/SKIPPED_RISK")
        String status,
        @Schema(description = "交易哈希")
        String txId,
        @Schema(description = "失败原因")
        String failReason,
        @Schema(description = "创建时间")
        Instant createdAt
) {
    public static CollectionTaskResponse from(CollectionTaskEntity e) {
        return new CollectionTaskResponse(
                e.getId(),
                e.getDepositAddressId(),
                e.getAsset().name(),
                e.getFromAddress(),
                e.getToAddress(),
                e.getAmount(),
                e.getStatus().name(),
                e.getTxId(),
                e.getFailReason(),
                e.getCreatedAt()
        );
    }
}
