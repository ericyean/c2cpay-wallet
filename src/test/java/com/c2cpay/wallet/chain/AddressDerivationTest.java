package com.c2cpay.wallet.chain;

import com.c2cpay.wallet.chain.bitcoin.BitcoinCoinEntry;
import com.c2cpay.wallet.chain.ethereum.EthereumCoinEntry;
import com.c2cpay.wallet.chain.tron.TronCoinEntry;
import com.c2cpay.wallet.crypto.HdWallet;
import com.c2cpay.wallet.crypto.KeyResolver;
import com.c2cpay.wallet.crypto.PrivateKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressDerivationTest {

    private static final String MNEMONIC =
            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    private final KeyResolver keyResolver = new KeyResolver();

    @Test
    void ethereumAddressFromKnownMnemonic() {
        PrivateKey key = HdWallet.fromMnemonic(MNEMONIC).getKey("m/44'/60'/0'/0/0");
        EthereumCoinEntry entry = new EthereumCoinEntry(keyResolver);
        String address = entry.deriveAddress(key.getPublicKey(false));
        assertEquals("0x9858EfFD232B4033E47d90003D41EC34EcaEda94", address);
        assertTrue(entry.validateAddress(address));
    }

    @Test
    void bitcoinSegwitAddressFromKnownMnemonic() {
        PrivateKey key = HdWallet.fromMnemonic(MNEMONIC).getKey("m/84'/0'/0'/0/0");
        BitcoinCoinEntry entry = new BitcoinCoinEntry(keyResolver);
        String address = entry.deriveAddress(key.getPublicKey(true));
        assertTrue(address.startsWith("bc1q"));
        assertTrue(entry.validateAddress(address));
    }

    @Test
    void tronAddressFromKnownMnemonic() {
        PrivateKey key = HdWallet.fromMnemonic(MNEMONIC).getKey("m/44'/195'/0'/0/0");
        TronCoinEntry entry = new TronCoinEntry(keyResolver);
        String address = entry.deriveAddress(key.getPublicKey(false));
        assertTrue(address.startsWith("T"));
        assertTrue(entry.validateAddress(address));
    }
}
