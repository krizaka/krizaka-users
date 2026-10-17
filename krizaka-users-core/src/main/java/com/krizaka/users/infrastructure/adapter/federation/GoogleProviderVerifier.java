package com.krizaka.users.infrastructure.adapter.federation;

import com.krizaka.users.domain.model.ExtractedProfile;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * OAuth2 provider verifier for Google identity tokens.
 *
 * <p>Verification flow:
 *
 * <ol>
 *   <li>Decode the JWT payload and check the issuer ({@code accounts.google.com}) and expiry.
 *   <li>Validate the token server-side with Google's {@code tokeninfo} endpoint (signature).
 *   <li>Require the audience to be <b>our</b> client id — a token issued to any other application
 *       is refused (ADR-001, finding 2) — and the e-mail address to be verified by Google.
 *   <li>Extract {@code sub}, {@code email}, {@code name}, {@code picture}.
 * </ol>
 *
 * <p>Per §2.16 [ERR-120], all HTTP calls use Spring {@link RestClient}.
 */
class GoogleProviderVerifier extends AbstractProviderVerifier {

  private static final String GOOGLE_TOKENINFO_URL =
      "https://oauth2.googleapis.com/tokeninfo?id_token=";

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String clientId;

  GoogleProviderVerifier(RestClient restClient, ObjectMapper objectMapper, String clientId) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
    this.clientId = Objects.requireNonNull(clientId, "Google client id");
    logger.info("Google OAuth2 provider verifier initialized");
  }

  @Override
  protected String providerId() {
    return "google";
  }

  @Override
  protected String tokenLabel() {
    return "Google ID token";
  }

  @Override
  protected ExtractedProfile doVerifyAndExtract(String idToken) {
    JsonNode payload = decodeJwtPayload(idToken);

    String issuer = textOrNull(payload, "iss");
    if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
      throw new IllegalArgumentException("Invalid Google token issuer: " + issuer);
    }

    long exp = payload.has("exp") ? payload.get("exp").asLong() : 0;
    long now = System.currentTimeMillis() / 1000;
    if (exp < now) {
      throw new IllegalArgumentException(
          "Google ID token has expired (exp=" + exp + ", now=" + now + ")");
    }

    JsonNode tokenInfo = verifyViaTokenInfo(idToken);

    if (!clientId.equals(textOrNull(tokenInfo, "aud"))) {
      throw new IllegalArgumentException("Google ID token audience is not this application");
    }
    if (!"true".equals(textOrNull(tokenInfo, "email_verified"))) {
      throw new IllegalArgumentException("Google account e-mail address is not verified");
    }

    String sub = extractRequired(tokenInfo, "sub", "Google user ID");
    String email = extractRequired(tokenInfo, "email", "email");
    String name = textOrNull(tokenInfo, "name");
    String picture = textOrNull(tokenInfo, "picture");

    return new ExtractedProfile(email, sub, name, picture);
  }

  private JsonNode decodeJwtPayload(String jwt) {
    String[] parts = jwt.split("\\.");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid JWT format: expected at least 2 segments");
    }
    try {
      byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
      return objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to decode Google JWT payload", e);
    }
  }

  private JsonNode verifyViaTokenInfo(String idToken) {
    JsonNode tokenInfo =
        restClient
            .get()
            .uri(GOOGLE_TOKENINFO_URL + idToken)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(JsonNode.class);

    if (tokenInfo == null) {
      throw new IllegalArgumentException("Google tokeninfo API returned null response");
    }

    if (tokenInfo.has("error_description")) {
      throw new IllegalArgumentException(
          "Google token verification failed: " + tokenInfo.get("error_description").asString());
    }

    return tokenInfo;
  }

  private String extractRequired(JsonNode node, String field, String label) {
    String value = textOrNull(node, field);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Google token is missing required field: " + label);
    }
    return value;
  }
}
