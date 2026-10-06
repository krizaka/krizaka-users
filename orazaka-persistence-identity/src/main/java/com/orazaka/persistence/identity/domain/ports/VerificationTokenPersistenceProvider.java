package com.orazaka.persistence.identity.domain.ports;

import com.orazaka.persistence.identity.domain.model.VerificationTokenDto;
import java.util.Optional;

/** Port interface for managing VerificationToken persistence operations. */
public interface VerificationTokenPersistenceProvider {

  VerificationTokenDto save(VerificationTokenDto tokenDto);

  Optional<VerificationTokenDto> findByTokenHashAndUsedFalse(String tokenHash);

  Optional<VerificationTokenDto> findById(String tokenId);

  void deleteById(String tokenId);
}
