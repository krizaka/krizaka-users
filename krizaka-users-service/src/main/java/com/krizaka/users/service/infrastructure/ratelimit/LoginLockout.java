package com.krizaka.users.service.infrastructure.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

/**
 * Progressive lockout of failed logins, in memory (one instance of the identity service; a shared
 * store comes with the second one).
 *
 * <p>Two scopes: one account <em>from one address</em> (so an attacker cannot lock a victim out
 * from elsewhere) and one account from <em>anywhere</em> (so a botnet spreading its guesses is
 * still stopped, at a higher threshold). Past a threshold every further failure doubles the lock,
 * up to a ceiling; a success clears the pair. A locked account refuses even the right password —
 * that is the point.
 */
final class LoginLockout {

  private static final int MAX_DOUBLINGS = 10;

  private final AuthRateLimitProperties.Lockout settings;
  private final Ticker ticker;
  private final Cache<String, State> states;

  LoginLockout(AuthRateLimitProperties.Lockout settings, Ticker ticker) {
    this.settings = settings;
    this.ticker = ticker;
    this.states =
        Caffeine.newBuilder()
            .ticker(ticker)
            .maximumSize(200_000)
            .expireAfterAccess(settings.forgetAfter().plus(settings.maxDelay()))
            .build();
  }

  /** Seconds the caller must wait before trying again, or {@code 0} when it may try. */
  long retryAfterSeconds(String account, String ip) {
    long wait = Math.max(remaining(pairKey(account, ip)), remaining(accountKey(account)));
    return wait <= 0 ? 0 : Math.max(1, (wait + 999_999_999L) / 1_000_000_000L);
  }

  void recordFailure(String account, String ip) {
    bump(pairKey(account, ip), settings.threshold());
    bump(accountKey(account), settings.accountThreshold());
  }

  void recordSuccess(String account, String ip) {
    states.invalidate(pairKey(account, ip));
    // The account-wide counter is kept: one valid login from the owner must not reset a
    // distributed attack that is still running against the same account.
  }

  private long remaining(String key) {
    State state = states.getIfPresent(key);
    return state == null ? 0 : state.lockedUntilNanos - ticker.read();
  }

  private void bump(String key, int threshold) {
    states.asMap().compute(key, (k, old) -> next(old, threshold));
  }

  private State next(State old, int threshold) {
    long now = ticker.read();
    boolean stale = old == null || now - old.lastFailureNanos > settings.forgetAfter().toNanos();
    int failures = stale ? 1 : old.failures + 1;
    long lockedUntil = stale ? 0 : old.lockedUntilNanos;
    if (failures >= threshold) {
      int doublings = Math.min(failures - threshold, MAX_DOUBLINGS);
      long delay =
          Math.min(settings.baseDelay().toNanos() << doublings, settings.maxDelay().toNanos());
      lockedUntil = now + delay;
    }
    return new State(failures, now, lockedUntil);
  }

  private static String pairKey(String account, String ip) {
    return "p|" + account + "|" + ip;
  }

  private static String accountKey(String account) {
    return "a|" + account;
  }

  private record State(int failures, long lastFailureNanos, long lockedUntilNanos) {}
}
