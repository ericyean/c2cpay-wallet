package com.c2cpay.wallet.coin;

import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;

/**
 * Mirrors Trust Wallet Core {@code CoinEntry}: validate / derive / sign per chain.
 */
public interface CoinEntry {

    CoinType coinType();

    boolean validateAddress(String address);

    String deriveAddress(PublicKey publicKey);

    SignResponse sign(SignRequest request);
}
