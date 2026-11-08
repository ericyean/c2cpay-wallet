package com.c2cpay.wallet.repository;

import com.c2cpay.wallet.domain.DepositAddressEntity;
import com.c2cpay.wallet.domain.DepositAddressStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepositAddressRepository extends JpaRepository<DepositAddressEntity, Long> {

    Optional<DepositAddressEntity> findByBizOrderId(String bizOrderId);

    Optional<DepositAddressEntity> findByAddressIgnoreCase(String address);

    List<DepositAddressEntity> findByStatus(DepositAddressStatus status);
}
