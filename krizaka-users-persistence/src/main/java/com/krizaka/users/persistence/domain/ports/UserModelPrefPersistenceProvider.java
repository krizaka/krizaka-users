package com.krizaka.users.persistence.domain.ports;

import com.krizaka.users.persistence.domain.model.UserModelPrefDto;
import java.util.List;
import java.util.Optional;

/**
 * Inbound port for reading and writing a user's per-category default model preferences ({@code
 * user_model_prefs}). A {@code save} is an upsert on the composite {@code (userId, category)} key —
 * so a user can change their default freely.
 */
public interface UserModelPrefPersistenceProvider {

  /** All per-category default-model preferences for one user. */
  List<UserModelPrefDto> findByUserId(String userId);

  /** The user's default for a single capability category, if set. */
  Optional<UserModelPrefDto> find(String userId, String category);

  /** Upserts the user's default model/voice for one category. */
  UserModelPrefDto save(UserModelPrefDto pref);
}
