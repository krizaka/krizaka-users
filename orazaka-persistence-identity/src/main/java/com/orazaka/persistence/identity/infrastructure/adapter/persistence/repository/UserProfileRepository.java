package com.orazaka.persistence.identity.infrastructure.adapter.persistence.repository;

import com.orazaka.persistence.identity.infrastructure.adapter.persistence.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JpaRepository interface for {@link UserProfileEntity} persistence operations. */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, String> {}
