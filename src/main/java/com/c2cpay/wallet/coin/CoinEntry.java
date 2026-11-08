package com.c2cpay.wallet.coin;

import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;

/**
 * Mirrors Trust Wallet Core {@code CoinEntry}: validate / derive / sign per chain.
 */
public interface CoinEntry {

    /** coin type */
    CoinType coinType();

    /** 校验地址 */
    boolean validateAddress(String address);

    /** 派生地址 */
    String deriveAddress(PublicKey publicKey);

    /** 按币种签名交易 */
    SignResponse sign(SignRequest request);
}
