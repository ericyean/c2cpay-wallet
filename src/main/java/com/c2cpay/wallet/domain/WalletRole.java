package com.c2cpay.wallet.domain;

/**
 * C2C Pay wallet roles aligned with USDT on-chain collection / payout design.
 */
public enum WalletRole {
    /** HD-derived one-order-one-address deposit child */
    DEPOSIT,
    /** Company hot wallet — collection target + payout source */
    HOT,
    /** Black-U / high-risk isolation wallet */
    ISOLATION,
    /** Fee / energy payer (may share hot key) */
    FEE
}
