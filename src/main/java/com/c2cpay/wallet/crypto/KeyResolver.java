package com.c2cpay.wallet.crypto;

import com.c2cpay.wallet.coin.CoinType;
import com.c2cpay.wallet.dto.DeriveAddressRequest;
import com.c2cpay.wallet.dto.SignRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KeyResolver {

    public PrivateKey resolve(CoinType coinType, String privateKeyHex, String mnemonic, String derivationPath) {
        if (StringUtils.hasText(privateKeyHex)) {
            return PrivateKey.fromHex(privateKeyHex.trim());
        }
        if (StringUtils.hasText(mnemonic)) {
            String path = StringUtils.hasText(derivationPath) ? derivationPath : coinType.getDefaultDerivationPath();
            return HdWallet.fromMnemonic(mnemonic).getKey(path);
        }
        throw new IllegalArgumentException("Provide either privateKeyHex or mnemonic");
    }

    public PrivateKey resolve(CoinType coinType, SignRequest request) {
        return resolve(coinType, request.privateKeyHex(), request.mnemonic(), request.derivationPath());
    }

    public PrivateKey resolve(CoinType coinType, DeriveAddressRequest request) {
        return resolve(coinType, request.privateKeyHex(), request.mnemonic(), request.derivationPath());
    }

    public String effectivePath(CoinType coinType, String derivationPath) {
        return StringUtils.hasText(derivationPath) ? derivationPath : coinType.getDefaultDerivationPath();
    }
}
