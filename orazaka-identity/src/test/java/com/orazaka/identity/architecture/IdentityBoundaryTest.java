package com.orazaka.identity.architecture;

import static com.orazaka.test.TestConstants.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ArchUnit boundary enforcement for the orazaka-identity module.
 *
 * <p>Validates that identity remains a pure domain hexagon with no outward dependencies on router,
 * core, or tools modules (ERR-102, §1.A).
 */
class IdentityBoundaryTest {

  private static JavaClasses identityClasses;

  @BeforeAll
  static void importClasses() {
    identityClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.orazaka.identity");
  }

  @Test
  @DisplayName("[ERR-102] Identity must not depend on router")
  void identityDoesNotDependOnRouter() {
    noClasses()
        .that()
        .resideInAPackage(PKG_IDENTITY)
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.orazaka.conversationservice..")
        .because("Identity is a pure domain hexagon — router imports are forbidden [ERR-102]")
        .check(identityClasses);
  }

  @Test
  @DisplayName("[ERR-102] Identity must not depend on core")
  void identityDoesNotDependOnCore() {
    noClasses()
        .that()
        .resideInAPackage(PKG_IDENTITY)
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.orazaka.core..")
        .because("Identity must remain decoupled from the AI core engine [ERR-102]")
        .check(identityClasses);
  }

  @Test
  @DisplayName("[ERR-102] Identity must not depend on tools")
  void identityDoesNotDependOnTools() {
    noClasses()
        .that()
        .resideInAPackage(PKG_IDENTITY)
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.orazaka.tools..")
        .because("Identity must remain decoupled from the tools module [ERR-102]")
        .check(identityClasses);
  }

  @Test
  @DisplayName("[ERR-106] No field injection in identity classes")
  void noFieldInjection() {
    noFields()
        .that()
        .areDeclaredInClassesThat()
        .resideInAPackage(PKG_IDENTITY)
        .should()
        .beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
        .because("Field injection is prohibited — constructor-based DI is mandatory [ADR-012]")
        .check(identityClasses);
  }

  @Test
  @DisplayName("[ERR-102] Service implementations must not be public")
  void serviceImplNotPublic() {
    classes()
        .that()
        .resideInAPackage("com.orazaka.identity.application.service..")
        .and()
        .haveSimpleNameEndingWith("Impl")
        .should()
        .notBePublic()
        .because(
            "Service implementations are package-private — only interfaces are public [Ports & Adapters]")
        .check(identityClasses);
  }
}
