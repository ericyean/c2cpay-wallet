package com.c2cpay.wallet.repository;

import com.c2cpay.wallet.domain.PayoutOrderEntity;
import com.c2cpay.wallet.domain.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayoutOrderRepository extends JpaRepository<PayoutOrderEntity, Long> {

    Optional<PayoutOrderEntity> findByBizOrderId(String bizOrderId);

    List<PayoutOrderEntity> findByStatus(PayoutStatus status);
}
