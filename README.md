# c2cpay-wallet

C2C Pay 多链钱包服务：对齐 Trust Wallet Core 的 HD / 地址 / 签名能力，并提供代收、风控、归集、代付、验签、广播。

| 项 | 说明 |
|---|---|
| 技术栈 | JDK 21 · Spring Boot 3.4 · MySQL 8 · JPA |
| 目录位置 | 与 `wallet-core` 同级 |
| 文档 | http://localhost:8080/swagger-ui.html （中文） |
| OpenAPI | http://localhost:8080/api-docs |

---

## 1. 能力一览

| 能力 | 状态 | 说明 |
|---|---|---|
| 助记词生成 / 校验 | ✅ | BIP39 |
| 地址派生 / 校验 | ✅ | BTC / ETH / BSC / TRON |
| 交易签名 | ✅ | 原生币转账（本地签，不落库） |
| 签名恢复验签 | ✅ | `sign/recover`（SDK recover） |
| 链上广播 | ✅ | 需配置对应 RPC / API |
| HD 一单一址代收 | ✅ | 私钥 AES-GCM 落库 |
| 黑 U 风控 | ✅ | SPI，默认 noop |
| 归集签名 | ✅ | 子地址 → 热钱包 / 隔离钱包 |
| 代付审核签名 | ✅ | 热钱包出款 |
| USDT 合约 transfer | ⏳ | 资产枚举已登记，合约签名待补齐 |

### 支持的链与资产

| 链 | 币种 id | 原生币 | 业务资产 |
|---|---|---|---|
| Bitcoin | `bitcoin` | BTC | `BTC` |
| Ethereum | `ethereum` | ETH | `ETH`、`USDT_ERC20` |
| BNB Smart Chain | `smartchain` | BNB | `BNB`、`USDT_BEP20` |
| TRON | `tron` | TRX | `TRX`、`USDT_TRC20` |

> 加密接口用币种 id；代收/代付业务接口用资产枚举（如 `USDT_TRC20`）。

---

## 2. 架构

```
客户端 / 业务系统
        │
        ▼
 Spring REST + OpenAPI（中文）
        │
   ┌────┴────┐
   │         │
 Crypto    业务托管
 (无状态)   (JPA + MySQL)
   │         │
   ▼         ▼
 CoinDispatcher          代收 / 归集 / 代付 / 风控
   │
   ├── BitcoinCoinEntry
   ├── EthereumCoinEntry / SmartChainCoinEntry
   └── TronCoinEntry
           │
     BouncyCastle · bitcoinj · web3j
           │
   recover 验签 · RPC 广播
```

与 Wallet Core 对应关系：

| Wallet Core | 本项目 |
|---|---|
| `CoinEntry` | `coin.CoinEntry` |
| `Coin.cpp` dispatcher | `coin.CoinDispatcher` |
| `TWCoinType` / registry | `CoinType` + `AssetType` |
| `HDWallet` | `crypto.HdWallet` |
| `TWAnySigner` | `POST /api/v1/wallet/sign` |

### 资金角色

| 角色 | 用途 |
|---|---|
| DEPOSIT | HD 子地址，一单一址收款 |
| HOT | 热钱包：归集目标 + 代付出口 |
| ISOLATION | 黑 U / 高风险资金隔离 |
| FEE | 能量 / 手续费账户（可与热钱包共用） |

---

## 3. 快速开始

### 3.1 环境

- JDK 21+
- Maven 3.9+
- 本地 MySQL 8（默认账号见下）

### 3.2 数据库

```bash
# 初始化 DDL（推荐先执行）
mysql -uroot -p12345678 < sql/schema.sql
```

| 项 | 默认值 |
|---|---|
| Host | `127.0.0.1:3306` |
| Database | `c2cpay_wallet` |
| User | `root` |
| Password | `12345678` |

表结构见 `sql/schema.sql`：

- `hd_index_counter` — HD 下标计数
- `deposit_address` — 代收地址
- `collection_task` — 归集任务
- `payout_order` — 代付订单

> 开发环境 `ddl-auto: update` 也可自动建表；生产建议只跑 DDL。

### 3.3 启动

```bash
cd /Users/mac/wallet/c2cpay-wallet

# 代收 HD（必填才会分配地址）
export WALLET_HD_MNEMONIC="your twelve or twenty four words ..."
export WALLET_ENC_SECRET="replace-me-in-production-32bytes!!"

# 广播用（按需）
export ETH_RPC_URL="https://mainnet.infura.io/v3/<KEY>"
export BSC_RPC_URL="https://bsc-dataseed.binance.org"
export TRON_API_BASE="https://api.trongrid.io"
export TRON_API_KEY=""          # 可选
export BTC_API_BASE="https://blockstream.info/api"

mvn spring-boot:run
```

启动后：

- Swagger：http://localhost:8080/swagger-ui.html
- 健康检查：http://localhost:8080/api/v1/wallet/health

---

## 4. API 说明

### 4.1 钱包基础（Crypto）

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/coins` | 支持币种列表 |
| POST | `/api/v1/wallet/mnemonic` | 生成助记词 |
| GET | `/api/v1/wallet/mnemonic/validate` | 校验助记词 |
| POST | `/api/v1/wallet/address/validate` | 地址校验 |
| POST | `/api/v1/wallet/address/derive` | 地址派生 |
| POST | `/api/v1/wallet/sign` | 交易签名 |
| POST | `/api/v1/wallet/sign/recover` | 签名恢复验签 |
| POST | `/api/v1/wallet/broadcast` | 广播已签名交易 |

推荐链路：

```text
sign → sign/recover（确认付款地址）→ broadcast
```

#### 派生地址示例

```bash
curl -s http://localhost:8080/api/v1/wallet/address/derive \
  -H 'Content-Type: application/json' \
  -d '{
    "coin": "ethereum",
    "mnemonic": "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
  }'
```

#### 签名 → 验签示例（ETH）

```bash
# 1) 签名
SIGNED=$(curl -s http://localhost:8080/api/v1/wallet/sign \
  -H 'Content-Type: application/json' \
  -d '{
    "coin": "ethereum",
    "mnemonic": "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about",
    "to": "0x000000000000000000000000000000000000dEaD",
    "valueWei": "1000000000000000",
    "nonce": 0,
    "gasPriceWei": "20000000000",
    "gasLimit": "21000",
    "chainId": 1
  }')

# 2) recover 验签
curl -s http://localhost:8080/api/v1/wallet/sign/recover \
  -H 'Content-Type: application/json' \
  -d "{
    \"coin\": \"ethereum\",
    \"encoded\": $(echo "$SIGNED" | jq .encoded),
    \"expectedAddress\": \"0x9858EfFD232B4033E47d90003D41EC34EcaEda94\"
  }"
```

#### 广播示例

```bash
# EVM
curl -s http://localhost:8080/api/v1/wallet/broadcast \
  -H 'Content-Type: application/json' \
  -d '{"coin":"ethereum","encoded":"0x..."}'

# TRON（使用 sign 返回的 rawJson）
curl -s http://localhost:8080/api/v1/wallet/broadcast \
  -H 'Content-Type: application/json' \
  -d '{"coin":"tron","rawJson":{...}}'
```

### 4.2 业务：代收 / 归集 / 代付

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/v1/deposit/allocate` | 分配 HD 收款地址（按 bizOrderId 幂等） |
| GET | `/api/v1/deposit/{bizOrderId}` | 查询代收地址 |
| POST | `/api/v1/deposit/mark-deposited` | 确认入账 + 黑 U 检测 |
| GET | `/api/v1/collection/pending` | 待归集任务 |
| POST | `/api/v1/collection/sign` | 归集签名 |
| POST | `/api/v1/payout` | 创建代付单 |
| GET | `/api/v1/payout/{bizOrderId}` | 查询代付单 |
| POST | `/api/v1/payout/{bizOrderId}/approve-sign` | 审核并通过热钱包签名 |

#### 代收示例

```bash
curl -s http://localhost:8080/api/v1/deposit/allocate \
  -H 'Content-Type: application/json' \
  -d '{"bizOrderId":"ORD-10001","asset":"USDT_TRC20"}'
```

业务主流程：

```text
分配地址 → 用户打币 → mark-deposited（风控）
       → 可归集则 collection/sign →（可选）broadcast
       → 代付：payout → approve-sign → broadcast
```

---

## 5. 配置说明

`application.yml` 核心项：

```yaml
c2cpay.wallet:
  enc-secret:                 # AES-GCM 密钥材料（生产必换）
  hd.mnemonic:                # 代收 HD 助记词
  hd.passphrase:              # 可选
  hot.private-key-hex:        # 热钱包私钥
  isolation.address:          # 风控隔离地址
  ethereum.rpc-url:           # ETH 广播 RPC
  smartchain.rpc-url:         # BSC 广播 RPC
  tron.api-base-url:          # TronGrid 等
  tron.api-key:               # 可选
  bitcoin.api-base-url:       # 默认 blockstream.info/api
  risk.provider: noop         # 黑 U 厂商，实现 RiskProvider 替换
  risk.on-provider-error: fail-closed
```

常用环境变量：

| 变量 | 含义 |
|---|---|
| `WALLET_HD_MNEMONIC` | HD 助记词 |
| `WALLET_ENC_SECRET` | 私钥加密密钥 |
| `WALLET_HOT_PRIVATE_KEY` | 热钱包私钥 hex |
| `WALLET_ISOLATION_ADDRESS` | 隔离地址 |
| `ETH_RPC_URL` | 以太坊 RPC |
| `BSC_RPC_URL` | BSC RPC |
| `TRON_API_BASE` / `TRON_API_KEY` | TRON 节点 |
| `BTC_API_BASE` | BTC 广播 API |

---

## 6. 安全说明

- 子地址私钥 **AES-256-GCM** 加密存库；助记词 / 热钱包密钥只走配置或环境变量。
- `/wallet/sign` 等 Crypto 接口可在请求体带私钥，**仅适合受信内网**；生产务必网关鉴权或关闭明文私钥入口。
- 签名与广播解耦：先 recover 确认付款地址，再人工或系统广播。
- 黑 U：高风险 → 隔离；中风险 → 待审；查询失败默认 **fail-closed**。

---

## 7. 工程结构

```
c2cpay-wallet/
├── pom.xml
├── sql/schema.sql                 # MySQL 初始化 DDL
├── src/main/java/com/c2cpay/wallet/
│   ├── api/                       # REST 控制器
│   ├── chain/                     # 各链 CoinEntry / recover / broadcast
│   ├── coin/                      # CoinType、Dispatcher
│   ├── config/                    # 配置与 OpenAPI
│   ├── crypto/                    # HD、AES、Hex、Recover
│   ├── domain/                    # 实体与枚举
│   ├── dto/                       # 请求响应
│   ├── repository/
│   ├── risk/                      # 黑 U SPI
│   └── service/                   # 业务服务
└── src/test/                      # 单测（H2 内存库）
```

扩展新链：

1. `CoinType` / `AssetType` 登记  
2. 实现 `CoinEntry` 并加 `@Component`  
3. 按需补 recover / broadcast  
4. 补测试与 Swagger 字段说明  

---

## 8. 测试

```bash
mvn test
```

单测使用内存 H2（`src/test/resources/application.yml`），不依赖本机 MySQL。

---

## 9. 与周边仓库

| 仓库 | 关系 |
|---|---|
| `wallet-core` | 能力蓝本（C++/Rust） |
| `c2cpay26` | 业务账本 / 商户侧；本服务可独立作为链上钱包组件 |
| `wallet-core-spring` | 早期无状态原型 |

---

## License

内部项目，按团队规范使用。
