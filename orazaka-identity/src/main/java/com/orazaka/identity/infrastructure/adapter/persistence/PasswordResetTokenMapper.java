package com.orazaka.identity.infrastructure.adapter.persistence;

import com.orazaka.identity.domain.model.PasswordResetToken;
import com.orazaka.persistence.identity.domain.model.PasswordResetTokenDto;

/**
 * Package-private static mapper for password reset token domain/DTO conversions (ERR-107).
 *
 * <p>Maps between the identity domain {@link PasswordResetToken} and the persistence layer {@link
 * PasswordResetTokenDto}. All mapping logic is isolated here.
 */
final class PasswordResetTokenMapper {

  private PasswordResetTokenMapper() {}

  /**
   * Converts a domain record to a persistence DTO.
   *
   * @param domain The domain record.
   * @return The persistence DTO.
   */
  static PasswordResetTokenDto toDto(PasswordResetToken domain) {
    return new PasswordResetTokenDto(
        domain.id(), domain.email(), domain.tokenHash(), domain.expiresAt(), null);
  }

  /**
   * Converts a persistence DTO to a domain record.
   *
   * @param dto The persistence DTO.
   * @return The domain record.
   */
  static PasswordResetToken toDomain(PasswordResetTokenDto dto) {
    return new PasswordResetToken(dto.id(), dto.email(), dto.tokenHash(), dto.expiresAt());
  }
}
