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

import java.time.Instant;

@Entity
@Table(
        name = "deposit_address",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_deposit_biz_order", columnNames = {"biz_order_id"}),
                @UniqueConstraint(name = "uk_deposit_coin_path", columnNames = {"coin", "derivation_path"})
        },
        indexes = {
                @Index(name = "idx_deposit_address", columnList = "address"),
                @Index(name = "idx_deposit_status", columnList = "status")
        }
)
/** DepositAddressEntity 类 */
public class DepositAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "biz_order_id", nullable = false, length = 64)
    private String bizOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssetType asset;

    @Column(nullable = false, length = 32)
    private String coin;

    @Column(nullable = false, length = 128)
    private String address;

    @Column(name = "derivation_path", nullable = false, length = 64)
    private String derivationPath;

    @Column(name = "hd_index", nullable = false)
    private int hdIndex;

    /** Encrypted private key (AES-GCM). Null when only mnemonic HD is used server-side. */
    @Column(name = "encrypted_private_key", length = 512)
    private String encryptedPrivateKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DepositAddressStatus status = DepositAddressStatus.ALLOCATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 16)
    private RiskLevel riskLevel = RiskLevel.UNKNOWN;

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

    /** 获取coin */
    public String getCoin() {
        return coin;
    }

    /** 设置coin */
    public void setCoin(String coin) {
        this.coin = coin;
    }

    /** 获取address */
    public String getAddress() {
        return address;
    }

    /** 设置address */
    public void setAddress(String address) {
        this.address = address;
    }

    /** 获取派生路径 */
    public String getDerivationPath() {
        return derivationPath;
    }

    /** 设置派生路径 */
    public void setDerivationPath(String derivationPath) {
        this.derivationPath = derivationPath;
    }

    /** 获取HD 下标 */
    public int getHdIndex() {
        return hdIndex;
    }

    /** 设置HD 下标 */
    public void setHdIndex(int hdIndex) {
        this.hdIndex = hdIndex;
    }

    /** 获取加密私钥 */
    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    /** 设置加密私钥 */
    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    /** 获取status */
    public DepositAddressStatus getStatus() {
        return status;
    }

    /** 设置status */
    public void setStatus(DepositAddressStatus status) {
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

    /** 获取创建时间 */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** 获取更新时间 */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
