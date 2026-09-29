package com.lukaszpelikan.thousand.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Each rule is checked against production code and against {@code fixture}, a test-only module tree with known
 * compliant and violating classes, so a rule cannot pass vacuously while production modules are still empty.
 */
class ModuleBoundariesTest {

    private static final String ROOT = "com.lukaszpelikan.thousand";
    private static final String FIXTURE_ROOT = ROOT + ".architecture.fixture";

    private static final JavaClasses PRODUCTION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT);
    private static final JavaClasses FIXTURE = new ClassFileImporter().importPackages(FIXTURE_ROOT);

    private static final ModuleRules PRODUCTION_RULES = new ModuleRules(ROOT);
    private static final ModuleRules FIXTURE_RULES = new ModuleRules(FIXTURE_ROOT);

    @Test
    @DisplayName("AC-1: game depends only on java and game")
    void ac1_gameDependsOnlyOnJavaAndGame() {
        PRODUCTION_RULES.gameDependsOnlyOnJavaAndGame().check(PRODUCTION);
        assertThat(violators(FIXTURE_RULES.gameDependsOnlyOnJavaAndGame()))
                .containsExactlyInAnyOrder("game.domain.SpringAwareCard", "game.domain.GameUsingShared");
    }

    @Test
    @DisplayName("AC-2: modules depend only in allowed directions")
    void ac2_modulesDependOnlyInAllowedDirections() {
        PRODUCTION_RULES.modulesDependOnlyInAllowedDirections().check(PRODUCTION);
        assertThat(violators(FIXTURE_RULES.modulesDependOnlyInAllowedDirections()))
                .containsExactlyInAnyOrder(
                        "game.domain.GameUsingShared",
                        "identity.application.IdentityUsingUser",
                        "lobby.application.LobbyUsingMatch",
                        "match.application.MatchUsingLobbyDomain",
                        "user.application.UserUsingLobbyInfrastructure",
                        "shared.SharedUsingLobby");
    }

    @Test
    @DisplayName("AC-3: no module accesses another module's infrastructure")
    void ac3_noModuleAccessesAnotherModulesInfrastructure() {
        PRODUCTION_RULES.noAccessToOtherModulesInfrastructure().check(PRODUCTION);
        assertThat(violators(FIXTURE_RULES.noAccessToOtherModulesInfrastructure()))
                .containsExactlyInAnyOrder(
                        "match.application.MatchUsingGameInfrastructure",
                        "user.application.UserUsingLobbyInfrastructure");
    }

    /** Fixture classes (relative to {@link #FIXTURE_ROOT}) reported as the origin of a violation. */
    private static Set<String> violators(ArchRule rule) {
        EvaluationResult result = rule.evaluate(FIXTURE);
        return FIXTURE.stream()
                .map(JavaClass::getName)
                .filter(name -> result.getFailureReport().getDetails().stream()
                        .anyMatch(detail -> detail.startsWith("Class <" + name + ">")
                                || detail.startsWith("Field <" + name + ".")
                                || detail.startsWith("Constructor <" + name + ".")
                                || detail.startsWith("Method <" + name + ".")))
                .map(name -> name.substring(FIXTURE_ROOT.length() + 1))
                .collect(Collectors.toSet());
    }
}
