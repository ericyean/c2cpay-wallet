package com.c2cpay.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.math.BigInteger;
import java.util.List;

/** SignRequest 类 */
@Schema(description = "统一交易签名请求（按币种填写对应字段）")
public record SignRequest(
        @Schema(description = "币种：bitcoin / ethereum / smartchain / tron", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String coin,
        @Schema(description = "私钥十六进制（与助记词二选一）")
        String privateKeyHex,
        @Schema(description = "BIP39 助记词（与私钥二选一）")
        String mnemonic,
        @Schema(description = "派生路径")
        String derivationPath,

        @Schema(description = "【EVM】收款地址")
        String to,
        @Schema(description = "【EVM】转账金额（wei 字符串）")
        String valueWei,
        @Schema(description = "【EVM】合约调用 data（hex）")
        String dataHex,
        @Schema(description = "【EVM】nonce")
        Long nonce,
        @Schema(description = "【EVM】gasPrice（wei）")
        String gasPriceWei,
        @Schema(description = "【EVM】gasLimit")
        String gasLimit,
        @Schema(description = "【EVM】chainId，以太坊主网 1，BSC 56")
        Long chainId,
        @Schema(description = "【EVM EIP-1559】maxFeePerGas（wei）")
        String maxFeePerGasWei,
        @Schema(description = "【EVM EIP-1559】maxPriorityFeePerGas（wei）")
        String maxPriorityFeePerGasWei,

        @Schema(description = "【BTC】UTXO 列表（当前演示仅支持 1 个）")
        List<BitcoinUtxo> utxos,
        @Schema(description = "【BTC】收款地址")
        String toAddress,
        @Schema(description = "【BTC】转账金额（聪）")
        Long amountSatoshi,
        @Schema(description = "【BTC】手续费（聪）")
        Long feeSatoshi,
        @Schema(description = "【BTC】找零地址，不传则回自身")
        String changeAddress,

        @Schema(description = "【TRON】收款地址")
        String toTronAddress,
        @Schema(description = "【TRON】转账金额（sun，1 TRX = 1e6 sun）")
        Long amountSun,
        @Schema(description = "【TRON】交易时间戳（毫秒）")
        Long timestampMs,
        @Schema(description = "【TRON】过期时间（毫秒）")
        Long expirationMs,
        @Schema(description = "【TRON】ref_block_bytes（hex）")
        String refBlockBytesHex,
        @Schema(description = "【TRON】ref_block_hash（hex）")
        String refBlockHashHex
) {
    @Schema(description = "比特币 UTXO")
    public record BitcoinUtxo(
            @Schema(description = "前序交易哈希", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank String txId,
            @Schema(description = "输出下标 vout")
            int vout,
            @Schema(description = "UTXO 金额（聪）")
            long amountSatoshi,
            @Schema(description = "scriptPubKey（hex，可选）")
            String scriptPubKeyHex
    ) {
    }

    /** 解析 valueWei，空则返回 0 */
    public BigInteger valueWeiOrZero() {
        return valueWei == null || valueWei.isBlank() ? BigInteger.ZERO : new BigInteger(valueWei);
    }
}
