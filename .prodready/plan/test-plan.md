# Test Plan

## Testing Strategy

### Acceptance Criteria TDD
- Every task-scoped `AC-N` in `backlog.md` has **exactly one** canonical acceptance test. The test name starts with `AC-N: ` followed by the criterion text or a concise equivalent. Numbering is per task.
- A story-linked task AC (for example TASK-012 AC-2 = US-001 AC-2) is covered by that **one** canonical test. The traceability table below records both IDs, so each story AC also has exactly one canonical test.
- `Write failing canonical test for AC-N` must be completed and **RED-confirmed** (it executes and fails; skipped or pending doesn't count) before `Implement AC-N (red→green)` starts. The `Red Confirmed` column is updated during Implement.
- Additional technical tests are encouraged, but they must not use the `AC-N: ` prefix.

### Special AC types
| Type | Canonical test | RED | GREEN |
|---|---|---|---|
| **Verification** (TASK-005..TASK-009, owner) | A method per item in `RulesetVerificationTest` (from the TASK-004 harness), which reads `.prodready/define/ruleset.md` | The item is still `CONFIRM` | The owner records the observed Kurnik behavior and evidence (`docs/kurnik-evidence/<ID>/`) and sets the item to `VERIFIED` |
| **Kurnik option** (TASK-026, 034..040) | Domain test in the named file | Written only **after** the AC text has been updated with the VERIFIED behavior (a spec update). It must never assume a behavior. | Engine implements the verified behavior |
| **Timeout fallback** (TASK-041/042) | `game.timeout` tests | As usual | The ADR-009 implementation detail. Tests must not describe it as a rule. |
| **Ops** (TASK-015, 049..052, 089..093) | Vitest (Node) checks in `tests/ops/` against the workflow files and the deployed endpoints, plus the k6 script for load | As usual | As usual |

### Test Pyramid
```
        /\
       /  \   E2E: Playwright (invite link, full 2p/3p/4p matches)
      /----\
     /      \   Integration: Spring Boot + Testcontainers PostgreSQL (REST, STOMP, pipeline, recovery)
    /--------\
   /          \   Unit: pure game domain (JUnit 5 + AssertJ + jqwik), React components (Vitest + RTL)
  /------------\
```

## Unit Tests
- **Coverage target**: at least **90% line coverage for the `game` module**, a JaCoCo gate enforced by TASK-015 (success metric 3). Other modules report coverage without a gate.
- **Backend** (`backend/src/test/java/com/lukaszpelikan/thousand/game/**`):
  - Rule tests are named after rule IDs, e.g. `R061_mustFollowSuit`. **Every approved `R-xxx` in ruleset.md has at least one domain test** (success metric 1). The traceability check is: for each approved R-ID, grep the test sources for a method name starting with that ID.
  - jqwik properties:
    - card conservation (24 cards always accounted for)
    - no hidden card in another seat's PlayerView
    - legalActions consistent with apply
  - Seeded `Randomness` gives deterministic deals.
- **Frontend** (`frontend/src/**/*.test.ts(x)`): Vitest + React Testing Library, testing behavior (intentions sent, what is rendered from a PlayerView) and not implementation details.

## Integration Tests
- **Framework**: JUnit 5 + Spring Boot test + Testcontainers PostgreSQL 17. Files are named `*IT.java` and run by Maven Failsafe.
- **Covers**:
  - REST endpoints against openapi.yaml, including status codes and Error bodies
  - security: CSRF, cookies, 401/403, rate limits
  - migrations and DB constraints
  - the MatchStateStore and pipeline (versioning, idempotency)
  - STOMP (handshake, interceptor, view push)
  - presence and timers, using an injectable `Clock` so no real 60 s or 2 min waits are needed
  - recovery after restart
  - mail, via a local SMTP test server container
- **Contract check**: each integration test class for a REST endpoint validates responses against `design/api/openapi.yaml`.

### API Test Cases (summary; the canonical tests are listed in Traceability)
- Auth: register (success, email/username taken, password policy), login (success, wrong password, rate limit), logout, reset request (registered and unknown email give the same response), reset confirm (valid, expired or used, sessions revoked)
- Account: GET /me (auth/unauth), DELETE /me (success, wrong password, seated → 409, history anonymized)
- Tables: create (2/3/4, invalid count, already seated, unavailable count), get and join (full, unknown code), leave (host closes), start (host only, full only)
- Matches: view (participant, non-participant 403), current activity, history (own matches only, statuses)

## E2E Tests
- **Framework**: Playwright (`frontend/e2e/`), run against the Compose stack in CI (the public repo gives unlimited minutes, ADR-008).
- **Scenarios**, from `define/test-scenarios/*.feature`:
  - [ ] Invite link survives login (TASK-077)
  - [ ] Complete 2-player match (TASK-086)
  - [ ] Complete 3-player match (TASK-087)
  - [ ] Complete 4-player match (TASK-088)
- The complete-match tests use the test profile's seeded deck (game-domain.md `Randomness`) with 2–4 browser contexts, one per player.

## Test Data
- **Users**: a factory creates unique emails and usernames per test. Passwords meet A-01. No shared mutable fixtures.
- **Deals**: seeded `Randomness` plus named deck fixtures for specific rule cases, such as "four 9s in seat 1" or "♥ marriage in the musik".
- **Time**: an injectable `Clock` for timers, grace periods and token expiry.
- **Database**: a fresh schema per integration test class (Testcontainers + Flyway), with no seed data in production.

## CI Integration
Runs on every PR (TASK-015, ADR-008):
1. Format and lint: Spotless, Checkstyle, ESLint, Prettier
2. TypeScript `tsc --noEmit`
3. Backend unit tests, plus the JaCoCo gate of ≥ 90% on `game`
4. Backend integration tests (Testcontainers)
5. Frontend unit tests (Vitest)
6. Dependency and security scanning (Dependabot/CodeQL or OWASP Dependency-Check)
7. Playwright E2E on the Compose stack
8. The ruleset verification check. The items still unverified are **reported**; the check **fails** only for items whose verification task is marked Done.

## Technical Tests
These don't use the `AC-N: ` prefix and don't count as canonical coverage.
- One domain test per approved `R-xxx`: R-001..R-005, R-010, R-011, R-020..R-022, R-030..R-035, R-040..R-042, R-050, R-051, R-060..R-064, R-070..R-073, R-080..R-084, R-086, R-091, R-100..R-102, R-110..R-112.
- jqwik properties: card conservation, hidden information, consistency of legalActions.
- Snapshot upcaster tests using fixtures of the previous `schema_version` (ADR-004 risk).
- Log-masking test: no password, token or email in any log line during the auth flows.
- Timing: the Argon2id hash stays at 250 ms or less on CI hardware (ADR-003 risk; a warning, not a gate).

## Traceability

The canonical test names below reproduce each criterion text, shortened to 140 characters where needed. `Red Confirmed` is set to `Yes` during Implement.

| Task | AC | Story AC | Canonical Test File | Canonical Test Name | Red Confirmed |
|---|---|---|---|---|---|
| TASK-001 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/architecture/StackAlignmentTest.java` | AC-1: Given the backend test run, when the runtime is inspected, then the Java specification version is 25 | No |
| TASK-001 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/architecture/StackAlignmentTest.java` | AC-2: Given the backend classes, when the architecture test runs, then no class depends on `lombok` | No |
| TASK-001 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/architecture/StackAlignmentTest.java` | AC-3: Given the backend classes, when the architecture test runs, then every class resides in one of the modules identity, user, lobby, m... | No |
| TASK-002 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/architecture/ModuleBoundariesTest.java` | AC-1: Given any class in `game`, when the architecture test runs, then it depends only on `java.*` and `game` packages | No |
| TASK-002 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/architecture/ModuleBoundariesTest.java` | AC-2: Given the modules, when the architecture test runs, then module dependencies follow only the allowed directions in pattern.md (matc... | No |
| TASK-002 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/architecture/ModuleBoundariesTest.java` | AC-3: Given any module, when the architecture test runs, then no class accesses another module's `infrastructure` package | No |
| TASK-003 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/shared/infrastructure/SchemaMigrationIT.java` | AC-1: Given an empty PostgreSQL 17 database, when the application starts, then Flyway creates app_user, password_reset_token, game_table,... | No |
| TASK-003 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/shared/infrastructure/SchemaMigrationIT.java` | AC-2: Given a match_participant row referencing a user, when that user row is deleted, then match_participant.user_id becomes NULL and th... | No |
| TASK-003 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/shared/infrastructure/SchemaMigrationIT.java` | AC-3: Given a user already seated at a table, when a second table_seat row for the same user is inserted, then the database rejects it | No |
| TASK-004 | AC-1 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-1: Given ruleset.md with an item still marked CONFIRM, when the verification check for that item runs, then it fails and names the item | No |
| TASK-004 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-2: Given an item marked VERIFIED without an evidence reference, when the verification check runs, then it fails and names the item | No |
| TASK-005 | AC-1 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-1: Given ruleset.md, when the ruleset verification check runs for C-04 (re-entry after pass, auction end), then C-04 has status VERIFI... | No |
| TASK-005 | AC-2 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-2: Given ruleset.md, when the ruleset verification check runs for C-07 (cap on the final contract after musik), then C-07 has status V... | No |
| TASK-006 | AC-1 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-1: Given ruleset.md, when the ruleset verification check runs for C-12 (void-suit trump/over-trump obligations and beating the led sui... | No |
| TASK-006 | AC-2 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-2: Given ruleset.md, when the ruleset verification check runs for C-13 (marriage on the declarer's first lead), then C-13 has status V... | No |
| TASK-007 | AC-1 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-1: Given ruleset.md, when the ruleset verification check runs for C-01 (who sits out in 4p), then C-01 has status VERIFIED with the ob... | No |
| TASK-007 | AC-2 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-2: Given ruleset.md, when the ruleset verification check runs for C-02 (4p deal layout), then C-02 has status VERIFIED with the observ... | No |
| TASK-007 | AC-3 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-3: Given ruleset.md, when the ruleset verification check runs for C-05 (visibility of the unchosen 2p musik), then C-05 has status VER... | No |
| TASK-007 | AC-4 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-4: Given ruleset.md, when the ruleset verification check runs for C-06 (4p musik handling), then C-06 has status VERIFIED with the obs... | No |
| TASK-008 | AC-1 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-1: Given ruleset.md, when the ruleset verification check runs for C-08 (free first bomb per player or per game), then C-08 has status ... | No |
| TASK-008 | AC-2 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-2: Given ruleset.md, when the ruleset verification check runs for C-09 (when a bomb can be thrown), then C-09 has status VERIFIED with... | No |
| TASK-008 | AC-3 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-3: Given ruleset.md, when the ruleset verification check runs for C-10 (bomb and the 4p player sitting out), then C-10 has status VERI... | No |
| TASK-008 | AC-4 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-4: Given ruleset.md, when the ruleset verification check runs for C-11 (bomb bonus for opponents on the barrel), then C-11 has status ... | No |
| TASK-009 | AC-1 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-1: Given ruleset.md, when the ruleset verification check runs for C-03 (four-9s redeal procedure), then C-03 has status VERIFIED with ... | No |
| TASK-009 | AC-2 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-2: Given ruleset.md, when the ruleset verification check runs for R-085 (negative scores), then R-085 has status VERIFIED with the obs... | No |
| TASK-009 | AC-3 | verification | `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java` | AC-3: Given ruleset.md, when the ruleset verification check runs for R-103 (4p sit-out player on the barrel), then R-103 has status VERIF... | No |
| TASK-010 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/shared/api/ErrorModelIT.java` | AC-1: Given a REST request with an invalid body, when it is submitted, then the response is 400 with an Error body containing code, messa... | No |
| TASK-010 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/shared/api/ErrorModelIT.java` | AC-2: Given a request to an unknown /api path, when it is submitted, then the response is 404 with an Error body | No |
| TASK-011 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/SecurityFoundationIT.java` | AC-1: Given any client, when it calls GET /api/auth/csrf, then the response sets the XSRF-TOKEN cookie | No |
| TASK-011 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/SecurityFoundationIT.java` | AC-2: Given a mutating REST request without a valid X-XSRF-TOKEN header, when it is submitted, then it is rejected with 403 | No |
| TASK-011 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/SecurityFoundationIT.java` | AC-3: Given an unauthenticated client, when it calls GET /api/me, then the response is 401 with an Error body | No |
| TASK-011 | AC-4 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/SecurityFoundationIT.java` | AC-4: Given a successful authentication, when the session cookie is issued, then it is HttpOnly, Secure and SameSite=Strict | No |
| TASK-012 | AC-1 | US-001 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RegistrationIT.java` | AC-1: Given a visitor with an unused email and username and a password that meets the policy (A-01), when they submit registration, then ... | No |
| TASK-012 | AC-2 | US-001 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RegistrationIT.java` | AC-2: Given an email that is already registered, when a visitor submits registration with it, then registration is rejected with an "emai... | No |
| TASK-012 | AC-3 | US-001 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RegistrationIT.java` | AC-3: Given a username that is already taken, when a visitor submits registration with it, then registration is rejected with a "username... | No |
| TASK-013 | AC-1 | technical | `frontend/src/app/foundation.test.tsx` | AC-1: Given an unauthenticated visitor, when they open a protected route, then they are redirected to /logowanie | No |
| TASK-013 | AC-2 | technical | `frontend/src/app/foundation.test.tsx` | AC-2: Given the API client, when it sends a mutating request, then the request carries the X-XSRF-TOKEN header from the XSRF-TOKEN cookie | No |
| TASK-014 | AC-1 | technical | `frontend/src/features/auth/RegisterPage.test.tsx` | AC-1: Given the register page, when the server returns EMAIL_UNAVAILABLE, then the Polish message for that code is shown next to the emai... | No |
| TASK-014 | AC-2 | technical | `frontend/src/features/auth/RegisterPage.test.tsx` | AC-2: Given valid registration input, when the form is submitted and the server returns 201, then the user is taken to the home page as a... | No |
| TASK-015 | AC-1 | US-027 AC-1a | `tests/ops/ci-workflow.test.ts` | AC-1: Given a pull request, when CI runs, then the workflow executes unit tests, integration tests, dependency and security scanning, and... | No |
| TASK-015 | AC-2 | US-027 AC-1b | `tests/ops/ci-workflow.test.ts` | AC-2: Given a pull request with any failing CI job, when a merge is attempted, then branch protection blocks it. | No |
| TASK-015 | AC-3 | US-027 AC-2 | `tests/ops/ci-workflow.test.ts` | AC-3: Given the domain/game module, when CI reports coverage, then line coverage is at least 90% or the build fails. | No |
| TASK-016 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/CardsTest.java` | AC-1: Given a new deck, when it is created, then it contains exactly the 24 distinct cards 9-A in four suits (R-001) | No |
| TASK-016 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/CardsTest.java` | AC-2: Given two cards of the same suit, when their strength is compared, then the order is 9 < J < Q < K < 10 < A (R-002) | No |
| TASK-016 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/CardsTest.java` | AC-3: Given all 24 cards, when their point values are summed, then the total is 120 (R-003) | No |
| TASK-016 | AC-4 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/CardsTest.java` | AC-4: Given each suit, when its marriage value is read, then it is ♥ 100, ♦ 80, ♣ 60, ♠ 40 (R-004) | No |
| TASK-017 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/VariantsTest.java` | AC-1: Given a player count with at least one required rule option unset, when Variants.forPlayerCount is called, then UnverifiedRuleExcep... | No |
| TASK-017 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/VariantsTest.java` | AC-2: Given a player count whose required options are all set, when Variants.forPlayerCount is called, then it returns the configuration ... | No |
| TASK-018 | AC-1 | US-011 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/DealTest.java` | AC-1: Given a 3-player match, when a round is dealt, then each player holds 7 cards and the musik holds 3 cards from a 24-card deck (R-00... | No |
| TASK-018 | AC-2 | US-011 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/DealTest.java` | AC-2: Given a 2-player match, when a round is dealt, then each player holds 10 cards and there are two musiks of 2 cards each (R-021). | No |
| TASK-018 | AC-3 | US-011 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/DealTest.java` | AC-3: Given a round that ends, when the next round is dealt, then the dealer is the player to the left of the previous dealer (R-010). | No |
| TASK-019 | AC-1 | US-011 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/PlayerViewTest.java` | AC-1: Given a dealt round, when a player's client receives the game state, then it contains only that player's own cards and not other ha... | No |
| TASK-020 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/shared/api/HealthIT.java` | AC-1: Given the running application, when an unauthenticated client calls GET /api/health, then the response is 200 with status UP | No |
| TASK-020 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/shared/api/HealthIT.java` | AC-2: Given the running application, when /actuator/prometheus is requested on the public port 8080, then it is not served, and it is ser... | No |
| TASK-021 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/GameEngineTest.java` | AC-1: Given a round in any phase, when an action that is not valid in that phase is applied, then it is rejected with ILLEGAL_PHASE | No |
| TASK-021 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/GameEngineTest.java` | AC-2: Given a round, when a seat that is not on turn submits an action, then it is rejected with NOT_YOUR_TURN | No |
| TASK-021 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/GameEngineTest.java` | AC-3: Given an accepted action, when the new state is returned, then its version is exactly the previous version plus 1 | No |
| TASK-022 | AC-1 | US-012 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/AuctionTest.java` | AC-1: Given a new round, when bidding starts, then the player to the dealer's left holds an automatic bid of 100 (R-030). | No |
| TASK-022 | AC-2 | US-012 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/AuctionTest.java` | AC-2: Given a bidding turn, when a player bids a value that isn't a multiple of 10 or doesn't exceed the current bid, then the server rej... | No |
| TASK-022 | AC-3 | US-012 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/AuctionTest.java` | AC-3: Given a player holding no marriage, when they bid above 120, then the server rejects the bid (R-032). | No |
| TASK-022 | AC-4 | US-012 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/AuctionTest.java` | AC-4: Given a player holding marriages totalling M points, when they bid above 120 + M, then the server rejects the bid (R-033). | No |
| TASK-023 | AC-1 | US-013 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MusikThreePlayerTest.java` | AC-1: Given a 3-player declarer, when they take the musik, then its 3 cards are revealed to all players and added to the declarer's hand ... | No |
| TASK-023 | AC-2 | US-013 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MusikThreePlayerTest.java` | AC-2: Given a 3-player declarer holding 10 cards, when they give one card to each opponent, then every player holds 8 cards (R-040). | No |
| TASK-023 | AC-3 | US-013 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MusikThreePlayerTest.java` | AC-3: Given a declarer declaring the final contract, when the declared value is below their winning bid, then the server rejects it (R-042). | No |
| TASK-024 | AC-1 | US-013 AC-3a | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MusikTwoPlayerTest.java` | AC-1: Given a 2-player declarer, when they choose one of the two musiks, then only the chosen musik's cards are revealed to both players ... | No |
| TASK-024 | AC-2 | US-013 AC-3b | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MusikTwoPlayerTest.java` | AC-2: Given a 2-player declarer holding 12 cards after taking a musik, when they discard 2 cards face down, then each player holds 10 car... | No |
| TASK-025 | AC-1 | US-014 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickPlayTest.java` | AC-1: Given the first trick of a round, when play begins, then only the declarer may lead (R-060). | No |
| TASK-025 | AC-2 | US-014 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickPlayTest.java` | AC-2: Given a player holding a card of the led suit, when they play a card of another suit, then the server rejects the move (R-061). | No |
| TASK-025 | AC-3 | US-014 AC-5 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickPlayTest.java` | AC-3: Given a player whose move is rejected, when the rejection is returned, then the game state is unchanged for all players. | No |
| TASK-026 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/UnchosenMusikTest.java` | AC-1: Given a 2-player round, when the unchosen musik is projected in any PlayerView during and after the round, then its visibility foll... | No |
| TASK-027 | AC-1 | US-014 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickResolutionTest.java` | AC-1: Given a player holding a card of the led suit that beats the current highest card of that suit, when they play a lower card of that... | No |
| TASK-027 | AC-2 | US-014 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickResolutionTest.java` | AC-2: Given a completed trick, when the winner is determined, then the strongest trump wins, or if there is no trump the strongest card o... | No |
| TASK-028 | AC-1 | US-015 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MarriageTest.java` | AC-1: Given a player on lead holding the Q and K of one suit, when they lead one of them and declare, then the marriage value is added to... | No |
| TASK-028 | AC-2 | US-015 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MarriageTest.java` | AC-2: Given an active trump, when a new marriage is declared, then the new suit replaces the previous trump (R-071). | No |
| TASK-028 | AC-3 | US-015 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MarriageTest.java` | AC-3: Given a player who doesn't hold both the Q and K of a suit, when they try to declare a marriage in it, then the server rejects the ... | No |
| TASK-028 | AC-4 | US-015 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MarriageTest.java` | AC-4: Given a defender on lead holding a marriage, when they declare it, then the declaration is accepted and scored for that defender (R... | No |
| TASK-029 | AC-1 | US-016 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/RoundScoringTest.java` | AC-1: Given a declarer whose card points plus declared marriages are at least the contract, when the round is scored, then the contract i... | No |
| TASK-029 | AC-2 | US-016 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/RoundScoringTest.java` | AC-2: Given a declarer whose card points plus declared marriages are below the contract, when the round is scored, then the contract is s... | No |
| TASK-029 | AC-3 | US-016 AC-3a | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/RoundScoringTest.java` | AC-3: Given a defender whose card points plus marriages end in 5 to 9, when the round is scored, then the value is rounded up to the next... | No |
| TASK-029 | AC-4 | US-016 AC-3b | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/RoundScoringTest.java` | AC-4: Given a defender whose card points plus marriages end in 1 to 4, when the round is scored, then the value is rounded down to the pr... | No |
| TASK-030 | AC-1 | US-016 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BarrelScoringTest.java` | AC-1: Given a 2-player round, when it is scored, then the card points in both musik piles (the unchosen musik and the declarer's discards... | No |
| TASK-030 | AC-2 | US-016 AC-5 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BarrelScoringTest.java` | AC-2: Given a player with 800 or more points who is a defender, when the round is scored, then their score doesn't increase (R-100). | No |
| TASK-030 | AC-3 | US-016 AC-6 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BarrelScoringTest.java` | AC-3: Given a player with 800 or more points who fails as declarer, when the round is scored, then the contract is subtracted (R-102). | No |
| TASK-031 | AC-1 | US-019 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/EndOfGameTest.java` | AC-1: Given a round after which exactly one player has 1000 or more points, when it is scored, then the match ends with that player as th... | No |
| TASK-031 | AC-2 | US-019 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/EndOfGameTest.java` | AC-2: Given several players reaching 1000 in the same round, one of whom was the declarer, when it is scored, then the declarer wins (R-1... | No |
| TASK-031 | AC-3 | US-019 AC-3a | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/EndOfGameTest.java` | AC-3: Given several players reaching 1000 in the same round, none of whom was the declarer, with different totals, when the winner is det... | No |
| TASK-031 | AC-4 | US-019 AC-3b | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/EndOfGameTest.java` | AC-4: Given several players reaching 1000 in the same round, none of whom was the declarer, with equal totals, when the winner is determi... | No |
| TASK-032 | AC-1 | US-001 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RegistrationPolicyIT.java` | AC-1: Given a password shorter than the minimum length (A-01), when a visitor submits registration, then registration is rejected with a ... | No |
| TASK-032 | AC-2 | US-001 AC-5 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RegistrationPolicyIT.java` | AC-2: Given a successful registration, when the stored account record is inspected, then the password is stored only as an Argon2id hash ... | No |
| TASK-033 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/LegalActionsTest.java` | AC-1: Given any reachable state and seat, when legalActions is computed, then every listed action is accepted by apply and no unlisted ac... | No |
| TASK-034 | AC-1 | US-012 AC-5 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/AuctionOptionsTest.java` | AC-1: Given every other player has passed after the automatic 100, when bidding ends, then the opener is the declarer at 100 (R-035). | No |
| TASK-034 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/AuctionOptionsTest.java` | AC-2: Given a player who has passed, when they later submit a bid in the same auction, then the engine applies the C-04 behavior (exact e... | No |
| TASK-034 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/AuctionOptionsTest.java` | AC-3: Given a declarer after taking the musik, when they declare a final contract above the bid cap, then the engine applies the C-07 beh... | No |
| TASK-035 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickOptionsTest.java` | AC-1: Given a player void in the led suit holding a trump while a trump is active, when they play a non-trump, then the engine applies C-... | No |
| TASK-035 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickOptionsTest.java` | AC-2: Given a trump already in the trick and a player void in the led suit holding a higher trump, when they play a lower trump, then the... | No |
| TASK-035 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickOptionsTest.java` | AC-3: Given a trick already trumped and a player able to follow the led suit, when they play a lower card of the led suit, then the engin... | No |
| TASK-035 | AC-4 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickOptionsTest.java` | AC-4: Given the declarer on the first lead of a round holding a marriage, when they lead its Q or K and declare, then the engine applies ... | No |
| TASK-036 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/FourPlayerVariantTest.java` | AC-1: Given a 4-player round, when it starts, then the player sitting out is the one defined by C-01 (exact expected behavior = the VERIF... | No |
| TASK-036 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/FourPlayerVariantTest.java` | AC-2: Given a 4-player round, when it is dealt, then the active players' hands and the musik follow C-02 (exact expected behavior = the V... | No |
| TASK-036 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/FourPlayerVariantTest.java` | AC-3: Given a 4-player declarer, when they take the musik, then musik handling follows C-06 (exact expected behavior = the VERIFIED entry... | No |
| TASK-036 | AC-4 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/FourPlayerVariantTest.java` | AC-4: Given a 4-player round whose musik contains an ace and the ♥ marriage, when the round is scored, then the player sitting out scores... | No |
| TASK-037 | AC-1 | US-017 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BombTest.java` | AC-1: Given a declarer who throws a bomb, when the round is scored, then the declarer's score is unchanged (R-050). | No |
| TASK-037 | AC-2 | US-017 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BombTest.java` | AC-2: Given the first bomb that qualifies as free, when it is thrown, then no opponent receives points (R-051). | No |
| TASK-037 | AC-3 | US-017 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BombTest.java` | AC-3: Given a subsequent bomb in a 2- or 3-player match, when it is thrown, then each opponent receives 60 points (R-051). | No |
| TASK-037 | AC-4 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BombTest.java` | AC-4: Given a declarer at a point where C-09 does not allow a bomb, when they throw a bomb, then it is rejected (exact expected behavior ... | No |
| TASK-038 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BombOptionsTest.java` | AC-1: Given a second bomb in the same game, when it is thrown, then whether it is free follows C-08 (exact expected behavior = the VERIFI... | No |
| TASK-038 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BombOptionsTest.java` | AC-2: Given a 4-player round, when the declarer throws a penalized bomb, then the sitting-out player's points follow C-10 (exact expected... | No |
| TASK-038 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BombOptionsTest.java` | AC-3: Given an opponent on the barrel, when the declarer throws a penalized bomb, then that opponent's points follow C-11 (exact expected... | No |
| TASK-039 | AC-1 | US-018 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/RedealTest.java` | AC-1: Given a player holding all four 9s, when they request a redeal, then the round is redealt (R-022). | No |
| TASK-039 | AC-2 | US-018 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/RedealTest.java` | AC-2: Given a player not holding all four 9s, when they request a redeal, then the server rejects the request. | No |
| TASK-039 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/RedealTest.java` | AC-3: Given a player holding all four 9s, when they request a redeal outside the moment C-03 allows, then it is rejected (exact expected ... | No |
| TASK-040 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/ScoringOptionsTest.java` | AC-1: Given a declarer with 50 points who fails a contract of 120, when the round is scored, then the resulting score follows R-085 (exac... | No |
| TASK-040 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/ScoringOptionsTest.java` | AC-2: Given a 4-player sitting-out player on the barrel whose musik contains an ace, when the round is scored, then their score follows R... | No |
| TASK-041 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/timeout/TimeoutFallbackTest.java` | AC-1: Given a bidding turn, when the fallback chooses a move, then it is PASS | No |
| TASK-041 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/timeout/TimeoutFallbackTest.java` | AC-2: Given a 2-player musik choice, when the fallback chooses a move, then it is the first musik in the engine's internal ordering | No |
| TASK-041 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/timeout/TimeoutFallbackTest.java` | AC-3: Given a trick-play turn, when the fallback chooses a move, then it plays the legal card with the lowest rank strength, and among eq... | No |
| TASK-042 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/timeout/TimeoutFallbackOptionalTest.java` | AC-1: Given a 2-player discard, when the fallback chooses a move, then it discards the 2 lowest legal cards by the ADR-009 ordering | No |
| TASK-042 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/timeout/TimeoutFallbackOptionalTest.java` | AC-2: Given 3p/4p card passing, when the fallback chooses a move, then it gives the lowest legal cards by the ADR-009 ordering, assigned ... | No |
| TASK-042 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/timeout/TimeoutFallbackOptionalTest.java` | AC-3: Given a final contract declaration, when the fallback chooses a move, then it declares exactly the winning bid | No |
| TASK-042 | AC-4 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/game/timeout/TimeoutFallbackOptionalTest.java` | AC-4: Given any decision point, when the fallback chooses a move, then it is never THROW_BOMB, a marriage declaration or REQUEST_REDEAL | No |
| TASK-043 | AC-1 | US-002 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/LoginIT.java` | AC-1: Given a registered player, when they submit a correct email and password, then they are authenticated and can reach protected pages. | No |
| TASK-043 | AC-2 | US-002 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/LoginIT.java` | AC-2: Given a registered player, when they submit a wrong password, then login fails with a generic "invalid credentials" error that does... | No |
| TASK-043 | AC-3 | US-002 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/LoginIT.java` | AC-3: Given an authenticated player, when they log out, then their session or token no longer authorizes protected REST or WebSocket requ... | No |
| TASK-044 | AC-1 | US-002 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RateLimitIT.java` | AC-1: Given repeated failed login attempts exceeding the configured rate limit (A-02), when another attempt is made, then it is rejected ... | No |
| TASK-044 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RateLimitIT.java` | AC-2: Given repeated password-reset requests exceeding the configured limit, when another request is made, then it is rejected with HTTP 429 | No |
| TASK-045 | AC-1 | US-003 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/PasswordResetRequestIT.java` | AC-1: Given a registered email, when a reset is requested, then one email containing a single-use reset link is sent through the SMTP pro... | No |
| TASK-045 | AC-2 | US-003 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/PasswordResetRequestIT.java` | AC-2: Given an unregistered email, when a reset is requested, then the response is identical to the registered-email response and no emai... | No |
| TASK-046 | AC-1 | US-003 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/PasswordResetConfirmIT.java` | AC-1: Given a valid, unexpired reset token, when the player submits a new password that meets the policy, then the new password works for... | No |
| TASK-046 | AC-2 | US-003 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/PasswordResetConfirmIT.java` | AC-2: Given a reset token that is already used or older than 30 minutes (A-03), when it is submitted, then the reset is rejected and the ... | No |
| TASK-046 | AC-3 | US-003 AC-5 | `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/PasswordResetConfirmIT.java` | AC-3: Given a successful password reset, when a session issued before the reset is used, then it is rejected (A-03). | No |
| TASK-047 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/identity/infrastructure/MailDeliveryIT.java` | AC-1: Given the SMTP server fails twice then succeeds, when a reset email is sent, then it is delivered on the third attempt | No |
| TASK-047 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/identity/infrastructure/MailDeliveryIT.java` | AC-2: Given the SMTP server fails on all 3 attempts, when a reset email is sent, then mail_send_failures_total increases by 1 and the log... | No |
| TASK-048 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/identity/infrastructure/ResetTokenRetentionIT.java` | AC-1: Given reset tokens that expired more than 24 h ago, when the daily retention job runs, then they are deleted and unexpired tokens r... | No |
| TASK-049 | AC-1 | technical | `tests/ops/external-setup.test.ts` | AC-1: Given the registered domain, when its DNS A record is resolved, then it points to the VPS IP address | No |
| TASK-049 | AC-2 | technical | `tests/ops/external-setup.test.ts` | AC-2: Given the SMTP provider credentials in the VPS .env, when a test email is sent through the provider, then it is accepted | No |
| TASK-050 | AC-1 | technical | `tests/ops/vps-hardening.test.ts` | AC-1: Given the VPS, when its open TCP ports are scanned from outside, then only 22, 80 and 443 are open | No |
| TASK-050 | AC-2 | technical | `tests/ops/vps-hardening.test.ts` | AC-2: Given the VPS SSH service, when a password login is attempted, then it is refused | No |
| TASK-051 | AC-1 | US-027 AC-3 | `tests/ops/deploy-workflow.test.ts` | AC-1: Given a merge to `main`, when the deploy workflow runs, then the new version is running on the VPS without manual SSH steps. | No |
| TASK-051 | AC-2 | technical | `tests/ops/deploy-workflow.test.ts` | AC-2: Given a deployed version whose smoke check fails for 60 s, when the deploy workflow runs, then the previous image tag is restored a... | No |
| TASK-052 | AC-1 | US-027 AC-4 | `tests/ops/https.test.ts` | AC-1: Given the production domain, when it is requested over HTTP, then it redirects to HTTPS with a valid certificate. | No |
| TASK-053 | AC-1 | US-006 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/CreateTableIT.java` | AC-1: Given an authenticated player who is not seated elsewhere (A-10), when they create a table with a player count of 2, 3 or 4, then a... | No |
| TASK-053 | AC-2 | US-006 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/CreateTableIT.java` | AC-2: Given a create request with a player count other than 2, 3 or 4, when it is submitted, then it is rejected with a validation error. | No |
| TASK-053 | AC-3 | US-006 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/CreateTableIT.java` | AC-3: Given a player already seated at another active table or match, when they try to create a table, then the request is rejected. | No |
| TASK-053 | AC-4 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/CreateTableIT.java` | AC-4: Given a player count whose variant configuration is incomplete, when GET /api/tables/available-player-counts is called, then that c... | No |
| TASK-054 | AC-1 | US-004 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/user/api/AccountDeletionIT.java` | AC-1: Given an authenticated player who confirms with their current password (A-14), when they delete their account, then their email, us... | No |
| TASK-054 | AC-2 | US-004 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/user/api/AccountDeletionIT.java` | AC-2: Given a deleted account, when its former credentials are used to log in, then login fails with the generic invalid-credentials error. | No |
| TASK-054 | AC-3 | US-004 AC-5 | `backend/src/test/java/com/lukaszpelikan/thousand/user/api/AccountDeletionIT.java` | AC-3: Given a player seated at an active table or match, when they request account deletion, then deletion is rejected until they have le... | No |
| TASK-055 | AC-1 | US-007 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/JoinTableIT.java` | AC-1: Given a table whose seats are all taken, when another player tries to join, then the join is rejected with a "table full" error. | No |
| TASK-055 | AC-2 | US-007 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/JoinTableIT.java` | AC-2: Given a code that doesn't match any open table, when a player submits it, then the join is rejected with a "table not found" error. | No |
| TASK-056 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/infrastructure/MatchStateStoreIT.java` | AC-1: Given a match that has just started, when the store is inspected, then a match_state_snapshot row with version 0 exists | No |
| TASK-056 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/infrastructure/MatchStateStoreIT.java` | AC-2: Given an accepted action, when it is processed, then the snapshot version increases by 1 in the same transaction as the in-memory swap | No |
| TASK-057 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ActionPipelineIT.java` | AC-1: Given an action with an expectedVersion different from the current version, when it is processed, then it is rejected with STALE_VE... | No |
| TASK-057 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ActionPipelineIT.java` | AC-2: Given an action whose clientActionId was already processed for that seat, when it is received again, then it is ignored and the ver... | No |
| TASK-058 | AC-1 | US-008 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/StartMatchIT.java` | AC-1: Given a table with all seats filled, when the host starts the match, then a match begins for that player count with a server-chosen... | No |
| TASK-058 | AC-2 | US-008 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/StartMatchIT.java` | AC-2: Given a table with at least one empty seat, when the host tries to start, then the start is rejected. | No |
| TASK-058 | AC-3 | US-008 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/StartMatchIT.java` | AC-3: Given a seated non-host player, when they try to start the match, then the start is rejected as unauthorized. | No |
| TASK-059 | AC-1 | US-009 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/LeaveTableIT.java` | AC-1: Given the host of an unstarted table, when they leave, then the table is closed and its join code no longer admits players (A-15). | No |
| TASK-060 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/StompSecurityIT.java` | AC-1: Given a WebSocket handshake to /ws without a valid session cookie, when it is attempted, then it is rejected | No |
| TASK-060 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/StompSecurityIT.java` | AC-2: Given an authenticated user who is not a participant of a match, when they subscribe to that match's view destination, then the sub... | No |
| TASK-060 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/StompSecurityIT.java` | AC-3: Given a handshake with a foreign Origin header, when it is attempted, then it is rejected | No |
| TASK-061 | AC-1 | US-021 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchRealtimeIT.java` | AC-1: Given a match in progress, when a player makes a legal move, then every connected participant receives the resulting state update o... | No |
| TASK-061 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchRealtimeIT.java` | AC-2: Given an illegal action sent over STOMP, when it is processed, then the sender receives ActionRejected with a stable code on /user/... | No |
| TASK-062 | AC-1 | US-007 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/SeatUpdatesIT.java` | AC-1: Given an open table with a free seat, when an authenticated player submits its join code, then they are seated and all seated playe... | No |
| TASK-062 | AC-2 | US-009 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/SeatUpdatesIT.java` | AC-2: Given a seated non-host player at an unstarted table, when they leave, then their seat becomes free and the other players see the u... | No |
| TASK-063 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchViewIT.java` | AC-1: Given a participant of a live match, when they call GET /api/matches/{id}/view, then they receive the same PlayerView as the STOMP ... | No |
| TASK-063 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchViewIT.java` | AC-2: Given a user who is not a participant, when they call GET /api/matches/{id}/view, then the response is 403 | No |
| TASK-063 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchViewIT.java` | AC-3: Given a user seated in a live match, when they call GET /api/me/current, then the response contains that matchId | No |
| TASK-064 | AC-1 | US-019 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/MatchResultRecordingIT.java` | AC-1: Given a match that ends at 1000, when it is recorded, then its status is `COMPLETED` with every participant's final score. | No |
| TASK-064 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/MatchResultRecordingIT.java` | AC-2: Given a match that ends, when its result is recorded, then its match_state_snapshot row no longer exists | No |
| TASK-065 | AC-1 | US-023 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/PresenceIT.java` | AC-1: Given a player who disconnects, when the other participants view the table, then they see that player marked as disconnected with t... | No |
| TASK-066 | AC-1 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/infrastructure/MatchRecoveryIT.java` | AC-1: Given an in-progress match snapshot, when the application restarts, then the match is restored and each seat's PlayerView equals th... | No |
| TASK-066 | AC-2 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/infrastructure/MatchRecoveryIT.java` | AC-2: Given a turn deadline with 40 s remaining at shutdown, when the match is restored, then the turn timer resumes with 40 s remaining | No |
| TASK-066 | AC-3 | technical | `backend/src/test/java/com/lukaszpelikan/thousand/match/infrastructure/MatchRecoveryIT.java` | AC-3: Given disconnect grace periods at shutdown, when the match is restored, then each grace period restarts at the moment of recovery | No |
| TASK-067 | AC-1 | US-023 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ReconnectIT.java` | AC-1: Given a player who disconnects mid-match, when they reconnect within 2 minutes, then they receive the full current game state for t... | No |
| TASK-067 | AC-2 | US-023 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ReconnectIT.java` | AC-2: Given a player who reconnected within the grace period, when their state is compared with the state before the disconnect, then no ... | No |
| TASK-068 | AC-1 | US-022 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/TurnTimerIT.java` | AC-1: Given a connected player whose 60-second bidding turn timer (A-09) expires and who is allowed to pass, when the timer elapses, then... | No |
| TASK-068 | AC-2 | US-022 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/TurnTimerIT.java` | AC-2: Given a connected player whose 60-second trick-play turn timer (A-09) expires, when the timer elapses, then the server plays their ... | No |
| TASK-068 | AC-3 | US-022 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/TurnTimerIT.java` | AC-3: Given a player whose turn timer expired once, when the automatic move has been made, then the match continues and the player is sti... | No |
| TASK-069 | AC-1 | US-024 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ForfeitIT.java` | AC-1: Given a disconnected player who doesn't reconnect within 2 minutes, when the grace period expires, then the match ends with status ... | No |
| TASK-069 | AC-2 | US-024 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ForfeitIT.java` | AC-2: Given a match ended by forfeit, when it is recorded, then participants, status, timestamp and the scores at that moment are stored. | No |
| TASK-069 | AC-3 | US-024 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ForfeitIT.java` | AC-3: Given all remaining participants disconnected beyond the grace period, when the last grace period expires, then the match ends with... | No |
| TASK-070 | AC-1 | US-026 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/match/infrastructure/StatsEligibleQueryIT.java` | AC-1: Given stored matches with statuses `COMPLETED`, `FORFEIT` and `ABANDONED`, when the statistics-eligible match query runs, then only... | No |
| TASK-071 | AC-1 | US-025 AC-1 | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchHistoryIT.java` | AC-1: Given a player with finished matches, when they open their history, then each match shows its date, player count, participants, sta... | No |
| TASK-071 | AC-2 | US-025 AC-2 | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchHistoryIT.java` | AC-2: Given a player's history, when it is requested, then it contains only matches that player took part in. | No |
| TASK-071 | AC-3 | US-025 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchHistoryIT.java` | AC-3: Given a match with status `FORFEIT` or `ABANDONED`, when it appears in history, then its status is shown explicitly and distinguish... | No |
| TASK-072 | AC-1 | US-004 AC-3 | `backend/src/test/java/com/lukaszpelikan/thousand/user/api/AccountDeletionHistoryIT.java` | AC-1: Given a completed match that included the deleted player, when another participant views that match in their history, then the dele... | No |
| TASK-072 | AC-2 | US-004 AC-4 | `backend/src/test/java/com/lukaszpelikan/thousand/user/api/AccountDeletionHistoryIT.java` | AC-2: Given a deleted account, when all remaining match history records are inspected, then none contains the deleted user's id, email or... | No |
| TASK-073 | AC-1 | technical | `frontend/src/features/auth/AuthPages.test.tsx` | AC-1: Given the login page, when the server returns INVALID_CREDENTIALS, then the generic Polish invalid-credentials message is shown | No |
| TASK-073 | AC-2 | technical | `frontend/src/features/auth/AuthPages.test.tsx` | AC-2: Given the forgot-password page, when any email is submitted and the server returns 202, then the same Polish confirmation message i... | No |
| TASK-073 | AC-3 | technical | `frontend/src/features/auth/AuthPages.test.tsx` | AC-3: Given the reset page with a token, when the server returns RESET_TOKEN_INVALID, then the Polish expired-or-used link message is shown | No |
| TASK-074 | AC-1 | US-005 AC-1 | `frontend/src/features/legal/PrivacyPolicyPage.test.tsx` | AC-1: Given an unauthenticated visitor on the registration page, when they follow the privacy policy link, then the privacy policy page i... | No |
| TASK-074 | AC-2 | US-005 AC-2 | `frontend/src/features/legal/PrivacyPolicyPage.test.tsx` | AC-2: Given the privacy policy page, when it is displayed, then it lists the stored data (email, username, password hash, match history),... | No |
| TASK-075 | AC-1 | technical | `frontend/src/features/profile/AccountPage.test.tsx` | AC-1: Given the account page, when the user confirms deletion with their password and the server returns 204, then they are logged out an... | No |
| TASK-075 | AC-2 | technical | `frontend/src/features/profile/AccountPage.test.tsx` | AC-2: Given the account page, when the server returns SEATED_CANNOT_DELETE, then the Polish message explaining the seated restriction is ... | No |
| TASK-076 | AC-1 | US-010 AC-1 | `frontend/src/features/lobby/Home.test.tsx` | AC-1: Given a player with no match history, when they open their home or history page, then an empty state is shown with "Create table" a... | No |
| TASK-076 | AC-2 | technical | `frontend/src/features/lobby/Home.test.tsx` | AC-2: Given available player counts [2, 3], when the create-table form is shown, then the 4-player option is disabled with an explanation | No |
| TASK-077 | AC-1 | US-007 AC-2 | `frontend/e2e/invite-link.spec.ts` | AC-1: Given an unauthenticated visitor opening an invite link, when they log in, then they are returned to that table's join flow. | No |
| TASK-078 | AC-1 | technical | `frontend/src/services/websocket/stompClient.test.ts` | AC-1: Given an active match subscription, when the connection drops, then the reconnect banner is shown and the client resubscribes after... | No |
| TASK-078 | AC-2 | technical | `frontend/src/services/websocket/stompClient.test.ts` | AC-2: Given a received PlayerView, when it is applied, then the game store state equals that view exactly | No |
| TASK-079 | AC-1 | technical | `frontend/src/features/game/TrickPlay.test.tsx` | AC-1: Given a view where it is my turn in trick play, when I click a card listed in legalActions, then a PLAY_CARD intention with that ca... | No |
| TASK-079 | AC-2 | technical | `frontend/src/features/game/TrickPlay.test.tsx` | AC-2: Given a view, when it is rendered, then cards not in legalActions are dimmed and not clickable | No |
| TASK-080 | AC-1 | technical | `frontend/src/features/game/BiddingPanel.test.tsx` | AC-1: Given my bidding turn, when I choose Pass, then a PASS intention is sent | No |
| TASK-080 | AC-2 | technical | `frontend/src/features/game/BiddingPanel.test.tsx` | AC-2: Given my bidding turn, when I enter a bid value, then a BID intention with that value is sent | No |
| TASK-080 | AC-3 | technical | `frontend/src/features/game/BiddingPanel.test.tsx` | AC-3: Given the contract phase as declarer, when I declare a value, then a DECLARE_CONTRACT intention with that value is sent | No |
| TASK-081 | AC-1 | technical | `frontend/src/features/game/MusikPanels.test.tsx` | AC-1: Given the 2-player musik phase as declarer, when I choose a musik and then select 2 cards to discard, then CHOOSE_MUSIK and DISCARD... | No |
| TASK-081 | AC-2 | technical | `frontend/src/features/game/MusikPanels.test.tsx` | AC-2: Given the 3-player card-passing phase as declarer, when I assign one card to each opponent, then a GIVE_CARDS intention with both a... | No |
| TASK-082 | AC-1 | technical | `frontend/src/features/game/OptionalActions.test.tsx` | AC-1: Given my lead holding a marriage, when I play its Q or K with the marriage toggle on, then PLAY_CARD is sent with declareMarriage true | No |
| TASK-082 | AC-2 | technical | `frontend/src/features/game/OptionalActions.test.tsx` | AC-2: Given THROW_BOMB or REQUEST_REDEAL in legalActions, when the view is rendered, then the corresponding button is shown, and otherwis... | No |
| TASK-083 | AC-1 | technical | `frontend/src/features/game/GameStatus.test.tsx` | AC-1: Given a view with a turn deadline less than 15 s away, when it is rendered, then the timer shows the warning state | No |
| TASK-083 | AC-2 | technical | `frontend/src/features/game/GameStatus.test.tsx` | AC-2: Given a view with phase GAME_OVER, when it is rendered, then the game-over dialog shows the winner or draw and all final scores | No |
| TASK-083 | AC-3 | technical | `frontend/src/features/game/GameStatus.test.tsx` | AC-3: Given a view whose last event is an automatic move, when it is rendered, then the event line shows "Ruch automatyczny (czas minął)" | No |
| TASK-084 | AC-1 | technical | `frontend/src/features/game/FourPlayerLayout.test.tsx` | AC-1: Given a 4-player view, when it is rendered, then four seats are shown and the sitting-out seat is greyed out | No |
| TASK-085 | AC-1 | technical | `frontend/src/features/profile/MatchHistory.test.tsx` | AC-1: Given a history page with a FORFEIT match, when it is rendered, then the match shows a FORFEIT badge distinct from COMPLETED | No |
| TASK-085 | AC-2 | technical | `frontend/src/features/profile/MatchHistory.test.tsx` | AC-2: Given a participant with deleted true, when the history is rendered, then that seat shows "Gracz usunięty" | No |
| TASK-086 | AC-1 | US-020 AC-1 | `frontend/e2e/full-match-2p.spec.ts` | AC-1: Given a 2-player table, when a full match is played through the web UI until someone reaches 1000, then the match ends as `COMPLETE... | No |
| TASK-087 | AC-1 | US-020 AC-2 | `frontend/e2e/full-match-3p.spec.ts` | AC-1: Given a 3-player table, when a full match is played through the web UI until someone reaches 1000, then the match ends as `COMPLETE... | No |
| TASK-088 | AC-1 | US-020 AC-3 | `frontend/e2e/full-match-4p.spec.ts` | AC-1: Given a 4-player table, when a full match is played through the web UI until someone reaches 1000, then the match ends as `COMPLETE... | No |
| TASK-089 | AC-1 | US-028 AC-1 | `tests/ops/observability-metrics.test.ts` | AC-1: Given the production deployment, when the operator opens Grafana, then application metrics (HTTP latency, active WebSocket sessions... | No |
| TASK-090 | AC-1 | US-028 AC-2 | `tests/ops/observability-logs.test.ts` | AC-1: Given the production deployment, when the operator searches logs in Grafana, then application logs from Loki are available and cont... | No |
| TASK-091 | AC-1 | technical | `tests/ops/backup-restore.test.ts` | AC-1: Given the nightly backup job, when it has run, then a compressed pg_dump from the last 24 h exists off the VPS | No |
| TASK-091 | AC-2 | technical | `tests/ops/backup-restore.test.ts` | AC-2: Given the latest backup, when it is restored into an empty PostgreSQL, then the application starts against it and GET /api/health r... | No |
| TASK-092 | AC-1 | US-021 AC-2 | `tests/ops/load/k6-50-users.js` | AC-1: Given 50 concurrent simulated users in a load test, when typical game actions are submitted, then the p95 server processing time is... | No |
| TASK-093 | AC-1 | US-028 AC-3 | `tests/ops/observability-traces.test.ts` | AC-1: Given the tracing increment is delivered, when a request is handled, then its trace is viewable in Tempo. | No |
