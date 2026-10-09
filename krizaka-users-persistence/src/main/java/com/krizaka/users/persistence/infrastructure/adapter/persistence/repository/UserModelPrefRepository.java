package com.krizaka.users.persistence.infrastructure.adapter.persistence.repository;

import com.krizaka.users.persistence.infrastructure.adapter.persistence.entity.UserModelPrefEntity;
import com.krizaka.users.persistence.infrastructure.adapter.persistence.entity.UserModelPrefId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JpaRepository for {@link UserModelPrefEntity} (composite key {@link UserModelPrefId}). */
@Repository
public interface UserModelPrefRepository
    extends JpaRepository<UserModelPrefEntity, UserModelPrefId> {

  /** All per-category default-model preferences for one user. */
  List<UserModelPrefEntity> findByUserId(String userId);
}
