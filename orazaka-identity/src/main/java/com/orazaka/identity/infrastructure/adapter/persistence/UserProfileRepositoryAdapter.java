package com.orazaka.identity.infrastructure.adapter.persistence;

import com.orazaka.identity.domain.model.UserProfile;
import com.orazaka.identity.domain.ports.outbound.UserProfileRepositoryPort;
import com.orazaka.persistence.identity.domain.ports.UserProfilePersistenceProvider;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Outbound persistence adapter implementing {@link UserProfileRepositoryPort}. Delegates
 * persistence requests to the clean UserProfilePersistenceProvider port.
 */
@Component
class UserProfileRepositoryAdapter implements UserProfileRepositoryPort {

  private final UserProfilePersistenceProvider provider;

  UserProfileRepositoryAdapter(UserProfilePersistenceProvider provider) {
    this.provider =
        Objects.requireNonNull(provider, "UserProfilePersistenceProvider cannot be null");
  }

  @Override
  public Optional<UserProfile> findByUserId(String userId) {
    Objects.requireNonNull(userId, "UserId cannot be null");
    return provider.findByUserId(userId).map(UserProfileMapper::toDomain);
  }
}
