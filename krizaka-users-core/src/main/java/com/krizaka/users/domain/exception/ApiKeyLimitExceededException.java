package com.krizaka.users.domain.exception;

/** Exception thrown when a user attempts to create an API key beyond the per-user maximum. */
public class ApiKeyLimitExceededException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message The detail message.
   */
  public ApiKeyLimitExceededException(String message) {
    super(message);
  }
}
