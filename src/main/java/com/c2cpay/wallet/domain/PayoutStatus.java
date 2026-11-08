package com.c2cpay.wallet.domain;

/** PayoutStatus 类 */
public enum PayoutStatus {
    PENDING_REVIEW,
    APPROVED,
    SIGNING,
    BROADCASTED,
    CONFIRMED,
    FAILED,
    REJECTED
}
