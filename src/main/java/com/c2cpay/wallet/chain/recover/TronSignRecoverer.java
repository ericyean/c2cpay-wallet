package com.c2cpay.wallet.chain.recover;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.c2cpay.wallet.chain.tron.TronAddress;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.crypto.Hashing;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.crypto.Secp256k1Recover;
import com.c2cpay.wallet.dto.RecoverSignRequest;
import com.c2cpay.wallet.dto.RecoverSignResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** TronSignRecoverer 类 */
@Component
public class TronSignRecoverer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 从已签名交易恢复付款地址并验签 */
    public RecoverSignResponse recover(RecoverSignRequest request) {
        try {
            byte[] rawData;
            byte[] signature;
            String txIdFromJson = null;

            if (StringUtils.hasText(request.rawJson())) {
                JsonNode root = objectMapper.readTree(request.rawJson());
                String rawHex = text(root, "raw_data_hex");
                if (!StringUtils.hasText(rawHex)) {
                    throw new IllegalArgumentException("rawJson 缺少 raw_data_hex");
                }
                rawData = Hex.decode(rawHex);
                JsonNode sigNode = root.get("signature");
                if (sigNode == null || !sigNode.isArray() || sigNode.isEmpty()) {
                    throw new IllegalArgumentException("rawJson 缺少 signature");
                }
                signature = Hex.decode(sigNode.get(0).asText());
                if (root.hasNonNull("txID")) {
                    txIdFromJson = root.get("txID").asText();
                }
            } else {
                throw new IllegalArgumentException("TRON 验签需要 rawJson（含 raw_data_hex 与 signature）");
            }

            byte[] hash = Hashing.sha256(rawData);
            String txId = txIdFromJson != null ? txIdFromJson : Hex.encode(hash);
            PublicKey pub = Secp256k1Recover.recoverPublicKey(hash, signature);
            String address = TronAddress.fromPublicKey(pub);
            boolean valid = !StringUtils.hasText(request.expectedAddress())
                    || address.equals(request.expectedAddress().trim());
            return new RecoverSignResponse(
                    CoinType.TRON.getId(),
                    valid,
                    address,
                    txId,
                    Hex.encode(pub.getBytes()),
                    valid ? "TRON recover 成功" : "恢复地址与期望地址不一致");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("TRON 签名恢复失败: " + e.getMessage(), e);
        }
    }

    /** 读取文本字段 */
    private static String text(JsonNode root, String field) {
        JsonNode n = root.get(field);
        return n == null || n.isNull() ? null : n.asText();
    }
}
