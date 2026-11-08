package com.c2cpay.wallet.repository;

import com.c2cpay.wallet.domain.HdIndexCounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HdIndexCounterRepository extends JpaRepository<HdIndexCounterEntity, String> {
}
