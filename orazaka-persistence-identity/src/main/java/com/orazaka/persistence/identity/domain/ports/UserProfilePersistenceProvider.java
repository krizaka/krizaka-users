package com.orazaka.persistence.identity.domain.ports;

import com.orazaka.persistence.identity.domain.model.UserProfileDto;
import java.util.Optional;

/** Port interface for managing UserProfile persistence operations. */
public interface UserProfilePersistenceProvider {

  Optional<UserProfileDto> findByUserId(String userId);

  UserProfileDto save(UserProfileDto profileDto);
}
