package com.orazaka.identityservice.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataSourceConfigTest {

  @Test
  @DisplayName("The pool is built from the identity properties, never from spring.datasource")
  void poolBuiltFromIdentityProperties() {
    var properties =
        new IdentityDataSourceProperties(
            "jdbc:postgresql://localhost:5432/orazaka_identity_db", "orazaka_identity", "secret");

    try (HikariDataSource dataSource =
        (HikariDataSource) new DataSourceConfig().identityDataSource(properties)) {
      assertThat(dataSource.getJdbcUrl()).isEqualTo(properties.url());
      assertThat(dataSource.getUsername()).isEqualTo("orazaka_identity");
      assertThat(dataSource.getMaximumPoolSize()).isEqualTo(5);
    }
  }
}
