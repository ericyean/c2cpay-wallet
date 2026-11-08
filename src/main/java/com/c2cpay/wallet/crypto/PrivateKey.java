package com.c2cpay.wallet.crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.Arrays;

public final class PrivateKey {

    private static final X9ECParameters CURVE_PARAMS = SECNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(),
            CURVE_PARAMS.getG(),
            CURVE_PARAMS.getN(),
            CURVE_PARAMS.getH());

    private final byte[] key;

    public PrivateKey(byte[] key) {
        if (key == null || key.length != 32) {
            throw new IllegalArgumentException("Private key must be 32 bytes");
        }
        BigInteger d = new BigInteger(1, key);
        if (d.signum() <= 0 || d.compareTo(CURVE.getN()) >= 0) {
            throw new IllegalArgumentException("Private key out of secp256k1 range");
        }
        this.key = Arrays.copyOf(key, key.length);
    }

    public static PrivateKey fromHex(String hex) {
        return new PrivateKey(Hex.decode(hex));
    }

    public byte[] getBytes() {
        return Arrays.copyOf(key, key.length);
    }

    public PublicKey getPublicKey(boolean compressed) {
        BigInteger d = new BigInteger(1, key);
        ECPoint q = CURVE.getG().multiply(d).normalize();
        return new PublicKey(q.getEncoded(compressed));
    }

    /**
     * Sign a 32-byte hash; returns (r||s) 64 bytes, low-S normalized.
     */
    public byte[] sign(byte[] hash32) {
        if (hash32.length != 32) {
            throw new IllegalArgumentException("Hash must be 32 bytes");
        }
        ECDSASigner signer = new ECDSASigner();
        BigInteger d = new BigInteger(1, key);
        signer.init(true, new ECPrivateKeyParameters(d, CURVE));
        BigInteger[] rs = signer.generateSignature(hash32);
        BigInteger r = rs[0];
        BigInteger s = rs[1];
        BigInteger halfN = CURVE.getN().shiftRight(1);
        if (s.compareTo(halfN) > 0) {
            s = CURVE.getN().subtract(s);
        }
        return concat32(r, s);
    }

    private static byte[] concat32(BigInteger r, BigInteger s) {
        byte[] out = new byte[64];
        System.arraycopy(toFixed32(r), 0, out, 0, 32);
        System.arraycopy(toFixed32(s), 0, out, 32, 32);
        return out;
    }

    private static byte[] toFixed32(BigInteger value) {
        byte[] raw = value.toByteArray();
        byte[] out = new byte[32];
        if (raw.length <= 32) {
            System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
        } else {
            System.arraycopy(raw, raw.length - 32, out, 0, 32);
        }
        return out;
    }
}
