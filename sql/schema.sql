-- c2cpay-wallet MySQL 初始化 DDL
-- MySQL 8.0+ / utf8mb4

CREATE DATABASE IF NOT EXISTS `c2cpay_wallet`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `c2cpay_wallet`;

-- ---------------------------------------------------------------------------
-- HD 派生索引计数器（按币种分配下一 index）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hd_index_counter` (
  `coin`       VARCHAR(32)  NOT NULL COMMENT '币种 id，如 tron/ethereum',
  `next_index` INT          NOT NULL DEFAULT 0 COMMENT '下一个可分配的 HD index',
  `version`    BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`coin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='HD 派生索引计数器';

-- ---------------------------------------------------------------------------
-- 代收地址（一单一址）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `deposit_address` (
  `id`                     BIGINT       NOT NULL AUTO_INCREMENT,
  `biz_order_id`           VARCHAR(64)  NOT NULL COMMENT '业务单号（幂等键）',
  `asset`                  VARCHAR(32)  NOT NULL COMMENT '资产：USDT_TRC20/USDT_ERC20/...',
  `coin`                   VARCHAR(32)  NOT NULL COMMENT '链：tron/ethereum/smartchain/bitcoin',
  `address`                VARCHAR(128) NOT NULL COMMENT '链上收款地址',
  `derivation_path`        VARCHAR(64)  NOT NULL COMMENT 'BIP44 派生路径',
  `hd_index`               INT          NOT NULL COMMENT 'HD 地址下标',
  `encrypted_private_key`  VARCHAR(512) DEFAULT NULL COMMENT 'AES-GCM 加密私钥',
  `status`                 VARCHAR(32)  NOT NULL DEFAULT 'ALLOCATED'
                           COMMENT 'ALLOCATED/DEPOSITED/COLLECTIBLE/COLLECTED/ISOLATED/DISABLED',
  `risk_level`             VARCHAR(16)  DEFAULT 'UNKNOWN' COMMENT 'LOW/MEDIUM/HIGH/UNKNOWN',
  `created_at`             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_deposit_biz_order` (`biz_order_id`),
  UNIQUE KEY `uk_deposit_coin_path` (`coin`, `derivation_path`),
  KEY `idx_deposit_address` (`address`),
  KEY `idx_deposit_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='代收 HD 子地址';

-- ---------------------------------------------------------------------------
-- 归集任务
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `collection_task` (
  `id`                 BIGINT         NOT NULL AUTO_INCREMENT,
  `deposit_address_id` BIGINT         NOT NULL COMMENT '关联 deposit_address.id',
  `asset`              VARCHAR(32)    NOT NULL,
  `from_address`       VARCHAR(128)   NOT NULL COMMENT '子地址',
  `to_address`         VARCHAR(128)   NOT NULL COMMENT '热钱包或隔离钱包',
  `amount`             DECIMAL(36,18) NOT NULL,
  `status`             VARCHAR(32)    NOT NULL DEFAULT 'PENDING'
                       COMMENT 'PENDING/DONE/FAILED/SKIPPED_RISK',
  `tx_id`              VARCHAR(128)   DEFAULT NULL,
  `fail_reason`        VARCHAR(512)   DEFAULT NULL,
  `created_at`         DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`         DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_collection_status` (`status`),
  KEY `idx_collection_deposit` (`deposit_address_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='子地址归集任务';

-- ---------------------------------------------------------------------------
-- 代付 / 提现单
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `payout_order` (
  `id`           BIGINT         NOT NULL AUTO_INCREMENT,
  `biz_order_id` VARCHAR(64)    NOT NULL COMMENT '业务单号（幂等键）',
  `asset`        VARCHAR(32)    NOT NULL,
  `to_address`   VARCHAR(128)   NOT NULL COMMENT '打款目标地址',
  `amount`       DECIMAL(36,18) NOT NULL,
  `status`       VARCHAR(32)    NOT NULL DEFAULT 'PENDING_REVIEW'
                 COMMENT 'PENDING_REVIEW/APPROVED/SIGNING/BROADCASTED/CONFIRMED/FAILED/REJECTED',
  `risk_level`   VARCHAR(16)    DEFAULT 'UNKNOWN',
  `tx_id`        VARCHAR(128)   DEFAULT NULL,
  `raw_tx_hex`   VARCHAR(4096)  DEFAULT NULL COMMENT '已签名原始交易',
  `fail_reason`  VARCHAR(512)   DEFAULT NULL,
  `created_at`   DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`   DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payout_biz` (`biz_order_id`),
  KEY `idx_payout_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='热钱包代付订单';
