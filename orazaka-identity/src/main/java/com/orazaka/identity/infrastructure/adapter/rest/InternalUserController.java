package com.orazaka.identity.infrastructure.adapter.rest;

import com.orazaka.identity.domain.model.RateLimitInfo;
import com.orazaka.identity.domain.model.User;
import com.orazaka.identity.domain.model.UserProfile;
import com.orazaka.identity.domain.ports.inbound.ApiKeyService;
import com.orazaka.identity.domain.ports.inbound.IdentityService;
import com.orazaka.identity.domain.ports.inbound.RateLimitProvider;
import com.orazaka.identity.domain.ports.inbound.TokenService;
import com.orazaka.identity.domain.ports.inbound.UserProfileProvider;
import com.orazaka.identity.infrastructure.adapter.rest.dto.InternalCredentialResponse;
import com.orazaka.identity.infrastructure.adapter.rest.dto.InternalUserResponse;
import com.orazaka.identity.infrastructure.adapter.rest.dto.TokenExchangeRequest;
import com.orazaka.identity.infrastructure.adapter.rest.dto.TokenExchangeResponse;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal service-to-service API of the identity context (Phase 2 cutover): downstream hosts
 * hydrate principals, profiles, tiers and BYOK credentials over HTTP once the identity library
 * leaves their process; the edge exchanges {@code oz_} API keys for session JWTs here.
 *
 * <p>{@code /internal/**} is not reachable through the edge route table (only {@code /api/**} flows
 * there) — LAN-local in this phase; per-service credentials arrive with the off-laptop hardening
 * pass.
 */
@RestController
@RequestMapping("/internal/v1")
class InternalUserController {

  private final IdentityService identityService;
  private final UserProfileProvider userProfileProvider;
  private final ApiKeyService apiKeyService;
  private final TokenService tokenService;
  private final RateLimitProvider rateLimitProvider;

  InternalUserController(
      IdentityService identityService,
      UserProfileProvider userProfileProvider,
      ApiKeyService apiKeyService,
      TokenService tokenService,
      RateLimitProvider rateLimitProvider) {
    this.identityService = Objects.requireNonNull(identityService, "IdentityService required");
    this.userProfileProvider =
        Objects.requireNonNull(userProfileProvider, "UserProfileProvider required");
    this.apiKeyService = Objects.requireNonNull(apiKeyService, "ApiKeyService required");
    this.tokenService = Objects.requireNonNull(tokenService, "TokenService required");
    this.rateLimitProvider =
        Objects.requireNonNull(rateLimitProvider, "RateLimitProvider required");
  }

  /** Full user snapshot for downstream principal hydration (404 via UserNotFoundException). */
  @GetMapping("/users/{id}")
  public InternalUserResponse getUser(@PathVariable String id) {
    return InternalUserResponse.from(identityService.getUser(id));
  }

  /** The user's profile for pipeline context assembly. */
  @GetMapping("/users/{id}/profile")
  public ResponseEntity<UserProfile> getProfile(@PathVariable String id) {
    UserProfile profile = userProfileProvider.getProfile(id);
    return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
  }

  /** The user's decrypted BYOK provider key (service-to-service only). */
  @GetMapping("/users/{id}/credentials/{provider}")
  public ResponseEntity<InternalCredentialResponse> getCredential(
      @PathVariable String id, @PathVariable String provider) {
    return identityService
        .getDecryptedApiKey(id, provider)
        .map(key -> ResponseEntity.ok(new InternalCredentialResponse(key)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /** Exchanges a valid {@code oz_} API key for a session JWT (performed by the edge). */
  @PostMapping("/tokens/exchange")
  public ResponseEntity<TokenExchangeResponse> exchange(@RequestBody TokenExchangeRequest request) {
    return apiKeyService
        .authenticate(request.apiKey())
        .filter(User::enabled)
        .map(user -> ResponseEntity.ok(new TokenExchangeResponse(tokenService.issue(user))))
        .orElseGet(() -> ResponseEntity.status(401).build());
  }

  /** The DB-flagged default rate-limit tier (anonymous callers). */
  @GetMapping("/tiers/default")
  public ResponseEntity<RateLimitInfo> getDefaultTier() {
    return rateLimitProvider
        .getDefaultTierKey()
        .flatMap(rateLimitProvider::getRateLimit)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /** Bucket parameters of a rate-limit tier. */
  @GetMapping("/tiers/{key}")
  public ResponseEntity<RateLimitInfo> getTier(@PathVariable String key) {
    return rateLimitProvider
        .getRateLimit(key)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
