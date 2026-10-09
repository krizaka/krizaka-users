package com.krizaka.users.persistence.domain.ports;

import com.krizaka.users.persistence.domain.model.ApiKeyDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Port interface for managing inbound API key persistence operations. */
public interface ApiKeyPersistenceProvider {

  List<ApiKeyDto> findByUserId(String userId);

  long countByUserId(String userId);

  Optional<ApiKeyDto> findByKeyHash(String keyHash);

  ApiKeyDto save(ApiKeyDto apiKeyDto);

  long deleteByIdAndUserId(String id, String userId);

  void touchLastUsed(String id, Instant lastUsedAt);
}
