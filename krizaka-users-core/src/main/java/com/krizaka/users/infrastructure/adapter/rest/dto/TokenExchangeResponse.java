package com.krizaka.users.infrastructure.adapter.rest.dto;

import java.util.Objects;

/**
 * Internal response carrying the session JWT minted for a valid API key.
 *
 * @param token the compact session JWT
 * @param scopes what the key may do (e.g. {@code ai:embed}); empty = the owner's full rights. The
 *     edge enforces it: the JWT itself is the owner's.
 */
public record TokenExchangeResponse(String token, java.util.List<String> scopes) {

  public TokenExchangeResponse {
    Objects.requireNonNull(token, "token is required");
    scopes = scopes == null ? java.util.List.of() : java.util.List.copyOf(scopes);
  }

  /** An unscoped exchange. */
  public TokenExchangeResponse(String token) {
    this(token, java.util.List.of());
  }
}
