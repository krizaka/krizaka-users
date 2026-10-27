package com.krizaka.users.infrastructure.config;

import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration of the identity session JWTs ({@code krizaka.users.jwt}).
 *
 * <p>With {@code signing} set (ADR-001), session tokens are ES256, signed by this issuer only; the
 * verifiers fetch the public keys from {@code /.well-known/jwks.json} and hold no signing material.
 * Without it the legacy HS256 shared secret signs them (deprecated). The {@code secret} still
 * verifies the machine-to-machine {@code SERVICE} tokens until per-caller keys land (slice 3).
 *
 * @param secret the HMAC-SHA256 secret (≥ 32 bytes)
 * @param issuer the {@code iss} claim — verifiers' {@code krizaka.security.jwt.issuer}
 * @param ttl session token lifetime ({@code exp = iat + ttl}) — bounds RBAC/tier staleness
 * @param signing the ES256 issuer key, or {@code null} for the legacy HS256 mode
 */
@ConfigurationProperties(prefix = "krizaka.users.jwt")
public record JwtProperties(
    String secret,
    @DefaultValue("krizaka-users-core") String issuer,
    @DefaultValue("PT30M") Duration ttl,
    Signing signing) {

  @ConstructorBinding
  public JwtProperties {
    if (secret == null || secret.length() < 32) {
      throw new IllegalArgumentException(
          "krizaka.users.jwt.secret must be at least 32 characters (256-bit HS256)");
    }
    if (issuer == null || issuer.isBlank()) {
      throw new IllegalArgumentException("krizaka.users.jwt.issuer cannot be blank");
    }
    if (ttl == null || ttl.isNegative() || ttl.isZero()) {
      throw new IllegalArgumentException("krizaka.users.jwt.ttl must be positive");
    }
  }

  /** Legacy HS256 mode: no issuer key. */
  public JwtProperties(String secret, String issuer, Duration ttl) {
    this(secret, issuer, ttl, null);
  }

  /**
   * The issuer's ES256 key pair, as PEM, from the environment ({@code AUTH_JWT_SIGNING_KEY}…) —
   * never committed, never logged.
   *
   * @param privateKey PKCS#8 PEM of the P-256 private key
   * @param publicKey SPKI PEM of the matching public key
   * @param keyId the {@code kid} of the current key (new tokens carry it)
   * @param retired previous {@code kid} to public-key PEM: still published so tokens in flight
   *     verify until they expire
   */
  public record Signing(
      String privateKey, String publicKey, String keyId, Map<String, String> retired) {

    public Signing {
      retired = retired == null ? Map.of() : Map.copyOf(retired);
    }

    /**
     * Whether any issuer key setting is present. Empty environment defaults bind as blank values,
     * so a present-but-blank block is not a configuration; a partial one is, and fails at startup.
     *
     * @return {@code true} when at least one of private key, public key or key id is non-blank
     */
    public boolean configured() {
      return !isBlank(privateKey) || !isBlank(publicKey) || !isBlank(keyId);
    }

    private static boolean isBlank(String value) {
      return value == null || value.isBlank();
    }

    @Override
    public String toString() {
      return "Signing[keyId=" + keyId + ", retired=" + retired.keySet() + ", privateKey=***]";
    }
  }

  @Override
  public String toString() {
    return "JwtProperties[issuer=" + issuer + ", ttl=" + ttl + ", signing=" + signing + "]";
  }
}
