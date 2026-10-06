package com.orazaka.identity.application.service;

import com.orazaka.identity.domain.model.RateLimitInfo;
import com.orazaka.identity.domain.ports.inbound.RateLimitProvider;
import com.orazaka.persistence.identity.domain.model.RateLimitDto;
import com.orazaka.persistence.identity.domain.ports.RateLimitPersistenceProvider;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Package-private implementation of the {@link RateLimitProvider} inbound port [ERR-105].
 *
 * <p>Lives in {@code application.service} alongside its sibling {@code UserProfileProviderImpl}: it
 * implements an inbound port by delegating to an outbound persistence port — an application
 * service, not a persistence {@code *Adapter} ([ERR-130]). The single-use DTO mapping is inlined.
 */
@Service
@Transactional(readOnly = true)
class RateLimitProviderImpl implements RateLimitProvider {

  private final RateLimitPersistenceProvider provider;

  RateLimitProviderImpl(RateLimitPersistenceProvider provider) {
    this.provider = Objects.requireNonNull(provider, "RateLimitPersistenceProvider cannot be null");
  }

  @Override
  public Optional<RateLimitInfo> getRateLimit(String tierKey) {
    Objects.requireNonNull(tierKey, "Tier key cannot be null");
    return provider.findById(tierKey).map(RateLimitProviderImpl::toInfo);
  }

  @Override
  public Optional<String> getDefaultTierKey() {
    return provider.findDefaultTierKey();
  }

  private static RateLimitInfo toInfo(RateLimitDto dto) {
    return new RateLimitInfo(dto.tierKey(), dto.requestsPerMinute(), dto.concurrentJobs());
  }
}
