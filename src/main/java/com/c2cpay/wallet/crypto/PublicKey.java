package com.c2cpay.wallet.crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.util.Arrays;

public final class PublicKey {

    private final byte[] data;

    public PublicKey(byte[] data) {
        if (data == null || !(data.length == 33 || data.length == 65)) {
            throw new IllegalArgumentException("Public key must be 33 or 65 bytes");
        }
        this.data = Arrays.copyOf(data, data.length);
    }

    public static PublicKey fromHex(String hex) {
        return new PublicKey(Hex.decode(hex));
    }

    public byte[] getBytes() {
        return Arrays.copyOf(data, data.length);
    }

    public boolean isCompressed() {
        return data.length == 33;
    }

    public PublicKey compressed() {
        if (isCompressed()) {
            return this;
        }
        return new PublicKey(decodePoint().getEncoded(true));
    }

    public PublicKey uncompressed() {
        if (!isCompressed()) {
            return this;
        }
        return new PublicKey(decodePoint().getEncoded(false));
    }

    private ECPoint decodePoint() {
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        return params.getCurve().decodePoint(data).normalize();
    }
}
