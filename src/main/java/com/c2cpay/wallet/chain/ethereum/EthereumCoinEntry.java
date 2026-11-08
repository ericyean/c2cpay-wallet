package com.c2cpay.wallet.chain.ethereum;

import com.c2cpay.wallet.coin.CoinEntry;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.KeyResolver;
import com.c2cpay.wallet.crypto.PrivateKey;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/** EthereumCoinEntry 类 */
@Component
public class EthereumCoinEntry implements CoinEntry {

    private final KeyResolver keyResolver;

    /** 构造 EthereumCoinEntry */
    public EthereumCoinEntry(KeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }

    /** coin type */
    @Override
    public CoinType coinType() {
        return CoinType.ETHEREUM;
    }

    /** 校验地址 */
    @Override
    public boolean validateAddress(String address) {
        return EthereumAddress.isValid(address);
    }

    /** 派生地址 */
    @Override
    public String deriveAddress(PublicKey publicKey) {
        return EthereumAddress.fromPublicKey(publicKey);
    }

    /** 按币种签名交易 */
    @Override
    public SignResponse sign(SignRequest request) {
        PrivateKey privateKey = keyResolver.resolve(coinType(), request);
        Credentials credentials = Credentials.create(Hex.encode(privateKey.getBytes()));

        if (!StringUtils.hasText(request.to())) {
            throw new IllegalArgumentException("Ethereum sign requires 'to'");
        }
        if (request.nonce() == null) {
            throw new IllegalArgumentException("Ethereum sign requires 'nonce'");
        }

        long chainId = request.chainId() == null ? 1L : request.chainId();
        BigInteger value = request.valueWeiOrZero();
        BigInteger gasLimit = StringUtils.hasText(request.gasLimit())
                ? new BigInteger(request.gasLimit())
                : BigInteger.valueOf(21_000L);
        byte[] data = StringUtils.hasText(request.dataHex()) ? Hex.decode(request.dataHex()) : new byte[0];

        RawTransaction rawTx;
        if (StringUtils.hasText(request.maxFeePerGasWei()) && StringUtils.hasText(request.maxPriorityFeePerGasWei())) {
            rawTx = RawTransaction.createTransaction(
                    chainId,
                    BigInteger.valueOf(request.nonce()),
                    gasLimit,
                    request.to(),
                    value,
                    Numeric.toHexString(data),
                    new BigInteger(request.maxPriorityFeePerGasWei()),
                    new BigInteger(request.maxFeePerGasWei()));
        } else {
            BigInteger gasPrice = StringUtils.hasText(request.gasPriceWei())
                    ? new BigInteger(request.gasPriceWei())
                    : new BigInteger("20000000000");
            rawTx = RawTransaction.createTransaction(
                    BigInteger.valueOf(request.nonce()),
                    gasPrice,
                    gasLimit,
                    request.to(),
                    value,
                    Numeric.toHexString(data));
        }

        byte[] signed = TransactionEncoder.signMessage(rawTx, chainId, credentials);
        String encoded = Numeric.toHexString(signed);
        String txId = Numeric.toHexString(org.web3j.crypto.Hash.sha3(signed));
        return SignResponse.of(coinType().getId(), encoded, txId);
    }
}
