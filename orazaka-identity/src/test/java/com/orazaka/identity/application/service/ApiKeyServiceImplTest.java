package com.orazaka.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orazaka.identity.domain.exception.ApiKeyLimitExceededException;
import com.orazaka.identity.domain.exception.InvalidRequestException;
import com.orazaka.identity.domain.model.ApiKey;
import com.orazaka.identity.domain.model.GeneratedApiKey;
import com.orazaka.identity.domain.model.User;
import com.orazaka.identity.domain.ports.inbound.ApiKeyService;
import com.orazaka.identity.domain.ports.outbound.ApiKeyRepositoryPort;
import com.orazaka.identity.domain.ports.outbound.CryptographyPort;
import com.orazaka.identity.domain.ports.outbound.UserRepositoryPort;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceImplTest {

  private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440001";

  @Mock private ApiKeyRepositoryPort apiKeyRepository;
  @Mock private UserRepositoryPort userRepository;
  @Mock private CryptographyPort cryptography;

  @InjectMocks private ApiKeyServiceImpl service;

  private User enabledUser() {
    return new User(
        UUID.fromString(USER_ID), "user", "user@example.com", true, Set.of("ROLE_USER"), Map.of());
  }

  @Test
  @DisplayName(
      "create generates an oz_live_ secret, persists only its hash, and returns plaintext once")
  void createHappyPath() {
    when(apiKeyRepository.countByUserId(USER_ID)).thenReturn(3L);
    when(cryptography.hashToken(anyString())).thenReturn("hashed");

    GeneratedApiKey generated = service.create(USER_ID, "  CI pipeline  ");

    assertThat(generated.name()).isEqualTo("CI pipeline");
    assertThat(generated.plaintext()).startsWith("oz_live_");
    assertThat(generated.keyPrefix()).hasSize(12).isEqualTo(generated.plaintext().substring(0, 12));
    verify(apiKeyRepository)
        .save(anyString(), eq(USER_ID), eq("CI pipeline"), eq(generated.keyPrefix()), eq("hashed"));
  }

  @Test
  @DisplayName("create rejects a blank name before touching the repository")
  void createBlankName() {
    assertThatThrownBy(() -> service.create(USER_ID, "   "))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessageContaining("must not be blank");
    verify(apiKeyRepository, never())
        .save(anyString(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("create throws when the per-user maximum is reached")
  void createLimitReached() {
    when(apiKeyRepository.countByUserId(USER_ID))
        .thenReturn((long) ApiKeyService.MAX_KEYS_PER_USER);

    assertThatThrownBy(() -> service.create(USER_ID, "one more"))
        .isInstanceOf(ApiKeyLimitExceededException.class);
    verify(apiKeyRepository, never())
        .save(anyString(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("revoke reports success when a row is deleted")
  void revokeDeletes() {
    when(apiKeyRepository.deleteByIdAndUserId("key-1", USER_ID)).thenReturn(1L);
    assertThat(service.revoke(USER_ID, "key-1")).isTrue();
  }

  @Test
  @DisplayName("revoke reports failure when nothing matched the owner")
  void revokeNoMatch() {
    when(apiKeyRepository.deleteByIdAndUserId("key-x", USER_ID)).thenReturn(0L);
    assertThat(service.revoke(USER_ID, "key-x")).isFalse();
  }

  @Test
  @DisplayName("authenticate ignores tokens without the oz_ prefix without hashing or lookups")
  void authenticateNonApiKeyToken() {
    assertThat(service.authenticate("some-session-uuid")).isEmpty();
    verify(cryptography, never()).hashToken(anyString());
    verify(apiKeyRepository, never()).findByKeyHash(anyString());
  }

  @Test
  @DisplayName("authenticate resolves a valid key to its enabled owner and stamps last-used")
  void authenticateValid() {
    ApiKey match = new ApiKey("key-1", USER_ID, "CI", "oz_live_abcd", Instant.now(), null);
    when(cryptography.hashToken("oz_live_secret")).thenReturn("h");
    when(apiKeyRepository.findByKeyHash("h")).thenReturn(Optional.of(match));
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(enabledUser()));

    Optional<User> result = service.authenticate("oz_live_secret");

    assertThat(result).isPresent();
    assertThat(result.get().id()).hasToString(USER_ID);
    verify(apiKeyRepository).touchLastUsed(eq("key-1"), any(Instant.class));
  }

  @Test
  @DisplayName("authenticate rejects a disabled owner and does not stamp last-used")
  void authenticateDisabledUser() {
    ApiKey match = new ApiKey("key-1", USER_ID, "CI", "oz_live_abcd", Instant.now(), null);
    User disabled =
        new User(
            UUID.fromString(USER_ID),
            "user",
            "user@example.com",
            false,
            Set.of("ROLE_USER"),
            Map.of());
    when(cryptography.hashToken("oz_live_secret")).thenReturn("h");
    when(apiKeyRepository.findByKeyHash("h")).thenReturn(Optional.of(match));
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(disabled));

    assertThat(service.authenticate("oz_live_secret")).isEmpty();
    verify(apiKeyRepository, never()).touchLastUsed(anyString(), any(Instant.class));
  }

  @Test
  @DisplayName("authenticate returns empty when the key hash matches nothing")
  void authenticateUnknownHash() {
    when(cryptography.hashToken("oz_live_secret")).thenReturn("h");
    when(apiKeyRepository.findByKeyHash("h")).thenReturn(Optional.empty());

    assertThat(service.authenticate("oz_live_secret")).isEmpty();
  }
}
