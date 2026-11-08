package com.c2cpay.wallet.domain;

public enum PayoutStatus {
    PENDING_REVIEW,
    APPROVED,
    SIGNING,
    BROADCASTED,
    CONFIRMED,
    FAILED,
    REJECTED
}
