package com.c2cpay.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/** CollectionTaskEntity 类 */
@Entity
@Table(name = "collection_task")
public class CollectionTaskEntity {

    public enum Status {
        PENDING,
        DONE,
        FAILED,
        SKIPPED_RISK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deposit_address_id", nullable = false)
    private Long depositAddressId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssetType asset;

    @Column(name = "from_address", nullable = false, length = 128)
    private String fromAddress;

    @Column(name = "to_address", nullable = false, length = 128)
    private String toAddress;

    @Column(nullable = false, precision = 36, scale = 18)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status = Status.PENDING;

    @Column(name = "tx_id", length = 128)
    private String txId;

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

    /** 获取deposit address ID */
    public Long getDepositAddressId() {
        return depositAddressId;
    }

    /** 设置deposit address ID */
    public void setDepositAddressId(Long depositAddressId) {
        this.depositAddressId = depositAddressId;
    }

    /** 获取asset */
    public AssetType getAsset() {
        return asset;
    }

    /** 设置asset */
    public void setAsset(AssetType asset) {
        this.asset = asset;
    }

    /** 获取转出地址 */
    public String getFromAddress() {
        return fromAddress;
    }

    /** 设置转出地址 */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
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
    public Status getStatus() {
        return status;
    }

    /** 设置status */
    public void setStatus(Status status) {
        this.status = status;
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
