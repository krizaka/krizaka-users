package com.orazaka.persistence.identity.application.service;

import com.orazaka.persistence.identity.domain.model.RateLimitDto;
import com.orazaka.persistence.identity.domain.ports.RateLimitPersistenceProvider;
import com.orazaka.persistence.identity.infrastructure.adapter.persistence.entity.RateLimitEntity;
import com.orazaka.persistence.identity.infrastructure.adapter.persistence.repository.RateLimitRepository;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Package-private implementation of RateLimitPersistenceProvider. */
@Service
@Transactional
class RateLimitPersistenceProviderImpl implements RateLimitPersistenceProvider {

  private final RateLimitRepository repository;

  RateLimitPersistenceProviderImpl(RateLimitRepository repository) {
    this.repository = Objects.requireNonNull(repository, "RateLimitRepository cannot be null");
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RateLimitDto> findById(String tierKey) {
    Objects.requireNonNull(tierKey, "Tier key cannot be null");
    return repository.findById(tierKey).map(RateLimitPersistenceProviderImpl::toDto);
  }

  @Override
  public RateLimitDto save(RateLimitDto rateLimitDto) {
    Objects.requireNonNull(rateLimitDto, "RateLimitDto cannot be null");
    RateLimitEntity entity = toEntity(rateLimitDto);
    RateLimitEntity saved = repository.save(entity);
    return toDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> findDefaultTierKey() {
    return repository.findFirstByIsDefaultTrue().map(RateLimitEntity::getTierKey);
  }

  private static RateLimitDto toDto(RateLimitEntity entity) {
    if (entity == null) {
      return null;
    }
    return new RateLimitDto(
        entity.getTierKey(), entity.getRequestsPerMinute(), entity.getConcurrentJobs());
  }

  static RateLimitEntity toEntity(RateLimitDto dto) {
    if (dto == null) {
      return null;
    }
    RateLimitEntity entity = new RateLimitEntity();
    entity.setTierKey(dto.tierKey());
    entity.setRequestsPerMinute(dto.requestsPerMinute());
    entity.setConcurrentJobs(dto.concurrentJobs());
    return entity;
  }
}
