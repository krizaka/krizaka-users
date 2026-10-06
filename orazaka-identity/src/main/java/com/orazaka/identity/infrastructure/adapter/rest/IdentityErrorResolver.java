package com.orazaka.identity.infrastructure.adapter.rest;

import com.orazaka.identity.domain.exception.ApiKeyLimitExceededException;
import com.orazaka.identity.domain.exception.InvalidRequestException;
import com.orazaka.identity.domain.exception.UserAlreadyExistsException;
import com.orazaka.identity.domain.exception.UserNotFoundException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps identity domain exceptions to HTTP responses. Lives with the identity context so every host
 * (identity service, and the router during the dual-run window) renders identical errors — the
 * advice applies context-wide, including to non-identity controllers that raise {@link
 * InvalidRequestException}.
 */
@RestControllerAdvice
public class IdentityErrorResolver {

  private static final Logger logger = LoggerFactory.getLogger(IdentityErrorResolver.class);

  /** Maps a duplicate registration to 409 Conflict. */
  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<Map<String, String>> handleUserAlreadyExists(
      UserAlreadyExistsException ex) {
    return errorResponse(HttpStatus.CONFLICT, ex);
  }

  /** Maps an exhausted API key allowance to 409 Conflict. */
  @ExceptionHandler(ApiKeyLimitExceededException.class)
  public ResponseEntity<Map<String, String>> handleApiKeyLimitExceeded(
      ApiKeyLimitExceededException ex) {
    return errorResponse(HttpStatus.CONFLICT, ex);
  }

  /** Maps an invalid request to 400 Bad Request. */
  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Map<String, String>> handleInvalidRequest(InvalidRequestException ex) {
    return errorResponse(HttpStatus.BAD_REQUEST, ex);
  }

  /** Maps an unknown user to 404 Not Found (internal user-directory lookups). */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
    return errorResponse(HttpStatus.NOT_FOUND, ex);
  }

  private ResponseEntity<Map<String, String>> errorResponse(
      HttpStatus status, RuntimeException ex) {
    logger.warn("Identity request rejected ({}): {}", status.value(), ex.getMessage());
    return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
  }
}
