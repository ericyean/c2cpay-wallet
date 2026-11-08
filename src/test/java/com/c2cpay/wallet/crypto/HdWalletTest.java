package com.c2cpay.wallet.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HdWalletTest {

    private static final String MNEMONIC =
            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    @Test
    void validatesKnownMnemonic() {
        assertTrue(HdWallet.isValidMnemonic(MNEMONIC));
    }

    @Test
    void derivesEthereumKey() {
        HdWallet wallet = HdWallet.fromMnemonic(MNEMONIC);
        PrivateKey key = wallet.getKey("m/44'/60'/0'/0/0");
        assertEquals(32, key.getBytes().length);
        assertEquals(65, key.getPublicKey(false).getBytes().length);
    }
}
