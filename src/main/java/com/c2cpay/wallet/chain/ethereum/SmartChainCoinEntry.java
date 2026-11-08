package com.c2cpay.wallet.chain.ethereum;

import com.c2cpay.wallet.coin.CoinEntry;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import org.springframework.stereotype.Component;

/**
 * BNB Smart Chain shares Ethereum address format; default chainId = 56.
 */
@Component
public class SmartChainCoinEntry implements CoinEntry {

    private final EthereumCoinEntry ethereum;

    public SmartChainCoinEntry(EthereumCoinEntry ethereum) {
        this.ethereum = ethereum;
    }

    @Override
    public CoinType coinType() {
        return CoinType.BINANCE_SMART_CHAIN;
    }

    @Override
    public boolean validateAddress(String address) {
        return ethereum.validateAddress(address);
    }

    @Override
    public String deriveAddress(PublicKey publicKey) {
        return ethereum.deriveAddress(publicKey);
    }

    @Override
    public SignResponse sign(SignRequest request) {
        Long chainId = request.chainId() == null ? 56L : request.chainId();
        SignRequest withChain = new SignRequest(
                coinType().getId(),
                request.privateKeyHex(),
                request.mnemonic(),
                request.derivationPath(),
                request.to(),
                request.valueWei(),
                request.dataHex(),
                request.nonce(),
                request.gasPriceWei(),
                request.gasLimit(),
                chainId,
                request.maxFeePerGasWei(),
                request.maxPriorityFeePerGasWei(),
                request.utxos(),
                request.toAddress(),
                request.amountSatoshi(),
                request.feeSatoshi(),
                request.changeAddress(),
                request.toTronAddress(),
                request.amountSun(),
                request.timestampMs(),
                request.expirationMs(),
                request.refBlockBytesHex(),
                request.refBlockHashHex()
        );
        SignResponse signed = ethereum.sign(withChain);
        return SignResponse.of(coinType().getId(), signed.encoded(), signed.txId(), signed.rawJson());
    }
}
