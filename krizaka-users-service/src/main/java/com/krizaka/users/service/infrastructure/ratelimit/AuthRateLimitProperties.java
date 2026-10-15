package com.krizaka.users.service.infrastructure.ratelimit;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Limits of the public authentication endpoints (krizaka-users#17). Every figure is overridable
 * through {@code krizaka.users-service.rate-limit.*}; the defaults are the production stance.
 *
 * @param enabled master switch (only ever turned off in a load test)
 * @param trustedProxies addresses / CIDRs whose {@code X-Forwarded-For} is believed; the direct
 *     peer is the edge, so the private ranges are trusted by default
 * @param clientIpHeader optional header carrying the real client address when a CDN fronts the edge
 *     (e.g. {@code CF-Connecting-IP}, {@code DO-Connecting-IP}); honoured only from a trusted peer
 * @param login per-IP budget of {@code /login}
 * @param oauth per-IP budget of {@code /oauth}
 * @param register per-IP budget of {@code /register}
 * @param forgot per-IP budget of {@code /forgot}
 * @param reset per-IP budget of {@code /reset}
 * @param verify per-IP budget of {@code /verify}
 * @param emailMessages per-address budget of the endpoints that send an e-mail
 * @param lockout progressive lockout of failed logins
 */
@ConfigurationProperties(prefix = "krizaka.users-service.rate-limit")
public record AuthRateLimitProperties(
    @DefaultValue("true") boolean enabled,
    @DefaultValue({
          "127.0.0.0/8",
          "::1",
          "10.0.0.0/8",
          "172.16.0.0/12",
          "192.168.0.0/16",
          "fc00::/7"
        })
        List<String> trustedProxies,
    @DefaultValue("") String clientIpHeader,
    @DefaultValue({"20", "PT1M"}) Budget login,
    @DefaultValue({"30", "PT1M"}) Budget oauth,
    @DefaultValue({"5", "PT1H"}) Budget register,
    @DefaultValue({"5", "PT1H"}) Budget forgot,
    @DefaultValue({"10", "PT15M"}) Budget reset,
    @DefaultValue({"20", "PT1M"}) Budget verify,
    @DefaultValue({"3", "PT1H"}) Budget emailMessages,
    @DefaultValue Lockout lockout) {

  /**
   * A budget: {@code requests} per {@code window}.
   *
   * @param requests tokens in the bucket
   * @param window time to refill the whole bucket
   */
  public record Budget(int requests, Duration window) {}

  /**
   * Progressive lockout of failed logins.
   *
   * @param threshold consecutive failures of one account from one address before the first lock
   * @param accountThreshold failures of one account from any address before it is locked for all
   * @param baseDelay first lock; doubles with every further failure
   * @param maxDelay ceiling of a lock
   * @param forgetAfter quiet time after which the failures are forgotten
   */
  public record Lockout(
      @DefaultValue("5") int threshold,
      @DefaultValue("25") int accountThreshold,
      @DefaultValue("PT1M") Duration baseDelay,
      @DefaultValue("PT15M") Duration maxDelay,
      @DefaultValue("PT30M") Duration forgetAfter) {}
}
