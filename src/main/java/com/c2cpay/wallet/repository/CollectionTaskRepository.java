package com.c2cpay.wallet.repository;

import com.c2cpay.wallet.domain.CollectionTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** CollectionTaskRepository 类 */
public interface CollectionTaskRepository extends JpaRepository<CollectionTaskEntity, Long> {

    /** 查询by status */
    List<CollectionTaskEntity> findByStatus(CollectionTaskEntity.Status status);
}
