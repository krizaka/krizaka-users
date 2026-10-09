package com.krizaka.users.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Where the users service is and how to call it ({@code krizaka.users.client}).
 *
 * @param baseUrl the users service's base URL, e.g. {@code http://localhost:8083}
 * @param serviceSecret the shared HS256 secret the {@code SERVICE} token is signed with
 * @param serviceName who is calling — the token's subject, visible in the users service's logs
 * @param cacheTtl how long a user, profile or tier is reused before it is fetched again; default
 *     one minute
 */
@ConfigurationProperties(prefix = "krizaka.users.client")
public record UsersClientProperties(
    String baseUrl,
    String serviceSecret,
    String serviceName,
    @DefaultValue("PT60S") Duration cacheTtl) {

  /** Refuses an incomplete client: a call that cannot authenticate fails at startup, not later. */
  public UsersClientProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalArgumentException("krizaka.users.client.base-url is required");
    }
    if (serviceSecret == null || serviceSecret.isBlank()) {
      throw new IllegalArgumentException("krizaka.users.client.service-secret is required");
    }
    if (serviceName == null || serviceName.isBlank()) {
      throw new IllegalArgumentException("krizaka.users.client.service-name is required");
    }
    if (cacheTtl == null || cacheTtl.isNegative()) {
      throw new IllegalArgumentException("krizaka.users.client.cache-ttl cannot be negative");
    }
  }

  /**
   * Hides the secret.
   *
   * @return a description without the secret
   */
  @Override
  public String toString() {
    return "UsersClientProperties[baseUrl=%s, serviceName=%s, cacheTtl=%s, serviceSecret=<redacted>]"
        .formatted(baseUrl, serviceName, cacheTtl);
  }
}
