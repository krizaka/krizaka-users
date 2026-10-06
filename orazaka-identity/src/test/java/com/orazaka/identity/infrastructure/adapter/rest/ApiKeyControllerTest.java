package com.orazaka.identity.infrastructure.adapter.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.orazaka.identity.domain.model.ApiKey;
import com.orazaka.identity.domain.model.GeneratedApiKey;
import com.orazaka.identity.domain.model.User;
import com.orazaka.identity.domain.ports.inbound.ApiKeyService;
import com.orazaka.identity.infrastructure.adapter.rest.dto.ApiKeyResponse;
import com.orazaka.identity.infrastructure.adapter.rest.dto.CreateApiKeyRequest;
import com.orazaka.identity.infrastructure.adapter.rest.dto.GeneratedApiKeyResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ApiKeyControllerTest {

  @Mock private ApiKeyService apiKeyService;

  private ApiKeyController controller;
  private User user;
  private String userId;

  @BeforeEach
  void setUp() {
    controller = new ApiKeyController(apiKeyService);
    user =
        new User(
            UUID.randomUUID(), "user", "user@example.com", true, Set.of("ROLE_USER"), Map.of());
    userId = user.id().toString();
  }

  @Test
  void list_returnsMappedResponses() {
    ApiKey apiKey = new ApiKey("key-1", userId, "CI", "oz_live_abcd", Instant.now(), null);
    when(apiKeyService.list(userId)).thenReturn(List.of(apiKey));

    ResponseEntity<List<ApiKeyResponse>> response = controller.list(user);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("CI", response.getBody().get(0).name());
    assertEquals("oz_live_abcd", response.getBody().get(0).keyPrefix());
  }

  @Test
  void create_returnsCreatedWithPlaintextOnce() {
    GeneratedApiKey generated =
        new GeneratedApiKey("key-1", "CI", "oz_live_abcd", "oz_live_abcdSECRET");
    when(apiKeyService.create(userId, "CI")).thenReturn(generated);

    ResponseEntity<GeneratedApiKeyResponse> response =
        controller.create(new CreateApiKeyRequest("CI"), user);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("oz_live_abcdSECRET", response.getBody().key());
  }

  @Test
  void revoke_returnsNoContentWhenRevoked() {
    when(apiKeyService.revoke(userId, "key-1")).thenReturn(true);

    ResponseEntity<Void> response = controller.revoke("key-1", user);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void revoke_returnsNotFoundWhenNothingMatched() {
    when(apiKeyService.revoke(userId, "key-x")).thenReturn(false);

    ResponseEntity<Void> response = controller.revoke("key-x", user);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
