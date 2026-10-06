package com.orazaka.persistence.identity.application.service;

import com.orazaka.persistence.identity.domain.model.UserModelPrefDto;
import com.orazaka.persistence.identity.domain.ports.UserModelPrefPersistenceProvider;
import com.orazaka.persistence.identity.infrastructure.adapter.persistence.entity.UserModelPrefEntity;
import com.orazaka.persistence.identity.infrastructure.adapter.persistence.entity.UserModelPrefId;
import com.orazaka.persistence.identity.infrastructure.adapter.persistence.repository.UserModelPrefRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Package-private implementation of {@link UserModelPrefPersistenceProvider}. */
@Service
@Transactional
class UserModelPrefPersistenceProviderImpl implements UserModelPrefPersistenceProvider {

  private final UserModelPrefRepository repository;

  UserModelPrefPersistenceProviderImpl(UserModelPrefRepository repository) {
    this.repository = Objects.requireNonNull(repository, "UserModelPrefRepository cannot be null");
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserModelPrefDto> findByUserId(String userId) {
    Objects.requireNonNull(userId, "userId cannot be null");
    return repository.findByUserId(userId).stream()
        .map(UserModelPrefPersistenceProviderImpl::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserModelPrefDto> find(String userId, String category) {
    Objects.requireNonNull(userId, "userId cannot be null");
    Objects.requireNonNull(category, "category cannot be null");
    return repository
        .findById(new UserModelPrefId(userId, category))
        .map(UserModelPrefPersistenceProviderImpl::toDto);
  }

  @Override
  public UserModelPrefDto save(UserModelPrefDto pref) {
    Objects.requireNonNull(pref, "UserModelPrefDto cannot be null");
    UserModelPrefEntity saved = repository.save(toEntity(pref));
    return toDto(saved);
  }

  private static UserModelPrefDto toDto(UserModelPrefEntity entity) {
    if (entity == null) {
      return null;
    }
    return new UserModelPrefDto(
        entity.getUserId(), entity.getCategory(), entity.getModelId(), entity.getVoice());
  }

  static UserModelPrefEntity toEntity(UserModelPrefDto dto) {
    if (dto == null) {
      return null;
    }
    UserModelPrefEntity entity = new UserModelPrefEntity();
    entity.setUserId(dto.userId());
    entity.setCategory(dto.category());
    entity.setModelId(dto.modelId());
    entity.setVoice(dto.voice());
    return entity;
  }
}
