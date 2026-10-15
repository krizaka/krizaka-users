package com.krizaka.users.infrastructure.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.krizaka.users.domain.model.AuthenticatedApiKey;
import com.krizaka.users.domain.model.User;
import com.krizaka.users.domain.ports.inbound.ApiKeyService;
import com.krizaka.users.domain.ports.inbound.IdentityService;
import com.krizaka.users.domain.ports.inbound.RateLimitProvider;
import com.krizaka.users.domain.ports.inbound.TokenService;
import com.krizaka.users.domain.ports.inbound.UserProfileProvider;
import com.krizaka.users.infrastructure.adapter.rest.dto.TokenExchangeRequest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InternalUserControllerExchangeTest {

  private final ApiKeyService apiKeys = mock(ApiKeyService.class);
  private final TokenService tokens = mock(TokenService.class);
  private final InternalUserController controller =
      new InternalUserController(
          mock(IdentityService.class),
          mock(UserProfileProvider.class),
          apiKeys,
          tokens,
          mock(RateLimitProvider.class));

  private static User user() {
    return new User(
        UUID.randomUUID(), "svc", "svc@orazaka.com", true, Set.of("ROLE_USER"), Map.of());
  }

  @Test
  void aScopedKeyExchangesForAJwtAndItsScopesTheEdgeWillEnforce() {
    User u = user();
    when(apiKeys.authenticateKey("oz_live_k"))
        .thenReturn(Optional.of(new AuthenticatedApiKey(u, Set.of("ai:embed"))));
    when(tokens.issue(u)).thenReturn("jwt");

    var response = controller.exchange(new TokenExchangeRequest("oz_live_k"));

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody().token()).isEqualTo("jwt");
    assertThat(response.getBody().scopes()).containsExactly("ai:embed");
  }

  @Test
  void anUnscopedKeyAnswersNoScopes() {
    User u = user();
    when(apiKeys.authenticateKey("oz_live_k"))
        .thenReturn(Optional.of(new AuthenticatedApiKey(u, Set.of())));
    when(tokens.issue(u)).thenReturn("jwt");

    assertThat(controller.exchange(new TokenExchangeRequest("oz_live_k")).getBody().scopes())
        .isEmpty();
  }

  @Test
  void anInvalidOrRevokedKeyIsA401() {
    when(apiKeys.authenticateKey("oz_live_x")).thenReturn(Optional.empty());

    assertThat(controller.exchange(new TokenExchangeRequest("oz_live_x")).getStatusCode().value())
        .isEqualTo(401);
  }
}
