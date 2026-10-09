package com.krizaka.users.persistence.infrastructure.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * JPA Entity mapping the {@code orazaka_user_model_prefs} table.
 *
 * <p>A user's OWN default model per capability {@code category}, overriding the global/admin
 * default ({@code orazaka_models.is_default}). {@code modelId} references {@code
 * orazaka_models(id)}; {@code voice} applies to {@code category = 'speech'}.
 */
@Entity
@Table(name = "orazaka_user_model_prefs")
@IdClass(UserModelPrefId.class)
public class UserModelPrefEntity {

  @Id
  @Column(name = "user_id", length = 255)
  private String userId;

  @Id
  @Column(name = "category", length = 50)
  private String category;

  @Column(name = "model_id")
  private Integer modelId;

  @Column(name = "voice", length = 50)
  private String voice;

  /** Default constructor required by JPA/Hibernate. */
  public UserModelPrefEntity() {
    /* JPA requires no-arg constructor */
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

  public Integer getModelId() {
    return modelId;
  }

  public void setModelId(Integer modelId) {
    this.modelId = modelId;
  }

  public String getVoice() {
    return voice;
  }

  public void setVoice(String voice) {
    this.voice = voice;
  }
}
