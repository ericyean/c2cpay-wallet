package com.c2cpay.wallet.chain.recover;

import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.dto.RecoverSignRequest;
import com.c2cpay.wallet.dto.RecoverSignResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.crypto.transaction.type.TransactionType;
import org.web3j.utils.Numeric;

/** EvmSignRecoverer 类 */
@Component
public class EvmSignRecoverer {

    /** 从已签名交易恢复付款地址并验签 */
    public RecoverSignResponse recover(CoinType coin, RecoverSignRequest request) {
        try {
            var decoded = TransactionDecoder.decode(Numeric.prependHexPrefix(request.encoded()));
            if (!(decoded instanceof SignedRawTransaction signed)) {
                return new RecoverSignResponse(
                        coin.getId(), false, null, null, null, "交易未包含签名，无法 recover");
            }
            String from = signed.getFrom();
            byte[] raw = Numeric.hexStringToByteArray(Numeric.prependHexPrefix(request.encoded()));
            String txId = Numeric.toHexString(org.web3j.crypto.Hash.sha3(raw));
            boolean valid = !StringUtils.hasText(request.expectedAddress())
                    || from.equalsIgnoreCase(request.expectedAddress().trim());
            String type = signed.getType() == null ? TransactionType.LEGACY.name() : signed.getType().name();
            return new RecoverSignResponse(
                    coin.getId(),
                    valid,
                    from,
                    txId,
                    null,
                    valid ? "EVM recover 成功 (" + type + ")" : "恢复地址与期望地址不一致");
        } catch (Exception e) {
            throw new IllegalArgumentException("EVM 签名恢复失败: " + e.getMessage(), e);
        }
    }
}
