package com.c2cpay.wallet.service;

import com.c2cpay.wallet.dto.business.AllocateDepositRequest;
import com.c2cpay.wallet.dto.business.DepositAddressResponse;
import com.c2cpay.wallet.dto.business.MarkDepositRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DepositAddressServiceTest {

    @Autowired
    private DepositAddressService depositAddressService;

    @Test
    void allocateTronDepositIsIdempotentAndIncrementsIndex() {
        DepositAddressResponse first = depositAddressService.allocate(
                new AllocateDepositRequest("order-1", "USDT_TRC20"));
        DepositAddressResponse again = depositAddressService.allocate(
                new AllocateDepositRequest("order-1", "USDT_TRC20"));
        DepositAddressResponse second = depositAddressService.allocate(
                new AllocateDepositRequest("order-2", "USDT_TRC20"));

        assertEquals(first.address(), again.address());
        assertEquals(first.derivationPath(), again.derivationPath());
        assertNotEquals(first.address(), second.address());
        assertTrue(first.address().startsWith("T"));
        assertEquals(0, first.hdIndex());
        assertEquals(1, second.hdIndex());
    }

    @Test
    void markDepositedSetsCollectibleWhenRiskLow() {
        DepositAddressResponse allocated = depositAddressService.allocate(
                new AllocateDepositRequest("order-mark-1", "USDT_TRC20"));
        DepositAddressResponse marked = depositAddressService.markDeposited(
                new MarkDepositRequest(allocated.bizOrderId(), "txid-demo", "10"));

        assertEquals("COLLECTIBLE", marked.status());
        assertEquals("LOW", marked.riskLevel());
        assertNotNull(marked.risk());
        assertTrue(marked.risk().allowCredit());
    }
}
