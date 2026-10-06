package com.orazaka.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.orazaka.identity.domain.model.User;
import com.orazaka.identity.infrastructure.config.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class TokenServiceImplTest {

  private static final String SECRET = "unit-test-identity-jwt-secret-256bit-key!";

  private final JwtProperties properties =
      new JwtProperties(SECRET, "orazaka-identity", Duration.ofMinutes(30));

  private final SecretKeySpec key =
      new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

  private final TokenServiceImpl service =
      new TokenServiceImpl(new NimbusJwtEncoder(new ImmutableSecret<>(key)), properties);

  @Test
  @DisplayName("Issues an HS256 JWT carrying sub, roles, tier, username and email claims")
  void issuesClaimsEnrichedToken() {
    UUID id = UUID.randomUUID();
    User user =
        new User(
            id,
            "testuser",
            "test@orazaka.com",
            true,
            Set.of("ROLE_ADMIN"),
            Map.of(),
            List.of(),
            "premium");

    String token = service.issue(user);

    Jwt jwt =
        NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build().decode(token);
    assertThat(jwt.getSubject()).isEqualTo(id.toString());
    assertThat(jwt.getClaimAsString("username")).isEqualTo("testuser");
    assertThat(jwt.getClaimAsString("email")).isEqualTo("test@orazaka.com");
    assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ROLE_ADMIN");
    assertThat(jwt.getClaimAsString("tier")).isEqualTo("premium");
    assertThat(jwt.getClaimAsString("iss")).isEqualTo("orazaka-identity");
    assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
  }

  @Test
  @DisplayName("Omits the tier claim when the user has no tier")
  void omitsTierWhenAbsent() {
    User user =
        new User(UUID.randomUUID(), "u", "u@orazaka.com", true, Set.of("ROLE_USER"), Map.of());

    Jwt jwt =
        NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
            .decode(service.issue(user));
    assertThat(jwt.getClaims()).doesNotContainKey("tier");
  }

  @Test
  @DisplayName("Rejects a null user")
  void rejectsNullUser() {
    assertThrows(NullPointerException.class, () -> service.issue(null));
  }
}
