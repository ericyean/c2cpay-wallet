package com.c2cpay.wallet.service;

import com.c2cpay.wallet.dto.RecoverSignRequest;
import com.c2cpay.wallet.dto.RecoverSignResponse;
import com.c2cpay.wallet.dto.SignRequest;
import com.c2cpay.wallet.dto.SignResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SignRecoverServiceTest {

    private static final String MNEMONIC =
            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
    private static final String ETH_ADDRESS = "0x9858EfFD232B4033E47d90003D41EC34EcaEda94";

    @Autowired
    private WalletService walletService;
    @Autowired
    private SignRecoverService signRecoverService;

    @Test
    void ethereumSignThenRecoverMatchesDerivedAddress() {
        SignResponse signed = walletService.sign(new SignRequest(
                "ethereum",
                null,
                MNEMONIC,
                "m/44'/60'/0'/0/0",
                "0x000000000000000000000000000000000000dEaD",
                "1000000000000000",
                null,
                0L,
                "20000000000",
                "21000",
                1L,
                null,
                null,
                null, null, null, null, null,
                null, null, null, null, null, null
        ));

        RecoverSignResponse recovered = signRecoverService.recover(new RecoverSignRequest(
                "ethereum",
                signed.encoded(),
                null,
                ETH_ADDRESS
        ));

        assertTrue(recovered.valid());
        assertEquals(ETH_ADDRESS.toLowerCase(), recovered.recoveredAddress().toLowerCase());
        assertEquals(signed.txId().toLowerCase(), recovered.txId().toLowerCase());
    }

    @Test
    void tronSignThenRecoverMatchesOwner() {
        SignResponse signed = walletService.sign(new SignRequest(
                "tron",
                null,
                MNEMONIC,
                "m/44'/195'/0'/0/0",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null,
                "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb",
                1_000_000L,
                1_700_000_000_000L,
                1_700_000_060_000L,
                "0000",
                "0000000000000000"
        ));

        RecoverSignResponse recovered = signRecoverService.recover(new RecoverSignRequest(
                "tron",
                signed.encoded(),
                signed.rawJson(),
                null
        ));

        assertTrue(recovered.valid());
        assertTrue(recovered.recoveredAddress().startsWith("T"));
        assertEquals(signed.txId().toLowerCase(), recovered.txId().toLowerCase());
    }
}
