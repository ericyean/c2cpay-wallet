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

    public Long getId() {
        return id;
    }

    public String getBizOrderId() {
        return bizOrderId;
    }

    public void setBizOrderId(String bizOrderId) {
        this.bizOrderId = bizOrderId;
    }

    public AssetType getAsset() {
        return asset;
    }

    public void setAsset(AssetType asset) {
        this.asset = asset;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
        this.updatedAt = Instant.now();
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getRawTxHex() {
        return rawTxHex;
    }

    public void setRawTxHex(String rawTxHex) {
        this.rawTxHex = rawTxHex;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
