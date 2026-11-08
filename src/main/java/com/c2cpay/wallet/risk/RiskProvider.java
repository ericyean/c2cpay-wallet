package com.c2cpay.wallet.risk;

/**
 * Black-U / address risk SPI. Swap noop for a third-party HTTP provider in production.
 */
public interface RiskProvider {

    /** 返回风控服务商名称 */
    String name();

    /** 检查地址/交易风险 */
    RiskCheckResult checkAddress(String chain, String address, String txId);
}
