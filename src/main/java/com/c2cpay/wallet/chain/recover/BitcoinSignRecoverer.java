package com.c2cpay.wallet.chain.recover;

import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.RecoverSignRequest;
import com.c2cpay.wallet.dto.RecoverSignResponse;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析本服务产出的单输入 P2WPKH 已签名交易：从 witness 取出公钥推导地址。
 * （BTC 见证签名通常不含 recovery id，因此用公钥推导地址，并校验签名 DER 可解析。）
 */
@Component
public class BitcoinSignRecoverer {

    /** 从已签名交易恢复付款地址并验签 */
    public RecoverSignResponse recover(RecoverSignRequest request) {
        try {
            byte[] raw = Hex.decode(request.encoded());
            ParsedTx tx = parseSegwit(raw);
            if (tx.witnessStack.size() < 2) {
                throw new IllegalArgumentException("非 P2WPKH 见证结构，无法提取公钥");
            }
            byte[] derSig = tx.witnessStack.get(0);
            byte[] pubBytes = tx.witnessStack.get(1);
            // 校验 DER 签名可解析
            TransactionSignature.decodeFromBitcoin(derSig, true, true);

            ECKey key = ECKey.fromPublicOnly(pubBytes);
            String address = key.toAddress(ScriptType.P2WPKH, BitcoinNetwork.MAINNET).toString();
            String txId = Hex.encode(reverse(Sha256Hash.hashTwice(tx.noWitnessSerialization)));
            boolean valid = !StringUtils.hasText(request.expectedAddress())
                    || address.equals(request.expectedAddress().trim());
            return new RecoverSignResponse(
                    CoinType.BITCOIN.getId(),
                    valid,
                    address,
                    txId,
                    Hex.encode(new PublicKey(pubBytes).getBytes()),
                    valid ? "BTC 从 witness 公钥推导地址成功" : "恢复地址与期望地址不一致");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("BTC 签名恢复失败: " + e.getMessage(), e);
        }
    }

    /** 解析segwit */
    private static ParsedTx parseSegwit(byte[] raw) {
        ByteBuffer buf = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        int version = buf.getInt();
        int marker = buf.get() & 0xFF;
        int flag = buf.get() & 0xFF;
        if (marker != 0x00 || flag != 0x01) {
            throw new IllegalArgumentException("仅支持 segwit 交易 (marker=0 flag=1)");
        }
        long inCount = readVarInt(buf);
        if (inCount != 1) {
            throw new IllegalArgumentException("当前仅支持单输入交易 recover");
        }
        byte[] prevTx = new byte[32];
        buf.get(prevTx);
        int vout = buf.getInt();
        long scriptLen = readVarInt(buf);
        byte[] script = new byte[(int) scriptLen];
        buf.get(script);
        int sequence = buf.getInt();

        long outCount = readVarInt(buf);
        List<byte[]> outs = new ArrayList<>();
        ByteBuffer outBuf = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);
      /** 写入var int */
        writeVarInt(outBuf, outCount);
        for (int i = 0; i < outCount; i++) {
            long value = buf.getLong();
            long pkLen = readVarInt(buf);
            byte[] pk = new byte[(int) pkLen];
            buf.get(pk);
            outBuf.putLong(value);
          /** 写入var int */
            writeVarInt(outBuf, pkLen);
            outBuf.put(pk);
            outs.add(pk);
        }
        byte[] outputs = trim(outBuf);

        long stackCount = readVarInt(buf);
        List<byte[]> stack = new ArrayList<>();
        for (int i = 0; i < stackCount; i++) {
            long n = readVarInt(buf);
            byte[] item = new byte[(int) n];
            buf.get(item);
            stack.add(item);
        }
        int lockTime = buf.getInt();

        // 重建无见证序列化用于 txid
        ByteBuffer nw = ByteBuffer.allocate(raw.length).order(ByteOrder.LITTLE_ENDIAN);
        nw.putInt(version);
      /** 写入var int */
        writeVarInt(nw, 1);
        nw.put(prevTx);
        nw.putInt(vout);
      /** 写入var int */
        writeVarInt(nw, script.length);
        nw.put(script);
        nw.putInt(sequence);
        nw.put(outputs);
        nw.putInt(lockTime);

        return new ParsedTx(trim(nw), stack);
    }

    /** parsed tx */
    private record ParsedTx(byte[] noWitnessSerialization, List<byte[]> witnessStack) {
    }

    /** 读取var int */
    private static long readVarInt(ByteBuffer buf) {
        int first = buf.get() & 0xFF;
        if (first < 0xfd) {
            return first;
        }
        if (first == 0xfd) {
            return buf.getShort() & 0xFFFF;
        }
        if (first == 0xfe) {
            return buf.getInt() & 0xFFFFFFFFL;
        }
        return buf.getLong();
    }

    /** 写入var int */
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

    /** 裁剪 */
    private static byte[] trim(ByteBuffer buf) {
        byte[] out = new byte[buf.position()];
        System.arraycopy(buf.array(), 0, out, 0, out.length);
        return out;
    }

    /** 反转 */
    private static byte[] reverse(byte[] in) {
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[in.length - 1 - i];
        }
        return out;
    }
}
