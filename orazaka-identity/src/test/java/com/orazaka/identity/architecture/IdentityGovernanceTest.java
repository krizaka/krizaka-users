package com.orazaka.identity.architecture;

import static com.orazaka.test.architecture.GovernanceRules.*;

import com.orazaka.test.architecture.RunSurfaceRules;
import com.orazaka.test.architecture.SourceFileScanner;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Governance (naming + one-package-one-kind) for the orazaka-identity module, reusing the shared
 * {@link com.orazaka.test.architecture.GovernanceRules}.
 *
 * <p>Identity uses the port/impl split: inbound ports live in {@code domain.ports.inbound} and
 * their implementations in {@code application.service} as package-private {@code
 * *ServiceImpl}/{@code *ProviderImpl}. The router's "service package = *Service only" rule
 * therefore does not apply; impl package-privacy is enforced by {@code IdentityBoundaryTest}
 * ([ERR-105/106]).
 */
class IdentityGovernanceTest {

  private static final String PKG = "com.orazaka.identity";
  private static JavaClasses identityClasses;

  @BeforeAll
  static void importClasses() {
    identityClasses = importProductionClasses(PKG);
  }

  @Test
  @DisplayName("[DOOR-001] no inbound HTTP entry dispatches a job")
  void noInboundEntryDispatchesAJob() {
    RunSurfaceRules.assertNoInboundEntryDispatchesAJob();
  }

  @Test
  @DisplayName("[ERR-103] One top-level class per file")
  void oneClassPerFile() {
    assertOneTopLevelClassPerFile(identityClasses, PKG);
  }

  @Test
  @DisplayName("[ERR-104] No redundant 'Orazaka' prefix")
  void noRedundantPrefix() {
    assertNoRedundantPrefix(identityClasses, PKG);
  }

  @Test
  @DisplayName("[ERR-107] Mapper classes are final and package-private")
  void mappersAreFinalAndPackagePrivate() {
    assertMappersFinal(identityClasses, PKG);
    assertMappersPackagePrivate(identityClasses, PKG);
  }

  @Test
  @DisplayName("[ERR-112] Web controllers only in the segregated REST ingress adapter")
  void webControllersOnlyInRestAdapter() {
    // Strangler-fig Phase 2: the identity context owns its REST ingress
    // (auth/profile/api-keys/credentials) — hosted by the identity service and,
    // during the dual-run window, by the router. Controllers stay segregated in
    // infrastructure.adapter.rest per ERR-112; anywhere else remains banned.
    com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
        .that()
        .areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
        .should()
        .resideInAPackage(PKG + ".infrastructure.adapter.rest..")
        .because("The identity REST ingress is segregated in its adapter package [ERR-112]")
        .check(identityClasses);
  }

  @Test
  @DisplayName("[ERR-130] Domain holds no transport DTOs (*Request/*Response)")
  void domainHasNoTransportDtos() {
    assertDomainHasNoTransportDtos(identityClasses, PKG + ".domain");
  }

  @Test
  @DisplayName("[HEX-002] Domain layer is framework-free (no Spring/JPA/Jackson)")
  void domainPurity() {
    assertDomainPurity(identityClasses, PKG);
  }

  // GOV-006: [ERR-130] support-package hygiene is not invoked here: this module has no
  // infrastructure.support package, so the rule inspected no class.

  @Test
  @DisplayName("[ERR-130] adapter/persistence holds only *Adapter/*Mapper")
  void persistenceAdapterPackageKind() {
    assertPersistenceAdapterPackageKind(identityClasses, PKG);
  }

  @Test
  @DisplayName("[ADR-009] Instance fields in concrete classes are private")
  void fieldsArePrivate() {
    assertFieldsPrivate(identityClasses);
  }

  @Test
  @DisplayName("[ADR-007] Collection fields are private final")
  void collectionFieldsPrivateFinal() {
    assertCollectionFieldsPrivateFinal(identityClasses);
  }

  @Test
  @DisplayName("[GOV-001] No anonymous classes in production")
  void noAnonymousClasses() {
    assertNoAnonymousClasses(identityClasses, PKG);
  }

  @Test
  @DisplayName("[GOV-004] No standard streams (use SLF4J)")
  void noStandardStreams() {
    assertNoStandardStreams(identityClasses);
  }

  @Test
  @DisplayName("[ERR-113] No Environment injection in production beans")
  void noEnvironmentInjection() {
    SourceFileScanner.assertNoEnvironmentInjection(Path.of("src", "main", "java"));
  }
}
