package com.orazaka.identity.infrastructure.adapter.rest.dto;

import com.orazaka.identity.domain.exception.InvalidRequestException;

/**
 * Request data containing login credentials.
 *
 * @param email The user's email address.
 * @param password The user's plaintext password.
 */
public record LoginRequest(String email, String password) {
  /** Compact constructor validating credentials. */
  public LoginRequest {
    if (email == null || email.isBlank() || password == null || password.isBlank()) {
      throw new InvalidRequestException("Email and password are required");
    }
  }
}
