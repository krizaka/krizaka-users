package com.orazaka.persistence.identity.infrastructure.adapter.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key {@code (user_id, category)} for {@link UserModelPrefEntity}.
 *
 * <p>Field names mirror the entity's {@code @Id} fields, as required by {@code @IdClass}.
 */
public class UserModelPrefId implements Serializable {

  private String userId;
  private String category;

  /** Default constructor required by JPA/Hibernate. */
  public UserModelPrefId() {
    /* JPA requires no-arg constructor */
  }

  public UserModelPrefId(String userId, String category) {
    this.userId = userId;
    this.category = category;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserModelPrefId that)) {
      return false;
    }
    return Objects.equals(userId, that.userId) && Objects.equals(category, that.category);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, category);
  }
}
