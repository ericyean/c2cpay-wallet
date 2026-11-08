package com.c2cpay.wallet.crypto;

import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * BIP39 + BIP32/BIP44 HD wallet helper, aligned with Trust Wallet Core HDWallet usage.
 */
public final class HdWallet {

    private final List<String> mnemonic;
    private final byte[] seed;

    private HdWallet(List<String> mnemonic, byte[] seed) {
        this.mnemonic = List.copyOf(mnemonic);
        this.seed = Arrays.copyOf(seed, seed.length);
    }

    public static HdWallet create(int strengthBits) {
        if (strengthBits != 128 && strengthBits != 256) {
            throw new IllegalArgumentException("strengthBits must be 128 or 256");
        }
        byte[] entropy = new byte[strengthBits / 8];
        new SecureRandom().nextBytes(entropy);
        List<String> words = MnemonicCode.INSTANCE.toMnemonic(entropy);
        DeterministicSeed seed = DeterministicSeed.ofMnemonic(words, "");
        return new HdWallet(words, seed.getSeedBytes());
    }

    public static HdWallet fromMnemonic(String mnemonicPhrase) {
        List<String> words = Arrays.asList(mnemonicPhrase.trim().split("\\s+"));
        try {
            MnemonicCode.INSTANCE.check(words);
            DeterministicSeed seed = DeterministicSeed.ofMnemonic(words, "");
            return new HdWallet(words, seed.getSeedBytes());
        } catch (MnemonicException e) {
            throw new IllegalArgumentException("Invalid mnemonic: " + e.getMessage(), e);
        }
    }

    public String mnemonic() {
        return String.join(" ", mnemonic);
    }

    public List<String> words() {
        return mnemonic;
    }

    public PrivateKey getKey(String derivationPath) {
        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(seed);
        // bitcoinj 0.17 uses 'H' for hardened (e.g. m/44H/60H/0H/0/0)
        HDPath path = HDPath.parsePath(toBitcoinjPath(derivationPath));
        DeterministicKey key = master;
        for (ChildNumber child : path) {
            key = HDKeyDerivation.deriveChildKey(key, child);
        }
        return new PrivateKey(key.getPrivKeyBytes());
    }

    public PublicKey getPublicKey(String derivationPath, boolean compressed) {
        return getKey(derivationPath).getPublicKey(compressed);
    }

    public static boolean isValidMnemonic(String mnemonicPhrase) {
        try {
            List<String> words = Arrays.asList(mnemonicPhrase.trim().split("\\s+"));
            MnemonicCode.INSTANCE.check(words);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static String toBitcoinjPath(String path) {
        String p = path.trim().replace('\'', 'H');
        if (!(p.startsWith("m/") || p.startsWith("M/"))) {
            p = "m/" + p;
        }
        return p;
    }

    public String seedFingerprint() {
        return Sha256Hash.of(seed).toString().substring(0, 8);
    }
}
