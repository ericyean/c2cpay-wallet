package com.c2cpay.wallet.repository;

import com.c2cpay.wallet.domain.CollectionTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionTaskRepository extends JpaRepository<CollectionTaskEntity, Long> {

    List<CollectionTaskEntity> findByStatus(CollectionTaskEntity.Status status);
}
