package com.orazaka.identity.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Signing configuration of the identity session JWTs ({@code orazaka.identity.jwt}).
 *
 * <p>HS256 with a shared secret in the local phase (every host validates locally — no per-request
 * identity hop); documented upgrade path is RS256/JWKS once off the laptop.
 *
 * @param secret the HMAC-SHA256 signing secret (≥ 32 bytes)
 * @param issuer the {@code iss} claim
 * @param ttl session token lifetime ({@code exp = iat + ttl}) — bounds RBAC/tier staleness
 */
@ConfigurationProperties(prefix = "orazaka.identity.jwt")
public record JwtProperties(
    String secret,
    @DefaultValue("orazaka-identity") String issuer,
    @DefaultValue("PT30M") Duration ttl) {

  public JwtProperties {
    if (secret == null || secret.length() < 32) {
      throw new IllegalArgumentException(
          "orazaka.identity.jwt.secret must be at least 32 characters (256-bit HS256)");
    }
    if (issuer == null || issuer.isBlank()) {
      throw new IllegalArgumentException("orazaka.identity.jwt.issuer cannot be blank");
    }
    if (ttl == null || ttl.isNegative() || ttl.isZero()) {
      throw new IllegalArgumentException("orazaka.identity.jwt.ttl must be positive");
    }
  }
}
