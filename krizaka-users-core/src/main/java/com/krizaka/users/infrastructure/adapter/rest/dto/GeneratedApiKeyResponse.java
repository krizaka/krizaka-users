package com.krizaka.users.infrastructure.adapter.rest.dto;

import com.krizaka.users.domain.model.GeneratedApiKey;
import java.util.Objects;

/**
 * DTO record returned exactly once, at creation, carrying the plaintext secret ({@code key}).
 *
 * @param id The key identifier.
 * @param name The user-supplied label.
 * @param keyPrefix The short display prefix.
 * @param key The full plaintext secret — shown once, never retrievable again.
 */
public record GeneratedApiKeyResponse(String id, String name, String keyPrefix, String key) {

  public GeneratedApiKeyResponse {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(keyPrefix, "keyPrefix must not be null");
    Objects.requireNonNull(key, "key must not be null");
  }

  /**
   * Projects a domain {@link GeneratedApiKey} into its one-time REST representation.
   *
   * @param generated The domain record.
   * @return The response DTO.
   */
  public static GeneratedApiKeyResponse from(GeneratedApiKey generated) {
    return new GeneratedApiKeyResponse(
        generated.id(), generated.name(), generated.keyPrefix(), generated.plaintext());
  }
}
