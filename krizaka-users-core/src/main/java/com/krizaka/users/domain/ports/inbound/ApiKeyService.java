package com.krizaka.users.domain.ports.inbound;

import com.krizaka.users.domain.model.ApiKey;
import com.krizaka.users.domain.model.GeneratedApiKey;
import com.krizaka.users.domain.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Inbound port for managing user-owned inbound API keys (Personal Access Tokens) and resolving them
 * during bearer authentication.
 *
 * <p>The router binds exclusively to this interface; the concrete implementation is package-private
 * within {@code krizaka-users-core}. Distinct from {@code IdentityService}'s credential methods,
 * which handle <b>outbound</b> BYOK provider keys.
 */
public interface ApiKeyService {

  /** Maximum number of active API keys a single user may own. */
  int MAX_KEYS_PER_USER = 10;

  /**
   * Lists a user's API keys (metadata only — never the secret).
   *
   * @param userId The owning user ID.
   * @return The user's keys, most recent first.
   */
  List<ApiKey> list(String userId);

  /**
   * Generates a new API key for a user, returning the plaintext secret exactly once.
   *
   * @param userId The owning user ID.
   * @param name The user-supplied label.
   * @return The generated key, including its one-time plaintext secret.
   * @throws com.krizaka.users.domain.exception.ApiKeyLimitExceededException if the user already
   *     owns {@link #MAX_KEYS_PER_USER} keys.
   * @throws com.krizaka.users.domain.exception.InvalidRequestException if the name is blank.
   */
  GeneratedApiKey create(String userId, String name);

  /**
   * Revokes (deletes) a user's API key. Scoped by owner so a user can never revoke another's key.
   *
   * @param userId The owning user ID.
   * @param keyId The key ID to revoke.
   * @return {@code true} if a key was revoked; {@code false} if none matched.
   */
  boolean revoke(String userId, String keyId);

  /**
   * Resolves a presented bearer secret to its owning, enabled {@link User}, stamping last-used.
   *
   * @param presentedKey The raw bearer token presented on the request.
   * @return The owning user if the key is valid and the account is enabled; otherwise empty.
   */
  Optional<User> authenticate(String presentedKey);
}
