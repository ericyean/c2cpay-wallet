package com.c2cpay.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "payout_order",
        uniqueConstraints = @UniqueConstraint(name = "uk_payout_biz", columnNames = "biz_order_id"),
        indexes = @Index(name = "idx_payout_status", columnList = "status")
)
/** PayoutOrderEntity 类 */
public class PayoutOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "biz_order_id", nullable = false, length = 64)
    private String bizOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssetType asset;

    @Column(name = "to_address", nullable = false, length = 128)
    private String toAddress;

    @Column(nullable = false, precision = 36, scale = 18)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PayoutStatus status = PayoutStatus.PENDING_REVIEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 16)
    private RiskLevel riskLevel = RiskLevel.UNKNOWN;

    @Column(name = "tx_id", length = 128)
    private String txId;

    @Column(name = "raw_tx_hex", length = 4096)
    private String rawTxHex;

    @Column(name = "fail_reason", length = 512)
    private String failReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /** 获取id */
    public Long getId() {
        return id;
    }

    /** 获取biz order ID */
    public String getBizOrderId() {
        return bizOrderId;
    }

    /** 设置biz order ID */
    public void setBizOrderId(String bizOrderId) {
        this.bizOrderId = bizOrderId;
    }

    /** 获取asset */
    public AssetType getAsset() {
        return asset;
    }

    /** 设置asset */
    public void setAsset(AssetType asset) {
        this.asset = asset;
    }

    /** 获取转入地址 */
    public String getToAddress() {
        return toAddress;
    }

    /** 设置转入地址 */
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    /** 获取amount */
    public BigDecimal getAmount() {
        return amount;
    }

    /** 设置amount */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /** 获取status */
    public PayoutStatus getStatus() {
        return status;
    }

    /** 设置status */
    public void setStatus(PayoutStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    /** 获取风险等级 */
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    /** 设置风险等级 */
    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
        this.updatedAt = Instant.now();
    }

    /** 获取tx ID */
    public String getTxId() {
        return txId;
    }

    /** 设置tx ID */
    public void setTxId(String txId) {
        this.txId = txId;
    }

    /** 获取已签名原始交易 */
    public String getRawTxHex() {
        return rawTxHex;
    }

    /** 设置已签名原始交易 */
    public void setRawTxHex(String rawTxHex) {
        this.rawTxHex = rawTxHex;
    }

    /** 获取失败原因 */
    public String getFailReason() {
        return failReason;
    }

    /** 设置失败原因 */
    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    /** 获取创建时间 */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** 获取更新时间 */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
