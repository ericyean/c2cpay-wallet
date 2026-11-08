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

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDerivationPath() {
        return derivationPath;
    }

    public void setDerivationPath(String derivationPath) {
        this.derivationPath = derivationPath;
    }

    public int getHdIndex() {
        return hdIndex;
    }

    public void setHdIndex(int hdIndex) {
        this.hdIndex = hdIndex;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public DepositAddressStatus getStatus() {
        return status;
    }

    public void setStatus(DepositAddressStatus status) {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
