package com.c2cpay.wallet.repository;

import com.c2cpay.wallet.domain.DepositAddressEntity;
import com.c2cpay.wallet.domain.DepositAddressStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** DepositAddressRepository 类 */
public interface DepositAddressRepository extends JpaRepository<DepositAddressEntity, Long> {

    /** 查询by biz order ID */
    Optional<DepositAddressEntity> findByBizOrderId(String bizOrderId);

    /** 查询by address ignore case */
    Optional<DepositAddressEntity> findByAddressIgnoreCase(String address);

    /** 查询by status */
    List<DepositAddressEntity> findByStatus(DepositAddressStatus status);
}
