package com.orazaka.persistence.identity.domain.ports;

import com.orazaka.persistence.identity.domain.model.RateLimitDto;
import java.util.Optional;

/** Port interface for managing RateLimit persistence operations. */
public interface RateLimitPersistenceProvider {

  Optional<RateLimitDto> findById(String tierKey);

  RateLimitDto save(RateLimitDto rateLimitDto);

  /**
   * Resolves the tier key flagged as the system default in the database.
   *
   * @return the default tier key, or empty if none is flagged.
   */
  Optional<String> findDefaultTierKey();
}
