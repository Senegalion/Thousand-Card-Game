# TASK-001 Handoff

## Task
- Title: Align backend skeleton with accepted stack and modules
- Status: Done
- Completed at: 2026-09-29

## Acceptance Criteria Covered
- AC-1: Java specification version is 25 (runtime) and main classes are compiled to Java 25 bytecode
- AC-2: No class depends on `lombok` (Lombok removed from the build and classpath)
- AC-3: Every class resides in identity, user, lobby, match, game or shared

## Canonical Tests
All in `backend/src/test/java/com/lukaszpelikan/thousand/architecture/StackAlignmentTest.java`:
- `AC-1: compiles and runs on Java 25`: `Runtime.version().feature() == 25` and class-file major version of `ThousandApplication` == 69
- `AC-2: no class depends on lombok`: `lombok.Data` not loadable + ArchUnit `noClasses().dependOnClassesThat().resideInAPackage("lombok..")`
- `AC-3: every class resides in an allowed module`: ArchUnit package rule + all five business modules (identity, user, lobby, match, game) present

## Files Changed
- `backend/pom.xml`: `java.version` 21 → 25; Lombok dependency, boot-plugin exclude and compiler annotation-processor config removed; ArchUnit `archunit-junit5` 1.4.2 (test) added
- `backend/src/main/java/com/lukaszpelikan/thousand/room/` → `lobby/` (git mv)
- `backend/src/main/java/com/lukaszpelikan/thousand/player/` → `user/` (git mv)
- `backend/src/main/java/com/lukaszpelikan/thousand/{identity,user,lobby,match,game,shared}/package-info.java`: module descriptions (new)
- `backend/src/test/java/com/lukaszpelikan/thousand/architecture/StackAlignmentTest.java` (new)
- `.prodready/plan/backlog.md`

## Commands Run
- RED: `docker compose run --rm --no-deps backend ./mvnw -B -q test -Dtest=StackAlignmentTest`: 3 failures (bytecode 65, lombok on classpath, no module classes)
- GREEN: `docker compose run --rm --no-deps backend sh -c "./mvnw -B -q spotless:apply && ./mvnw -B verify"`: BUILD SUCCESS, 3/3 tests, 0 Checkstyle violations, Spotless clean

## Decisions
- AC-1 also checks bytecode version: the dev container is JDK 25, so a runtime-only check would pass even with `java.version=21`.
- AC-2 also checks the classpath: Lombok annotations are source-retention, so an ArchUnit bytecode rule alone can never fail.
- AC-3 exempts `ThousandApplication`: the Spring Boot entry point stays in the root package so component scanning covers every module. AC-3 also asserts that the five business modules exist, so the rule cannot pass vacuously on an empty skeleton.
- Modules are marked with `package-info.java` (not speculative layers); identity and match have no sub-packages yet.
- ArchUnit 1.4.2 (tech-stack.md pins 1.4.x); it reads Java 25 class files fine.
- The existing `game/api|application|infrastructure` placeholders were left alone. Splitting orchestration from `game` into `match` and enforcing purity belong to TASK-002 and later tasks.

## Follow-ups or Blockers
- Host JDK is 21. The Claude hook `.claude/hooks/test-after-edit.sh` runs `./mvnw test` on the host and now fails with "release 25 not supported". Fix by installing JDK 25 on the host or by running the hook through `docker compose run --rm --no-deps backend ./mvnw test`.
- Empty untracked `thousand-backend/` directory at repo root; unrelated, left untouched.

## Next Recommended Task
- TASK-002: Architecture rules: module boundaries and pure game domain
