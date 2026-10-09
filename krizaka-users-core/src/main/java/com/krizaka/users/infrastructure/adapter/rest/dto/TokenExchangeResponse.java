package com.krizaka.users.infrastructure.adapter.rest.dto;

import java.util.Objects;

/**
 * Internal response carrying the session JWT minted for a valid API key.
 *
 * @param token the compact session JWT
 */
public record TokenExchangeResponse(String token) {

  public TokenExchangeResponse {
    Objects.requireNonNull(token, "token is required");
  }
}
