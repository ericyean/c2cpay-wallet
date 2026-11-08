package com.c2cpay.wallet.risk;

/**
 * Black-U / address risk SPI. Swap noop for a third-party HTTP provider in production.
 */
public interface RiskProvider {

    String name();

    RiskCheckResult checkAddress(String chain, String address, String txId);
}
