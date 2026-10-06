package com.orazaka.persistence.identity.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clean domain DTO for a per-user, per-category default model preference (satisfying ERR-106).
 *
 * @param userId owning user id
 * @param category capability category (chat/image/video/speech/vision/audio)
 * @param modelId {@code orazaka_models.id} the user picked as their default for this category
 * @param voice optional voice for {@code category = 'speech'} (e.g. alloy, nova)
 */
public record UserModelPrefDto(String userId, String category, Integer modelId, String voice)
    implements Serializable {

  public UserModelPrefDto {
    Objects.requireNonNull(userId, "userId cannot be null");
    Objects.requireNonNull(category, "category cannot be null");
  }
}
