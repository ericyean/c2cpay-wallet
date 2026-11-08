package com.c2cpay.wallet.service;

import com.c2cpay.wallet.coin.CoinDispatcher;
import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.crypto.HdWallet;
import com.c2cpay.wallet.crypto.Hex;
import com.c2cpay.wallet.crypto.KeyResolver;
import com.c2cpay.wallet.crypto.PrivateKey;
import com.c2cpay.wallet.crypto.PublicKey;
import com.c2cpay.wallet.dto.CoinInfoResponse;
import com.c2cpay.wallet.dto.DeriveAddressRequest;
import com.c2cpay.wallet.dto.DeriveAddressResponse;
import com.c2cpay.wallet.dto.MnemonicResponse;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import com.c2cpay.wallet.dto.ValidateAddressRequest;
import com.c2cpay.wallet.dto.ValidateAddressResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class WalletService {

    private final CoinDispatcher dispatcher;
    private final KeyResolver keyResolver;

    public WalletService(CoinDispatcher dispatcher, KeyResolver keyResolver) {
        this.dispatcher = dispatcher;
        this.keyResolver = keyResolver;
    }

    public MnemonicResponse createMnemonic(int strengthBits) {
        HdWallet wallet = HdWallet.create(strengthBits);
        return new MnemonicResponse(wallet.mnemonic(), wallet.words().size());
    }

    public boolean validateMnemonic(String mnemonic) {
        return HdWallet.isValidMnemonic(mnemonic);
    }

    public List<CoinInfoResponse> listCoins() {
        return dispatcher.supportedCoins().stream()
                .map(c -> new CoinInfoResponse(
                        c.getCoinId(),
                        c.getId(),
                        c.getSymbol(),
                        c.getDecimals(),
                        c.getDefaultDerivationPath()))
                .toList();
    }

    public ValidateAddressResponse validateAddress(ValidateAddressRequest request) {
        CoinType coin = resolveCoin(request.coin());
        boolean valid = dispatcher.validateAddress(coin, request.address());
        return new ValidateAddressResponse(coin.getId(), request.address(), valid);
    }

    public DeriveAddressResponse deriveAddress(DeriveAddressRequest request) {
        CoinType coin = resolveCoin(request.coin());
        PrivateKey privateKey = keyResolver.resolve(coin, request);
        boolean compressed = coin == CoinType.BITCOIN;
        PublicKey publicKey = privateKey.getPublicKey(compressed);
        String path = keyResolver.effectivePath(coin, request.derivationPath());
        String address = dispatcher.deriveAddress(coin, publicKey);
        return new DeriveAddressResponse(coin.getId(), address, Hex.encode(publicKey.getBytes()), path);
    }

    public SignResponse sign(SignRequest request) {
        CoinType coin = resolveCoin(request.coin());
        return dispatcher.sign(coin, request);
    }

    private CoinType resolveCoin(String raw) {
        return CoinType.fromId(raw)
                .or(() -> {
                    try {
                        return CoinType.fromCoinId(Integer.parseInt(raw.trim()));
                    } catch (NumberFormatException e) {
                        return java.util.Optional.empty();
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown coin '" + raw + "'. Supported: " +
                                Arrays.toString(dispatcher.supportedCoins().toArray())));
    }
}
