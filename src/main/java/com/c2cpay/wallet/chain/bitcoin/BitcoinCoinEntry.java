package com.c2cpay.wallet.chain.bitcoin;

import com.c2cpay.wallet.coin.CoinEntry;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.KeyResolver;
import com.c2cpay.wallet.crypto.PrivateKey;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.ScriptBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Bitcoin native SegWit (P2WPKH) address + demo single-UTXO signer.
 */
@Component
public class BitcoinCoinEntry implements CoinEntry {

    private static final NetworkParameters PARAMS = MainNetParams.get();
    private static final BitcoinNetwork NETWORK = BitcoinNetwork.MAINNET;

    private final KeyResolver keyResolver;

    public BitcoinCoinEntry(KeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }

    @Override
    public CoinType coinType() {
        return CoinType.BITCOIN;
    }

    @Override
    public boolean validateAddress(String address) {
        try {
            Address.fromString(PARAMS, address.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String deriveAddress(PublicKey publicKey) {
        ECKey key = ECKey.fromPublicOnly(publicKey.compressed().getBytes());
        return key.toAddress(ScriptType.P2WPKH, NETWORK).toString();
    }

    @Override
    public SignResponse sign(SignRequest request) {
        PrivateKey privateKey = keyResolver.resolve(coinType(), request);
        ECKey key = ECKey.fromPrivate(new BigInteger(1, privateKey.getBytes()), true);

        if (request.utxos() == null || request.utxos().size() != 1) {
            throw new IllegalArgumentException("This Bitcoin signer currently supports exactly 1 UTXO (demo)");
        }
        if (!StringUtils.hasText(request.toAddress()) || request.amountSatoshi() == null) {
            throw new IllegalArgumentException("Bitcoin sign requires toAddress and amountSatoshi");
        }

        SignRequest.BitcoinUtxo utxo = request.utxos().get(0);
        long fee = request.feeSatoshi() == null ? 1000L : request.feeSatoshi();
        long change = utxo.amountSatoshi() - request.amountSatoshi() - fee;
        if (change < 0) {
            throw new IllegalArgumentException("Insufficient UTXO for amount + fee");
        }

        String changeAddress = StringUtils.hasText(request.changeAddress())
                ? request.changeAddress()
                : deriveAddress(privateKey.getPublicKey(true));

        byte[] prevTxId = reverse(Hex.decode(utxo.txId()));
        List<TxOut> outs = new ArrayList<>();
        outs.add(new TxOut(request.amountSatoshi(), scriptForAddress(request.toAddress())));
        if (change > 546) {
            outs.add(new TxOut(change, scriptForAddress(changeAddress)));
        }

        byte[] scriptCode = scriptCodeP2PKH(key.getPubKeyHash());
        byte[] hashPrevouts = hash256(concat(prevTxId, int32LE(utxo.vout())));
        byte[] hashSequence = hash256(int32LE(0xffffffffL));
        byte[] hashOutputs = hash256(encodeOutputs(outs));

        ByteBuffer preimage = ByteBuffer.allocate(200 + scriptCode.length);
        preimage.order(ByteOrder.LITTLE_ENDIAN);
        preimage.putInt(2);
        preimage.put(hashPrevouts);
        preimage.put(hashSequence);
        preimage.put(prevTxId);
        preimage.putInt(utxo.vout());
        writeVarInt(preimage, scriptCode.length);
        preimage.put(scriptCode);
        preimage.putLong(utxo.amountSatoshi());
        preimage.putInt(0xffffffff);
        preimage.put(hashOutputs);
        preimage.putInt(0);
        preimage.putInt(1);

        byte[] sighash = hash256(trim(preimage));
        TransactionSignature signature = new TransactionSignature(
                key.sign(Sha256Hash.wrap(sighash)),
                org.bitcoinj.core.Transaction.SigHash.ALL,
                false);

        byte[] witSig = signature.encodeToBitcoin();
        byte[] pub = key.getPubKey();

        byte[] noWitness = encodeNoWitness(prevTxId, utxo.vout(), outs);
        byte[] withWitness = encodeWithWitness(prevTxId, utxo.vout(), outs, witSig, pub);
        String txId = Hex.encode(reverse(hash256(noWitness)));
        return SignResponse.of(coinType().getId(), Hex.encode(withWitness), txId);
    }

    private record TxOut(long value, byte[] script) {
    }

    private static byte[] encodeWithWitness(byte[] prevTxId, int vout, List<TxOut> outs, byte[] witSig, byte[] pub) {
        ByteBuffer buf = ByteBuffer.allocate(512);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(2);
        buf.put((byte) 0x00);
        buf.put((byte) 0x01);
        writeVarInt(buf, 1);
        buf.put(prevTxId);
        buf.putInt(vout);
        writeVarInt(buf, 0);
        buf.putInt(0xffffffff);
        buf.put(encodeOutputs(outs));
        writeVarInt(buf, 2);
        writePush(buf, witSig);
        writePush(buf, pub);
        buf.putInt(0);
        return trim(buf);
    }

    private static byte[] encodeNoWitness(byte[] prevTxId, int vout, List<TxOut> outs) {
        ByteBuffer buf = ByteBuffer.allocate(512);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(2);
        writeVarInt(buf, 1);
        buf.put(prevTxId);
        buf.putInt(vout);
        writeVarInt(buf, 0);
        buf.putInt(0xffffffff);
        buf.put(encodeOutputs(outs));
        buf.putInt(0);
        return trim(buf);
    }

    private static byte[] scriptForAddress(String address) {
        Address addr = Address.fromString(PARAMS, address);
        return ScriptBuilder.createOutputScript(addr).program();
    }

    private static byte[] scriptCodeP2PKH(byte[] pubKeyHash) {
        ByteBuffer buf = ByteBuffer.allocate(25);
        buf.put((byte) 0x76);
        buf.put((byte) 0xa9);
        buf.put((byte) 0x14);
        buf.put(pubKeyHash);
        buf.put((byte) 0x88);
        buf.put((byte) 0xac);
        return buf.array();
    }

    private static byte[] encodeOutputs(List<TxOut> outs) {
        ByteBuffer buf = ByteBuffer.allocate(256);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        writeVarInt(buf, outs.size());
        for (TxOut out : outs) {
            buf.putLong(out.value);
            writeVarInt(buf, out.script.length);
            buf.put(out.script);
        }
        return trim(buf);
    }

    private static void writePush(ByteBuffer buf, byte[] data) {
        writeVarInt(buf, data.length);
        buf.put(data);
    }

    private static void writeVarInt(ByteBuffer buf, long value) {
        if (value < 0xfd) {
            buf.put((byte) value);
        } else if (value <= 0xffff) {
            buf.put((byte) 0xfd);
            buf.putShort((short) value);
        } else {
            buf.put((byte) 0xfe);
            buf.putInt((int) value);
        }
    }

    private static byte[] hash256(byte[] data) {
        return Sha256Hash.hashTwice(data);
    }

    private static byte[] reverse(byte[] in) {
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[in.length - 1 - i];
        }
        return out;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    private static byte[] int32LE(long v) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) v).array();
    }

    private static byte[] trim(ByteBuffer buf) {
        byte[] out = new byte[buf.position()];
        System.arraycopy(buf.array(), 0, out, 0, out.length);
        return out;
    }
}
