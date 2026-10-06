package com.orazaka.identity.domain.ports.inbound;

import com.orazaka.identity.domain.model.User;

/**
 * Port interface defining the OAuth2 identity federation and reconciliation contract.
 *
 * <p>The router binds exclusively to this interface via constructor injection. The concrete
 * implementation ({@code IdentityReconciliationServiceImpl}) is package-private within {@code
 * orazaka-identity} and discovered by Spring component scanning.
 *
 * @see com.orazaka.identity.federation.OAuth2ProviderVerifier
 * @see com.orazaka.identity.domain.ExtractedProfile
 */
public interface IdentityReconciliationService {

  /**
   * Verifies an external identity token and reconciles the user against the local database.
   *
   * <p>Delegates token verification to the matching provider verifier, then either resolves the
   * existing user or provisions a new federated user record with no password.
   *
   * @param provider The provider identifier (e.g., "google", "github").
   * @param idToken The raw identity token issued by the external provider.
   * @return The fully resolved domain {@link User} record.
   * @throws IllegalArgumentException if no verifier supports the given provider, or if the token is
   *     invalid.
   */
  User reconcile(String provider, String idToken);
}
