# c2cpay-wallet

面向 **C2C Pay / USDT 代收代付** 的多链钱包服务。  
对齐 Trust Wallet Core 的核心能力（HD / 地址派生校验 / 交易签名），并补齐业务侧：**一单一址代收、黑 U 风控、归集、热钱包代付**。

> 路径：与 `wallet-core` 同级 — `/Users/mac/wallet/c2cpay-wallet`  
> 技术栈：**JDK 21 + Spring Boot 3.4**

## 架构对照

| Wallet Core / 业务方案 | 本项目 |
|---|---|
| `CoinEntry` | `coin.CoinEntry` |
| `Coin.cpp` dispatcher | `coin.CoinDispatcher` |
| `registry.json` / `TWCoinType` | `coin.CoinType` + `domain.AssetType` |
| `HDWallet` | `crypto.HdWallet` (BIP39/32/44) |
| `TWAnySigner` | `POST /api/v1/wallet/sign` |
| 收款子地址（HD） | `POST /api/v1/deposit/allocate` |
| 黑 U 检测 | `risk.RiskProvider` SPI（默认 noop） |
| 归集 | `POST /api/v1/collection/sign` |
| 代付下发 | `POST /api/v1/payout` → approve-sign |

```
HTTP / OpenAPI
    ├── Crypto APIs (stateless, wallet-core style)
    └── Business APIs (JPA + AES-GCM custodial keys)
            ↓
      CoinDispatcher → Bitcoin / Ethereum / BSC / TRON
            ↓
      BouncyCastle + bitcoinj + web3j
```

## 资金角色

| 角色 | 说明 |
|---|---|
| DEPOSIT | HD 派生子地址，一单一址收款 |
| HOT | 公司热钱包：正常归集目标 + 代付出口 |
| ISOLATION | 黑 U / 高风险资金隔离地址 |
| FEE | 能量 / 手续费账户（可与热钱包共用） |

## 快速开始

### 本地 MySQL

默认连接：

| 项 | 值 |
|---|---|
| Host | `127.0.0.1:3306` |
| Database | `c2cpay_wallet`（不存在会自动创建） |
| User | `root` |
| Password | `12345678` |

确保本机 MySQL 已启动，可用：

```bash
mysql -uroot -p12345678 -e "SELECT 1"
```

```bash
cd /Users/mac/wallet/c2cpay-wallet

# 开发可先不配 HD；代收接口需要助记词
export WALLET_HD_MNEMONIC="abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
export WALLET_ENC_SECRET="replace-me-in-production-32bytes!!"

mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html  
- OpenAPI: http://localhost:8080/api-docs  
- 单测仍使用内存 H2（`src/test/resources/application.yml`）

## 主要 API

### Crypto（对齐 Wallet Core，无状态）

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/coins` | 已支持币种 |
| POST | `/api/v1/wallet/mnemonic` | 生成助记词 |
| GET | `/api/v1/wallet/mnemonic/validate` | 校验助记词 |
| POST | `/api/v1/wallet/address/validate` | 地址校验 |
| POST | `/api/v1/wallet/address/derive` | 地址派生 |
| POST | `/api/v1/wallet/sign` | 交易签名 |

### 业务（代收 / 归集 / 代付）

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/v1/deposit/allocate` | 按业务单号分配 HD 收款地址（幂等） |
| GET | `/api/v1/deposit/{bizOrderId}` | 查询收款地址 |
| POST | `/api/v1/deposit/mark-deposited` | 确认入账 + 黑 U 检测 |
| POST | `/api/v1/collection/sign` | 子地址归集签名 |
| POST | `/api/v1/payout` | 创建代付单 |
| POST | `/api/v1/payout/{bizOrderId}/approve-sign` | 审核通过并热钱包签名 |

### 代收示例

```bash
curl -s http://localhost:8080/api/v1/deposit/allocate \
  -H 'Content-Type: application/json' \
  -d '{"bizOrderId":"ORD-10001","asset":"USDT_TRC20"}'
```

## 配置要点

```yaml
c2cpay.wallet:
  enc-secret:           # AES-GCM 密钥材料（生产必换）
  hd.mnemonic:          # 代收 HD 助记词（生产走密钥托管）
  hot.private-key-hex:  # 热钱包私钥
  isolation.address:    # 风控隔离地址
  risk.provider: noop   # 替换为第三方黑 U 厂商实现 RiskProvider
```

## 安全模型

- 子地址私钥 AES-256-GCM 加密落库；助记词 / 热钱包密钥仅配置注入，不进业务库明文。
- Crypto 无状态接口允许请求体带私钥，**仅适合受信内网**；生产请关或走网关鉴权。
- 签名结果默认不自动广播，由上层网关 / 运营确认后上链。
- 黑 U：高风险 → 隔离；中风险 → 入账冻结待审；查询失败默认 fail-closed。

## 支持资产（首期）

- TRON / TRX / USDT_TRC20  
- Ethereum / ETH / USDT_ERC20  
- BSC / BNB / USDT_BEP20  
- Bitcoin / BTC  

扩展新链：实现 `CoinEntry` + `@Component`，在 `CoinType` / `AssetType` 登记即可。

## 测试

```bash
mvn test
```

## 与周边仓库关系

| 仓库 | 关系 |
|---|---|
| `wallet-core` | 能力蓝本（C++/Rust 原版） |
| `c2cpay26` | 业务账本 / 商户 API；本服务可作为链上钱包侧能力独立部署 |
| `wallet-core-spring` | 早期无状态原型；本项目在其上升级 JDK21 并补齐托管业务 |
