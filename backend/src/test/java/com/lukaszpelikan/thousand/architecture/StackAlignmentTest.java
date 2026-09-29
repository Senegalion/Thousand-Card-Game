package com.lukaszpelikan.thousand.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lukaszpelikan.thousand.ThousandApplication;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StackAlignmentTest {

    private static final String ROOT = "com.lukaszpelikan.thousand";
    private static final int JAVA_25_CLASS_FILE_MAJOR_VERSION = 69;
    private static final Set<String> BUSINESS_MODULES = Set.of("identity", "user", "lobby", "match", "game");

    private static final JavaClasses MAIN_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT);

    @Test
    @DisplayName("AC-1: compiles and runs on Java 25")
    void ac1_compilesAndRunsOnJava25() throws IOException {
        // The runtime alone is not enough - a JDK 25 runtime happily runs Java 21 bytecode.
        assertThat(Runtime.version().feature()).isEqualTo(25);
        assertThat(classFileMajorVersion(ThousandApplication.class)).isEqualTo(JAVA_25_CLASS_FILE_MAJOR_VERSION);
    }

    @Test
    @DisplayName("AC-2: no class depends on lombok")
    void ac2_noClassDependsOnLombok() {
        // Lombok annotations are source-retention and leave no bytecode trace,
        // so the dependency itself must be absent from the classpath.
        assertThatThrownBy(() -> Class.forName("lombok.Data")).isInstanceOf(ClassNotFoundException.class);
        noClasses().should().dependOnClassesThat().resideInAPackage("lombok..").check(MAIN_CLASSES);
    }

    @Test
    @DisplayName("AC-3: every class resides in an allowed module")
    void ac3_everyClassResidesInAnAllowedModule() {
        // The Spring Boot entry point stays in the root package for component scanning.
        classes()
                .that()
                .doNotBelongToAnyOf(ThousandApplication.class)
                .should()
                .resideInAnyPackage(
                        ROOT + ".identity..",
                        ROOT + ".user..",
                        ROOT + ".lobby..",
                        ROOT + ".match..",
                        ROOT + ".game..",
                        ROOT + ".shared..")
                .check(MAIN_CLASSES);
        assertThat(topLevelModules()).containsAll(BUSINESS_MODULES);
    }

    private static Set<String> topLevelModules() {
        return MAIN_CLASSES.stream()
                .map(JavaClass::getPackageName)
                .filter(name -> name.startsWith(ROOT + "."))
                .map(name -> name.substring(ROOT.length() + 1).split("\\.")[0])
                .collect(Collectors.toSet());
    }

    private static int classFileMajorVersion(Class<?> type) throws IOException {
        String resource = "/" + type.getName().replace('.', '/') + ".class";
        try (InputStream raw = type.getResourceAsStream(resource);
                DataInputStream in = new DataInputStream(raw)) {
            in.readInt(); // magic
            in.readUnsignedShort(); // minor
            return in.readUnsignedShort();
        }
    }
}
