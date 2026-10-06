package com.orazaka.identityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the Identity service — the trust root of the constellation
 * (MICROSERVICES_TARGET_ARCHITECTURE Phase 2): authentication, RBAC, user profile, credentials, API
 * keys, rate-limit tiers. Issues the claims-enriched session JWTs every other service validates
 * locally.
 *
 * <p>Hosts exactly the identity bounded context: the {@code orazaka-identity} library (domain,
 * ports, REST ingress, JWT issuance, event adapters) over {@code persistence-identity} — never the
 * cognitive core or the app persistence context (§2: layer ≠ process).
 */
@SpringBootApplication(
    scanBasePackages = {
      "com.orazaka.identityservice",
      "com.orazaka.identity",
      "com.orazaka.persistence.identity"
    })
@ConfigurationPropertiesScan(basePackages = {"com.orazaka.identityservice", "com.orazaka.identity"})
@EntityScan("com.orazaka.persistence.identity")
@EnableJpaRepositories("com.orazaka.persistence.identity")
@EnableScheduling
public class IdentityServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(IdentityServiceApplication.class, args);
  }
}
