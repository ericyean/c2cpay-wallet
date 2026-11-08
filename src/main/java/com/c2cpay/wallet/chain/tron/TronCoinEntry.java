package com.c2cpay.wallet.chain.tron;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.c2cpay.wallet.coin.CoinEntry;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.crypto.Hashing;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.KeyResolver;
import com.c2cpay.wallet.crypto.PrivateKey;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * TRON address + TransferContract signing (minimal protobuf encoding).
 */
@Component
public class TronCoinEntry implements CoinEntry {

    private final KeyResolver keyResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 构造 TronCoinEntry */
    public TronCoinEntry(KeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }

    /** coin type */
    @Override
    public CoinType coinType() {
        return CoinType.TRON;
    }

    /** 校验地址 */
    @Override
    public boolean validateAddress(String address) {
        return TronAddress.isValid(address);
    }

    /** 派生地址 */
    @Override
    public String deriveAddress(PublicKey publicKey) {
        return TronAddress.fromPublicKey(publicKey);
    }

    /** 按币种签名交易 */
    @Override
    public SignResponse sign(SignRequest request) {
        PrivateKey privateKey = keyResolver.resolve(coinType(), request);
        if (!StringUtils.hasText(request.toTronAddress()) || request.amountSun() == null) {
            throw new IllegalArgumentException("TRON sign requires toTronAddress and amountSun");
        }

        String owner = deriveAddress(privateKey.getPublicKey(false));
        long now = request.timestampMs() == null ? System.currentTimeMillis() : request.timestampMs();
        long expiration = request.expirationMs() == null ? now + 60_000L : request.expirationMs();

        byte[] ownerBytes = TronAddress.toBytes(owner);
        byte[] toBytes = TronAddress.toBytes(request.toTronAddress());

        byte[] contractParameter = encodeTransferContract(ownerBytes, toBytes, request.amountSun());
        byte[] rawData = encodeTransactionRaw(
                contractParameter,
                now,
                expiration,
                StringUtils.hasText(request.refBlockBytesHex())
                        ? Hex.decode(request.refBlockBytesHex())
                        : new byte[]{0x00, 0x00},
                StringUtils.hasText(request.refBlockHashHex())
                        ? Hex.decode(request.refBlockHashHex())
                        : new byte[8]
        );

        byte[] hash = Hashing.sha256(rawData);
        byte[] signature = signRecoverable(privateKey, hash);

        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("txID", Hex.encode(hash));
            root.put("raw_data_hex", Hex.encode(rawData));
            root.putArray("signature").add(Hex.encode(signature));
            ObjectNode rawDataJson = root.putObject("raw_data");
            rawDataJson.put("timestamp", now);
            rawDataJson.put("expiration", expiration);
            rawDataJson.put("contract_type", "TransferContract");
            rawDataJson.put("owner_address", owner);
            rawDataJson.put("to_address", request.toTronAddress());
            rawDataJson.put("amount", request.amountSun());

            return SignResponse.of(
                    coinType().getId(),
                    Hex.encode(rawData),
                    Hex.encode(hash),
                    objectMapper.writeValueAsString(root));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode TRON signing output", e);
        }
    }

    /** 编码 TRON TransferContract */
    private static byte[] encodeTransferContract(byte[] owner, byte[] to, long amount) {
        ByteArrayOutputStream value = new ByteArrayOutputStream();
      /** 写入 protobuf bytes 字段 */
        writeBytes(value, 1, owner);
      /** 写入 protobuf bytes 字段 */
        writeBytes(value, 2, to);
      /** 写入 protobuf varint 字段 */
        writeVarintField(value, 3, amount);

        ByteArrayOutputStream any = new ByteArrayOutputStream();
      /** 写入 protobuf 字符串字段 */
        writeString(any, 1, "type.googleapis.com/protocol.TransferContract");
      /** 写入 protobuf bytes 字段 */
        writeBytes(any, 2, value.toByteArray());
        return any.toByteArray();
    }

    private static byte[] encodeTransactionRaw(
            byte[] anyParameter,
            long timestamp,
            long expiration,
            byte[] refBlockBytes,
            byte[] refBlockHash
    ) {
        ByteArrayOutputStream contract = new ByteArrayOutputStream();
      /** 写入 protobuf varint 字段 */
        writeVarintField(contract, 1, 1);
      /** 写入 protobuf bytes 字段 */
        writeBytes(contract, 2, anyParameter);

        ByteArrayOutputStream raw = new ByteArrayOutputStream();
      /** 写入 protobuf bytes 字段 */
        writeBytes(raw, 1, copy(refBlockBytes, 2));
      /** 写入 protobuf bytes 字段 */
        writeBytes(raw, 4, copy(refBlockHash, 8));
      /** 写入 protobuf varint 字段 */
        writeVarintField(raw, 8, expiration);
      /** 写入 protobuf bytes 字段 */
        writeBytes(raw, 11, contract.toByteArray());
      /** 写入 protobuf varint 字段 */
        writeVarintField(raw, 14, timestamp);
        return raw.toByteArray();
    }

    /** 拷贝 */
    private static byte[] copy(byte[] src, int len) {
        return Arrays.copyOf(src, len);
    }

    /** 生成可恢复签名 */
    private static byte[] signRecoverable(PrivateKey key, byte[] hash) {
        ECKeyPair pair = ECKeyPair.create(key.getBytes());
        Sign.SignatureData data = Sign.signMessage(hash, pair, false);
        byte[] out = new byte[65];
        System.arraycopy(data.getR(), 0, out, 0, 32);
        System.arraycopy(data.getS(), 0, out, 32, 32);
        out[64] = data.getV()[0];
        return out;
    }

    /** 写入 protobuf 字符串字段 */
    private static void writeString(ByteArrayOutputStream out, int fieldNumber, String value) {
      /** 写入 protobuf bytes 字段 */
        writeBytes(out, fieldNumber, value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** 写入 protobuf bytes 字段 */
    private static void writeBytes(ByteArrayOutputStream out, int fieldNumber, byte[] value) {
      /** 写入 varint */
        writeVarint(out, (fieldNumber << 3) | 2);
      /** 写入 varint */
        writeVarint(out, value.length);
        out.writeBytes(value);
    }

    /** 写入 protobuf varint 字段 */
    private static void writeVarintField(ByteArrayOutputStream out, int fieldNumber, long value) {
      /** 写入 varint */
        writeVarint(out, (long) fieldNumber << 3);
      /** 写入 varint */
        writeVarint(out, value);
    }

    /** 写入 varint */
    private static void writeVarint(ByteArrayOutputStream out, long value) {
        long v = value;
        while ((v & ~0x7FL) != 0) {
            out.write((int) ((v & 0x7F) | 0x80));
            v >>>= 7;
        }
        out.write((int) v);
    }
}
