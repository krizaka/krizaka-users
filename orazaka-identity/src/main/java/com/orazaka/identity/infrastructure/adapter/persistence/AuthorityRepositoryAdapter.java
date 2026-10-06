package com.orazaka.identity.infrastructure.adapter.persistence;

import com.orazaka.identity.domain.ports.outbound.AuthorityRepositoryPort;
import com.orazaka.persistence.identity.domain.ports.AuthorityPersistenceProvider;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Outbound persistence adapter implementing {@link AuthorityRepositoryPort}. Delegates persistence
 * requests to the clean AuthorityPersistenceProvider port.
 */
@Component
class AuthorityRepositoryAdapter implements AuthorityRepositoryPort {

  private final AuthorityPersistenceProvider provider;

  AuthorityRepositoryAdapter(AuthorityPersistenceProvider provider) {
    this.provider = Objects.requireNonNull(provider, "AuthorityPersistenceProvider cannot be null");
  }

  @Override
  public void saveAuthority(String userId, String authorityName) {
    Objects.requireNonNull(userId, "UserId cannot be null");
    Objects.requireNonNull(authorityName, "AuthorityName cannot be null");
    provider.saveAuthority(userId, authorityName);
  }
}
