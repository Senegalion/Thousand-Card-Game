package com.lukaszpelikan.thousand.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import java.util.List;
import java.util.Map;

/** Module-boundary rules from pattern.md and ADR-006, parameterized by root package so they can be tested on fixtures. */
final class ModuleRules {

    /**
     * Module-relative packages each module may depend on besides itself. {@code shared} (including
     * {@code shared.infrastructure}) is common technical infrastructure and must not depend on any module.
     */
    private static final Map<String, List<String>> ALLOWED_DEPENDENCIES = Map.of(
            "game", List.of(),
            "identity", List.of("shared"),
            "lobby", List.of("shared", "identity.api"),
            "match", List.of("shared", "game", "lobby.api"),
            "user", List.of("shared", "identity.api", "lobby.api", "match.api"),
            "shared", List.of());

    private static final List<String> BUSINESS_MODULES = List.of("identity", "user", "lobby", "match", "game");

    private final String root;

    ModuleRules(String root) {
        this.root = root;
    }

    ArchRule gameDependsOnlyOnJavaAndGame() {
        return classes()
                .that()
                .resideInAPackage(pkg("game"))
                .should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage("java..", pkg("game"))
                .allowEmptyShould(true);
    }

    ArchRule modulesDependOnlyInAllowedDirections() {
        List<ArchRule> rules = ALLOWED_DEPENDENCIES.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String[] allowed = entry.getValue().stream().map(this::pkg).toArray(String[]::new);
                    return noClasses()
                            .that()
                            .resideInAPackage(pkg(entry.getKey()))
                            .should()
                            .dependOnClassesThat(resideInAPackage(root + "..")
                                    .and(not(resideInAPackage(pkg(entry.getKey()))))
                                    .and(not(resideInAnyPackage(allowed))))
                            .allowEmptyShould(true);
                })
                .toList();
        return CompositeArchRule.of(rules);
    }

    ArchRule noAccessToOtherModulesInfrastructure() {
        List<ArchRule> rules = BUSINESS_MODULES.stream()
                .map(module -> noClasses()
                        .that()
                        .resideOutsideOfPackage(pkg(module))
                        .should()
                        .dependOnClassesThat()
                        .resideInAPackage(pkg(module + ".infrastructure"))
                        .allowEmptyShould(true))
                .toList();
        return CompositeArchRule.of(rules);
    }

    private String pkg(String relativePackage) {
        return root + "." + relativePackage + "..";
    }
}
