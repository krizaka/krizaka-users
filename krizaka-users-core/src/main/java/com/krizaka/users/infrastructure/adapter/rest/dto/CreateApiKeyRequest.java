package com.krizaka.users.infrastructure.adapter.rest.dto;

import java.util.Objects;

/**
 * DTO record representing a request to create a new inbound API key.
 *
 * @param name The user-supplied label for the key.
 */
public record CreateApiKeyRequest(String name) {

  public CreateApiKeyRequest {
    Objects.requireNonNull(name, "name must not be null");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
  }
}
