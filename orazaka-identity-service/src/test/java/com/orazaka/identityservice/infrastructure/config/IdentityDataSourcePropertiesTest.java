package com.orazaka.identityservice.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IdentityDataSourcePropertiesTest {

  @Test
  @DisplayName("Valid identity datasource wiring is accepted")
  void validWiring() {
    var properties =
        new IdentityDataSourceProperties(
            "jdbc:postgresql://localhost:5432/orazaka_identity_db", "orazaka_identity", "secret");
    assertThat(properties.url()).contains("orazaka_identity_db");
    assertThat(properties.username()).isEqualTo("orazaka_identity");
  }

  @Test
  @DisplayName("Missing values and non-postgres URLs are rejected")
  void invalidWiringRejected() {
    assertThatNullPointerException()
        .isThrownBy(() -> new IdentityDataSourceProperties(null, "u", "p"));
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new IdentityDataSourceProperties("jdbc:postgresql://localhost:5432/db", null, "p"));
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new IdentityDataSourceProperties("jdbc:postgresql://localhost:5432/db", "u", null));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new IdentityDataSourceProperties("jdbc:mysql://x/db", "u", "p"));
  }
}
