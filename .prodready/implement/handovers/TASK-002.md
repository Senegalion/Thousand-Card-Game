# TASK-002 Handoff

## Task
- Title: Architecture rules: module boundaries and pure game domain
- Status: Done
- Completed at: 2026-09-29

## Acceptance Criteria Covered
- AC-1: Every class in `game` depends only on `java..` and `game..` (no Spring, no `shared`)
- AC-2: Module dependencies follow only the allowed directions from pattern.md
- AC-3: No class accesses another module's `infrastructure` package

## Canonical Tests
All in `backend/src/test/java/com/lukaszpelikan/thousand/architecture/ModuleBoundariesTest.java`. Each test checks the rule against production code and against a fixture module tree, asserting the exact set of violating fixture classes:
- `AC-1: game depends only on java and game`: flags `game.domain.SpringAwareCard` (Spring) and `game.domain.GameUsingShared` (`shared`)
- `AC-2: modules depend only in allowed directions`: flags `GameUsingShared`, `IdentityUsingUser`, `LobbyUsingMatch`, `MatchUsingLobbyDomain`, `UserUsingLobbyInfrastructure`, `SharedUsingLobby`
- `AC-3: no module accesses another module's infrastructure`: flags `MatchUsingGameInfrastructure`, `UserUsingLobbyInfrastructure`

## Files Changed
- `backend/src/test/java/com/lukaszpelikan/thousand/architecture/ModuleBoundariesTest.java` (new)
- `backend/src/test/java/com/lukaszpelikan/thousand/architecture/ModuleRules.java` (new): the rules, parameterized by root package
- `backend/src/test/java/com/lukaszpelikan/thousand/architecture/fixture/**` (new, 21 files): test-only module tree with compliant and violating classes
- `.claude/hooks/test-after-edit.sh`: selects JDK 25 via `/usr/libexec/java_home -v 25` before `./mvnw test`
- `.prodready/plan/backlog.md`

## Commands Run
- RED: `./mvnw -B test -Dtest=ModuleBoundariesTest` (host, JDK 25): 3 errors, `UnsupportedOperationException: not implemented`
- GREEN: `./mvnw -B test -Dtest=ModuleBoundariesTest`: 3/3 pass
- `./mvnw -B verify`: BUILD SUCCESS, 6/6 tests, Spotless clean (after `spotless:apply` on `ModuleRules.java` only)
- Hook: run outside the sandbox with an inherited `JAVA_HOME=openjdk@21`; selected Temurin 25.0.2, tests passed, no block

## Decisions
Rules were reviewed and accepted before implementation.

| Module | May depend on (besides itself and external libraries) |
|---|---|
| `game` | nothing (JDK only) |
| `identity` | `shared` |
| `lobby` | `shared`, `identity.api` |
| `match` | `shared`, `game` (whole module), `lobby.api` |
| `user` | `shared`, `identity.api`, `lobby.api`, `match.api` |
| `shared` | no module |

- `shared.infrastructure` is common technical infrastructure usable by all modules except `game`. It must not contain business logic. AC-3 therefore covers only the business modules' `infrastructure` packages.
- `identity` stays isolated (only `shared`).
- `match → game` is allowed for the whole module by AC-2, but `match → game.infrastructure` is still rejected by AC-3.
- `ranking` is not in the rule map; add it when the module is created. Until then, any dependency on it from another module fails AC-2.
- `ThousandApplication` (root package) is not a module and is not constrained.
- Production rules use `allowEmptyShould(true)` because the modules have no classes yet. The fixtures prove the rules are not vacuous.
- The empty `game/api|application|infrastructure` placeholders were left unchanged by agreement. AC-1 applies to the whole `game` module, so nothing framework-dependent can live there anyway.

## Follow-ups or Blockers
- The TASK-001 follow-up about the hook running on JDK 21 is resolved on macOS. On other platforms the hook falls back to the inherited `JAVA_HOME`.
- Inside the Claude Code Bash sandbox, `/usr/libexec/java_home` finds no JDK, so `.zshrc` falls back to openjdk@21. Run Maven there with `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home`.
- Decide later whether to remove the empty `game/api|application|infrastructure` placeholders (pattern.md: `game` is a pure domain).

## Next Recommended Task
- TASK-016: Game domain: cards, deck, strength, points, marriage values
