package com.c2cpay.wallet.repository;

import com.c2cpay.wallet.domain.PayoutOrderEntity;
import com.c2cpay.wallet.domain.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** PayoutOrderRepository 类 */
public interface PayoutOrderRepository extends JpaRepository<PayoutOrderEntity, Long> {

    /** 查询by biz order ID */
    Optional<PayoutOrderEntity> findByBizOrderId(String bizOrderId);

    /** 查询by status */
    List<PayoutOrderEntity> findByStatus(PayoutStatus status);
}
