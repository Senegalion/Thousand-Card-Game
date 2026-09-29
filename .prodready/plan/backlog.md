# Implementation Backlog

Generated from the accepted Define and Design artifacts. Story-linked acceptance criteria are copied verbatim from `define/requirements/user-stories.md`; the source of each AC is given in brackets.

- **Owner** tasks (`Owner: owner`) are Kurnik verification and external-account work done by the product owner. They count against the same capacity.
- `(Critical path: Kurnik)` marks tasks that cannot start until the relevant Kurnik verification is complete.
- AC texts containing `the VERIFIED entry for C-xx` are completed with the concrete observed behavior (a spec update) **before** their RED test is written; see `test-plan.md`.
- Sprint = 2 weeks, planned load ≤ 17.5 h (70% of ~25 h capacity at 10–15 h/week).

## Sprint 1

### TASK-001: Align backend skeleton with accepted stack and modules
**Priority**: P0 | **Estimate**: 3h | **Status**: Done | **Owner**: dev

**Description**:
Set Java 25 in `backend/pom.xml`, remove Lombok (ADR-001), rename `room` → `lobby` and `player` → `user`, create `identity` and `match` module packages (pattern.md mapping). No behavior yet.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/architecture/StackAlignmentTest.java`

**Acceptance Criteria**:
AC-1: Given the backend test run, when the runtime is inspected, then the Java specification version is 25 [technical]
AC-2: Given the backend classes, when the architecture test runs, then no class depends on `lombok` [technical]
AC-3: Given the backend classes, when the architecture test runs, then every class resides in one of the modules identity, user, lobby, match, game or shared [technical]

**TDD Tasks**:
- [x] Write failing canonical test for AC-1
- [x] Implement AC-1 (red→green)
- [x] Write failing canonical test for AC-2
- [x] Implement AC-2 (red→green)
- [x] Write failing canonical test for AC-3
- [x] Implement AC-3 (red→green)

**Blocked by**: None
**Blocks**: TASK-002, TASK-003, TASK-010, TASK-015

---

### TASK-002: Architecture rules: module boundaries and pure game domain
**Priority**: P0 | **Estimate**: 2h | **Status**: Done | **Owner**: dev

**Description**:
ArchUnit rules from pattern.md and ADR-006.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/architecture/ModuleBoundariesTest.java`

**Acceptance Criteria**:
AC-1: Given any class in `game`, when the architecture test runs, then it depends only on `java.*` and `game` packages [technical]
AC-2: Given the modules, when the architecture test runs, then module dependencies follow only the allowed directions in pattern.md (match→game, match→lobby api, lobby→identity api, user→identity/lobby/match api, game→none) [technical]
AC-3: Given any module, when the architecture test runs, then no class accesses another module's `infrastructure` package [technical]

**TDD Tasks**:
- [x] Write failing canonical test for AC-1
- [x] Implement AC-1 (red→green)
- [x] Write failing canonical test for AC-2
- [x] Implement AC-2 (red→green)
- [x] Write failing canonical test for AC-3
- [x] Implement AC-3 (red→green)

**Blocked by**: TASK-001
**Blocks**: TASK-016

---

### TASK-003: Flyway baseline migrations and jOOQ codegen
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Migrations from define/data-model/schema.sql + design/architecture/persistence.md (match_state_snapshot, Spring Session tables). jOOQ codegen from the migrated Testcontainers database (ADR-002).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/shared/infrastructure/SchemaMigrationIT.java`

**Acceptance Criteria**:
AC-1: Given an empty PostgreSQL 17 database, when the application starts, then Flyway creates app_user, password_reset_token, game_table, table_seat, match, match_participant, match_state_snapshot, spring_session and spring_session_attributes [technical]
AC-2: Given a match_participant row referencing a user, when that user row is deleted, then match_participant.user_id becomes NULL and the row remains [technical]
AC-3: Given a user already seated at a table, when a second table_seat row for the same user is inserted, then the database rejects it [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-001
**Blocks**: TASK-011, TASK-056

---

### TASK-004: Kurnik verification harness and evidence template
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
Adds status `VERIFIED` to the ruleset.md legend (procedure only, no rule change), an evidence template (`docs/kurnik-evidence/<ID>/`), and a JUnit check that reads ruleset.md. Used by every V-task's canonical test.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java`

**Acceptance Criteria**:
AC-1: Given ruleset.md with an item still marked CONFIRM, when the verification check for that item runs, then it fails and names the item [verification]
AC-2: Given an item marked VERIFIED without an evidence reference, when the verification check runs, then it fails and names the item [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: None
**Blocks**: TASK-005, TASK-006, TASK-007, TASK-008, TASK-009

---

### TASK-005: Kurnik verification: auction and final contract (C-04, C-07) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: owner

**Description**:
OWNER TASK. Observe real Kurnik games; record behavior and evidence (screenshots/game log) in ruleset.md. **Critical path**: affects all player counts.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java`

**Acceptance Criteria**:
AC-1: Given ruleset.md, when the ruleset verification check runs for C-04 (re-entry after pass, auction end), then C-04 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-2: Given ruleset.md, when the ruleset verification check runs for C-07 (cap on the final contract after musik), then C-07 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-004
**Blocks**: TASK-034

---

### TASK-006: Kurnik verification: trick obligations and marriage timing (C-12, C-13) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 2.5h | **Status**: Ready | **Owner**: owner

**Description**:
OWNER TASK. **Critical path**: affects all player counts.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java`

**Acceptance Criteria**:
AC-1: Given ruleset.md, when the ruleset verification check runs for C-12 (void-suit trump/over-trump obligations and beating the led suit after the trick was trumped, parts (i)-(iii)), then C-12 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-2: Given ruleset.md, when the ruleset verification check runs for C-13 (marriage on the declarer's first lead), then C-13 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-004
**Blocks**: TASK-035

---

### TASK-007: Kurnik verification: 4-player setup and 2-player musik (C-01, C-02, C-05, C-06) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: owner

**Description**:
OWNER TASK. **Critical path** for 4p (and C-05 for 2p).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java`

**Acceptance Criteria**:
AC-1: Given ruleset.md, when the ruleset verification check runs for C-01 (who sits out in 4p), then C-01 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-2: Given ruleset.md, when the ruleset verification check runs for C-02 (4p deal layout), then C-02 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-3: Given ruleset.md, when the ruleset verification check runs for C-05 (visibility of the unchosen 2p musik), then C-05 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-4: Given ruleset.md, when the ruleset verification check runs for C-06 (4p musik handling), then C-06 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-004
**Blocks**: TASK-026, TASK-036

---

## Sprint 2

### TASK-008: Kurnik verification: bomb (C-08, C-09, C-10, C-11) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: owner

**Description**:
OWNER TASK. **Critical path**: affects all player counts.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java`

**Acceptance Criteria**:
AC-1: Given ruleset.md, when the ruleset verification check runs for C-08 (free first bomb per player or per game), then C-08 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-2: Given ruleset.md, when the ruleset verification check runs for C-09 (when a bomb can be thrown), then C-09 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-3: Given ruleset.md, when the ruleset verification check runs for C-10 (bomb and the 4p player sitting out), then C-10 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-4: Given ruleset.md, when the ruleset verification check runs for C-11 (bomb bonus for opponents on the barrel), then C-11 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-004
**Blocks**: TASK-037

---

### TASK-009: Kurnik verification: redeal, negative scores, sit-out on barrel (C-03, R-085, R-103) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 2.5h | **Status**: Ready | **Owner**: owner

**Description**:
OWNER TASK. **Critical path**: C-03 and R-085 affect all player counts.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/ruleset/RulesetVerificationTest.java`

**Acceptance Criteria**:
AC-1: Given ruleset.md, when the ruleset verification check runs for C-03 (four-9s redeal procedure), then C-03 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-2: Given ruleset.md, when the ruleset verification check runs for R-085 (negative scores), then R-085 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]
AC-3: Given ruleset.md, when the ruleset verification check runs for R-103 (4p sit-out player on the barrel), then R-103 has status VERIFIED with the observed Kurnik behavior and an evidence reference recorded [verification]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-004
**Blocks**: TASK-039, TASK-040

---

### TASK-010: Shared REST error model and validation
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
`Error{code,message,fieldErrors}` as in openapi.yaml; Bean Validation mapping.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/shared/api/ErrorModelIT.java`

**Acceptance Criteria**:
AC-1: Given a REST request with an invalid body, when it is submitted, then the response is 400 with an Error body containing code, message and fieldErrors [technical]
AC-2: Given a request to an unknown /api path, when it is submitted, then the response is 404 with an Error body [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-001
**Blocks**: TASK-011

---

### TASK-011: Security foundation: Spring Session JDBC, CSRF, cookie policy
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-003: session cookie attributes, CookieCsrfTokenRepository, 401 for protected endpoints.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/SecurityFoundationIT.java`

**Acceptance Criteria**:
AC-1: Given any client, when it calls GET /api/auth/csrf, then the response sets the XSRF-TOKEN cookie [technical]
AC-2: Given a mutating REST request without a valid X-XSRF-TOKEN header, when it is submitted, then it is rejected with 403 [technical]
AC-3: Given an unauthenticated client, when it calls GET /api/me, then the response is 401 with an Error body [technical]
AC-4: Given a successful authentication, when the session cookie is issued, then it is HttpOnly, Secure and SameSite=Strict [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-003, TASK-010
**Blocks**: TASK-012, TASK-020, TASK-060

---

### TASK-012: Registration API: create account
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
POST /api/auth/register (US-001).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RegistrationIT.java`

**Acceptance Criteria**:
AC-1: Given a visitor with an unused email and username and a password that meets the policy (A-01), when they submit registration, then an account is created and the visitor is authenticated. [US-001 AC-1]
AC-2: Given an email that is already registered, when a visitor submits registration with it, then registration is rejected with an "email unavailable" error and no account is created. [US-001 AC-2]
AC-3: Given a username that is already taken, when a visitor submits registration with it, then registration is rejected with a "username unavailable" error and no account is created. [US-001 AC-3]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-011
**Blocks**: TASK-014, TASK-032, TASK-043

---

### TASK-013: SPA foundation: routing, API client with CSRF, Polish strings, auth store
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
React Router routes from ui/components.md, fetch wrapper sending X-XSRF-TOKEN, `pl.ts` strings module, Zustand auth store.

**Canonical test file**: `frontend/src/app/foundation.test.tsx`

**Acceptance Criteria**:
AC-1: Given an unauthenticated visitor, when they open a protected route, then they are redirected to /logowanie [technical]
AC-2: Given the API client, when it sends a mutating request, then the request carries the X-XSRF-TOKEN header from the XSRF-TOKEN cookie [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: None
**Blocks**: TASK-014, TASK-074, TASK-078

---

## Sprint 3

### TASK-014: Register page (walking skeleton UI → API → DB)
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
First end-to-end slice: the register form calls the API, the account is stored, and the user lands on the home page.

**Canonical test file**: `frontend/src/features/auth/RegisterPage.test.tsx`

**Acceptance Criteria**:
AC-1: Given the register page, when the server returns EMAIL_UNAVAILABLE, then the Polish message for that code is shown next to the email field [technical]
AC-2: Given valid registration input, when the form is submitted and the server returns 201, then the user is taken to the home page as authenticated [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-013, TASK-012
**Blocks**: TASK-073

---

### TASK-015: CI quality gates: tests, coverage gate, dependency and security scanning
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Extends the Scaffold CI (ADR-008, success metric 3). Branch protection on `main` is configured by the owner in GitHub settings.

**Canonical test file**: `tests/ops/ci-workflow.test.ts`

**Acceptance Criteria**:
AC-1: Given a pull request, when CI runs, then the workflow executes unit tests, integration tests, dependency and security scanning, and coverage reporting. [US-027 AC-1a]
AC-2: Given a pull request with any failing CI job, when a merge is attempted, then branch protection blocks it. [US-027 AC-1b]
AC-3: Given the domain/game module, when CI reports coverage, then line coverage is at least 90% or the build fails. [US-027 AC-2]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-001
**Blocks**: TASK-051

---

### TASK-016: Game domain: cards, deck, strength, points, marriage values
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
R-001..R-005.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/CardsTest.java`

**Acceptance Criteria**:
AC-1: Given a new deck, when it is created, then it contains exactly the 24 distinct cards 9-A in four suits (R-001) [technical]
AC-2: Given two cards of the same suit, when their strength is compared, then the order is 9 < J < Q < K < 10 < A (R-002) [technical]
AC-3: Given all 24 cards, when their point values are summed, then the total is 120 (R-003) [technical]
AC-4: Given each suit, when its marriage value is read, then it is ♥ 100, ♦ 80, ♣ 60, ♠ 40 (R-004) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-002
**Blocks**: TASK-017

---

### TASK-017: Game domain: VariantConfig and fail-fast on unverified rules
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-006: VariantConfig per player count, RuleOptions with one nullable field per CONFIRM item.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/VariantsTest.java`

**Acceptance Criteria**:
AC-1: Given a player count with at least one required rule option unset, when Variants.forPlayerCount is called, then UnverifiedRuleException is thrown listing every unset option [technical]
AC-2: Given a player count whose required options are all set, when Variants.forPlayerCount is called, then it returns the configuration for that count [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-016
**Blocks**: TASK-018, TASK-053

---

### TASK-018: Game domain: dealing for 2p/3p and dealer rotation
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Seeded Randomness; R-010, R-020, R-021.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/DealTest.java`

**Acceptance Criteria**:
AC-1: Given a 3-player match, when a round is dealt, then each player holds 7 cards and the musik holds 3 cards from a 24-card deck (R-001, R-020). [US-011 AC-1]
AC-2: Given a 2-player match, when a round is dealt, then each player holds 10 cards and there are two musiks of 2 cards each (R-021). [US-011 AC-2]
AC-3: Given a round that ends, when the next round is dealt, then the dealer is the player to the left of the previous dealer (R-010). [US-011 AC-3]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-017
**Blocks**: TASK-019, TASK-039, TASK-058

---

### TASK-019: Game domain: PlayerView projection and hidden information
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
project(state, seat); jqwik property: no hidden card ever appears in another seat's view.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/PlayerViewTest.java`

**Acceptance Criteria**:
AC-1: Given a dealt round, when a player's client receives the game state, then it contains only that player's own cards and not other hands or the musik contents (hidden information). [US-011 AC-4]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-018
**Blocks**: TASK-021

---

### TASK-020: Health endpoint and separated management port
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
GET /api/health (openapi.yaml); Actuator on port 8081 only (ADR-008).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/shared/api/HealthIT.java`

**Acceptance Criteria**:
AC-1: Given the running application, when an unauthenticated client calls GET /api/health, then the response is 200 with status UP [technical]
AC-2: Given the running application, when /actuator/prometheus is requested on the public port 8080, then it is not served, and it is served on management port 8081 [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-011
**Blocks**: TASK-051

---

## Sprint 4

### TASK-021: Game domain: GameEngine.apply, phases, turn and version handling
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Round state machine from game-domain.md.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/GameEngineTest.java`

**Acceptance Criteria**:
AC-1: Given a round in any phase, when an action that is not valid in that phase is applied, then it is rejected with ILLEGAL_PHASE [technical]
AC-2: Given a round, when a seat that is not on turn submits an action, then it is rejected with NOT_YOUR_TURN [technical]
AC-3: Given an accepted action, when the new state is returned, then its version is exactly the previous version plus 1 [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-019
**Blocks**: TASK-022, TASK-039, TASK-056

---

### TASK-022: Game domain: auction rules (R-030..R-033)
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Approved auction rules only. Re-entry and auction end are in AUCTION-OPT (C-04).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/AuctionTest.java`

**Acceptance Criteria**:
AC-1: Given a new round, when bidding starts, then the player to the dealer's left holds an automatic bid of 100 (R-030). [US-012 AC-1]
AC-2: Given a bidding turn, when a player bids a value that isn't a multiple of 10 or doesn't exceed the current bid, then the server rejects the bid (R-031). [US-012 AC-2]
AC-3: Given a player holding no marriage, when they bid above 120, then the server rejects the bid (R-032). [US-012 AC-3]
AC-4: Given a player holding marriages totalling M points, when they bid above 120 + M, then the server rejects the bid (R-033). [US-012 AC-4]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-021
**Blocks**: TASK-023, TASK-024, TASK-034

---

### TASK-023: Game domain: 3p musik, card passing and final contract
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
R-040, R-042.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MusikThreePlayerTest.java`

**Acceptance Criteria**:
AC-1: Given a 3-player declarer, when they take the musik, then its 3 cards are revealed to all players and added to the declarer's hand (R-040). [US-013 AC-1]
AC-2: Given a 3-player declarer holding 10 cards, when they give one card to each opponent, then every player holds 8 cards (R-040). [US-013 AC-2]
AC-3: Given a declarer declaring the final contract, when the declared value is below their winning bid, then the server rejects it (R-042). [US-013 AC-4]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-022
**Blocks**: TASK-025, TASK-034, TASK-042

---

### TASK-024: Game domain: 2p musik choice and discard
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
R-041. Unchosen-musik visibility is in C05-OPT.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MusikTwoPlayerTest.java`

**Acceptance Criteria**:
AC-1: Given a 2-player declarer, when they choose one of the two musiks, then only the chosen musik's cards are revealed to both players (R-041). [US-013 AC-3a]
AC-2: Given a 2-player declarer holding 12 cards after taking a musik, when they discard 2 cards face down, then each player holds 10 cards and the discards are visible to no one (R-041). [US-013 AC-3b]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-022
**Blocks**: TASK-026, TASK-030, TASK-042

---

### TASK-025: Game domain: first lead, follow suit, rejection leaves state unchanged
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
R-060, R-061.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickPlayTest.java`

**Acceptance Criteria**:
AC-1: Given the first trick of a round, when play begins, then only the declarer may lead (R-060). [US-014 AC-1]
AC-2: Given a player holding a card of the led suit, when they play a card of another suit, then the server rejects the move (R-061). [US-014 AC-2]
AC-3: Given a player whose move is rejected, when the rejection is returned, then the game state is unchanged for all players. [US-014 AC-5]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-023
**Blocks**: TASK-027

---

### TASK-026: Implement verified 2p unchosen-musik visibility (C-05) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 1h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path** for 2p.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/UnchosenMusikTest.java`

**Acceptance Criteria**:
AC-1: Given a 2-player round, when the unchosen musik is projected in any PlayerView during and after the round, then its visibility follows C-05 (exact expected behavior = the VERIFIED entry for C-05 in ruleset.md) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-024, TASK-007
**Blocks**: TASK-086

---

## Sprint 5

### TASK-027: Game domain: must beat and trick resolution
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
R-061, R-062, R-063, R-064. Void-suit obligations are in TRICK-OPT (C-12).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickResolutionTest.java`

**Acceptance Criteria**:
AC-1: Given a player holding a card of the led suit that beats the current highest card of that suit, when they play a lower card of that suit, then the server rejects the move (R-061). [US-014 AC-3]
AC-2: Given a completed trick, when the winner is determined, then the strongest trump wins, or if there is no trump the strongest card of the led suit wins, and the winner leads the next trick (R-062, R-063, R-064). [US-014 AC-4]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-025
**Blocks**: TASK-028

---

### TASK-028: Game domain: marriages and trump
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
R-070..R-073. The first-lead marriage rule is in TRICK-OPT (C-13).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/MarriageTest.java`

**Acceptance Criteria**:
AC-1: Given a player on lead holding the Q and K of one suit, when they lead one of them and declare, then the marriage value is added to their round points and that suit becomes trump (R-070, R-071, R-072). [US-015 AC-1]
AC-2: Given an active trump, when a new marriage is declared, then the new suit replaces the previous trump (R-071). [US-015 AC-2]
AC-3: Given a player who doesn't hold both the Q and K of a suit, when they try to declare a marriage in it, then the server rejects the declaration. [US-015 AC-3]
AC-4: Given a defender on lead holding a marriage, when they declare it, then the declaration is accepted and scored for that defender (R-073). [US-015 AC-4]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-027
**Blocks**: TASK-029, TASK-035

---

### TASK-029: Game domain: contract result and defender rounding
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
R-080, R-082, R-083, R-084.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/RoundScoringTest.java`

**Acceptance Criteria**:
AC-1: Given a declarer whose card points plus declared marriages are at least the contract, when the round is scored, then the contract is added to their score (R-082, R-083). [US-016 AC-1]
AC-2: Given a declarer whose card points plus declared marriages are below the contract, when the round is scored, then the contract is subtracted from their score (R-082). [US-016 AC-2]
AC-3: Given a defender whose card points plus marriages end in 5 to 9, when the round is scored, then the value is rounded up to the next multiple of 10 (R-084). [US-016 AC-3a]
AC-4: Given a defender whose card points plus marriages end in 1 to 4, when the round is scored, then the value is rounded down to the previous multiple of 10 (R-084). [US-016 AC-3b]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-028
**Blocks**: TASK-030, TASK-037

---

### TASK-030: Game domain: 2p leftover musik points and barrel
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
R-081, R-100, R-101, R-102.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BarrelScoringTest.java`

**Acceptance Criteria**:
AC-1: Given a 2-player round, when it is scored, then the card points in both musik piles (the unchosen musik and the declarer's discards) go to the winner of the last trick (R-081). [US-016 AC-4]
AC-2: Given a player with 800 or more points who is a defender, when the round is scored, then their score doesn't increase (R-100). [US-016 AC-5]
AC-3: Given a player with 800 or more points who fails as declarer, when the round is scored, then the contract is subtracted (R-102). [US-016 AC-6]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-029, TASK-024
**Blocks**: TASK-031, TASK-036, TASK-040

---

### TASK-031: Game domain: end of game at 1000 and tie-break
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
R-110, R-111, R-112.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/EndOfGameTest.java`

**Acceptance Criteria**:
AC-1: Given a round after which exactly one player has 1000 or more points, when it is scored, then the match ends with that player as the winner (R-110). [US-019 AC-1]
AC-2: Given several players reaching 1000 in the same round, one of whom was the declarer, when it is scored, then the declarer wins (R-111). [US-019 AC-2]
AC-3: Given several players reaching 1000 in the same round, none of whom was the declarer, with different totals, when the winner is determined, then the player with the higher total wins (R-111, R-112). [US-019 AC-3a]
AC-4: Given several players reaching 1000 in the same round, none of whom was the declarer, with equal totals, when the winner is determined, then the match ends in a draw (R-111). [US-019 AC-3b]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-030
**Blocks**: TASK-033, TASK-064

---

### TASK-032: Registration: password policy and Argon2id storage
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
A-01, ADR-003.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RegistrationPolicyIT.java`

**Acceptance Criteria**:
AC-1: Given a password shorter than the minimum length (A-01), when a visitor submits registration, then registration is rejected with a password-policy error. [US-001 AC-4]
AC-2: Given a successful registration, when the stored account record is inspected, then the password is stored only as an Argon2id hash and the only personal fields are email and username. [US-001 AC-5]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-012
**Blocks**: None

---

## Sprint 6

### TASK-033: Game domain: legalActions query
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Used by the UI hint (PlayerView.legalActions) and by the timeout fallback (ADR-009).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/LegalActionsTest.java`

**Acceptance Criteria**:
AC-1: Given any reachable state and seat, when legalActions is computed, then every listed action is accepted by apply and no unlisted action of the same type is accepted (jqwik property) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-031
**Blocks**: TASK-041

---

### TASK-034: Implement verified auction options (C-04, C-07) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path**: blocked by Kurnik verification. The AC text is completed with the verified behavior before RED.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/AuctionOptionsTest.java`

**Acceptance Criteria**:
AC-1: Given every other player has passed after the automatic 100, when bidding ends, then the opener is the declarer at 100 (R-035). [US-012 AC-5]
AC-2: Given a player who has passed, when they later submit a bid in the same auction, then the engine applies the C-04 behavior (exact expected behavior = the VERIFIED entry for C-04 in ruleset.md) [technical]
AC-3: Given a declarer after taking the musik, when they declare a final contract above the bid cap, then the engine applies the C-07 behavior (exact expected behavior = the VERIFIED entry for C-07 in ruleset.md) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-022, TASK-023, TASK-005
**Blocks**: TASK-086

---

### TASK-035: Implement verified trick options (C-12, C-13) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path**: blocked by Kurnik verification.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TrickOptionsTest.java`

**Acceptance Criteria**:
AC-1: Given a player void in the led suit holding a trump while a trump is active, when they play a non-trump, then the engine applies C-12 (i) (exact expected behavior = the VERIFIED entry for C-12 in ruleset.md) [technical]
AC-2: Given a trump already in the trick and a player void in the led suit holding a higher trump, when they play a lower trump, then the engine applies C-12 (ii) (exact expected behavior = the VERIFIED entry for C-12 in ruleset.md) [technical]
AC-3: Given a trick already trumped and a player able to follow the led suit, when they play a lower card of the led suit, then the engine applies C-12 (iii) (exact expected behavior = the VERIFIED entry for C-12 in ruleset.md) [technical]
AC-4: Given the declarer on the first lead of a round holding a marriage, when they lead its Q or K and declare, then the engine applies the C-13 behavior (exact expected behavior = the VERIFIED entry for C-13 in ruleset.md) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-028, TASK-006
**Blocks**: TASK-086

---

### TASK-036: Implement verified 4-player variant: sitting out, deal, musik, sit-out scoring (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3.5h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path** for 4p. R-091 (aces 50 + marriages) is approved; C-01, C-02 and C-06 come from verification.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/FourPlayerVariantTest.java`

**Acceptance Criteria**:
AC-1: Given a 4-player round, when it starts, then the player sitting out is the one defined by C-01 (exact expected behavior = the VERIFIED entry for C-01 in ruleset.md) [technical]
AC-2: Given a 4-player round, when it is dealt, then the active players' hands and the musik follow C-02 (exact expected behavior = the VERIFIED entry for C-02 in ruleset.md) [technical]
AC-3: Given a 4-player declarer, when they take the musik, then musik handling follows C-06 (exact expected behavior = the VERIFIED entry for C-06 in ruleset.md) [technical]
AC-4: Given a 4-player round whose musik contains an ace and the ♥ marriage, when the round is scored, then the player sitting out scores 150 (R-091) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-030, TASK-007
**Blocks**: TASK-038, TASK-040, TASK-084

---

### TASK-037: Implement bomb (R-050, R-051) with verified decision point (C-09) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path**: blocked by Kurnik verification of C-08 and C-09.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BombTest.java`

**Acceptance Criteria**:
AC-1: Given a declarer who throws a bomb, when the round is scored, then the declarer's score is unchanged (R-050). [US-017 AC-1]
AC-2: Given the first bomb that qualifies as free, when it is thrown, then no opponent receives points (R-051). [US-017 AC-2]
AC-3: Given a subsequent bomb in a 2- or 3-player match, when it is thrown, then each opponent receives 60 points (R-051). [US-017 AC-3]
AC-4: Given a declarer at a point where C-09 does not allow a bomb, when they throw a bomb, then it is rejected (exact expected behavior = the VERIFIED entry for C-09 in ruleset.md) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-029, TASK-008
**Blocks**: TASK-038, TASK-082

---

### TASK-038: Implement verified bomb options (C-08, C-10, C-11) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 2h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path**.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/BombOptionsTest.java`

**Acceptance Criteria**:
AC-1: Given a second bomb in the same game, when it is thrown, then whether it is free follows C-08 (exact expected behavior = the VERIFIED entry for C-08 in ruleset.md) [technical]
AC-2: Given a 4-player round, when the declarer throws a penalized bomb, then the sitting-out player's points follow C-10 (exact expected behavior = the VERIFIED entry for C-10 in ruleset.md) [technical]
AC-3: Given an opponent on the barrel, when the declarer throws a penalized bomb, then that opponent's points follow C-11 (exact expected behavior = the VERIFIED entry for C-11 in ruleset.md) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-037, TASK-036
**Blocks**: TASK-086

---

## Sprint 7

### TASK-039: Implement four-9s redeal with verified procedure (C-03) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path**.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/RedealTest.java`

**Acceptance Criteria**:
AC-1: Given a player holding all four 9s, when they request a redeal, then the round is redealt (R-022). [US-018 AC-1]
AC-2: Given a player not holding all four 9s, when they request a redeal, then the server rejects the request. [US-018 AC-2]
AC-3: Given a player holding all four 9s, when they request a redeal outside the moment C-03 allows, then it is rejected (exact expected behavior = the VERIFIED entry for C-03 in ruleset.md) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-018, TASK-021, TASK-009
**Blocks**: TASK-082

---

### TASK-040: Implement verified scoring options (R-085, R-103) (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 2h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path**.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/ScoringOptionsTest.java`

**Acceptance Criteria**:
AC-1: Given a declarer with 50 points who fails a contract of 120, when the round is scored, then the resulting score follows R-085 (exact expected behavior = the VERIFIED entry for R-085 in ruleset.md) [technical]
AC-2: Given a 4-player sitting-out player on the barrel whose musik contains an ace, when the round is scored, then their score follows R-103 (exact expected behavior = the VERIFIED entry for R-103 in ruleset.md) [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-030, TASK-036, TASK-009
**Blocks**: TASK-086

---

### TASK-041: Timeout fallback (ADR-009): bidding, 2p musik choice, card play
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Technical fallback in `game.timeout`, not a rule. Lowest card = rank strength, then technical suit tie-break (ADR-009 implementation detail).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/timeout/TimeoutFallbackTest.java`

**Acceptance Criteria**:
AC-1: Given a bidding turn, when the fallback chooses a move, then it is PASS [technical]
AC-2: Given a 2-player musik choice, when the fallback chooses a move, then it is the first musik in the engine's internal ordering [technical]
AC-3: Given a trick-play turn, when the fallback chooses a move, then it plays the legal card with the lowest rank strength, and among equal strength the first in the technical suit order ♠, ♣, ♦, ♥ [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-033
**Blocks**: TASK-042

---

### TASK-042: Timeout fallback (ADR-009): discard, card passing, contract, optional actions
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Never bomb, marriage or redeal automatically.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/game/timeout/TimeoutFallbackOptionalTest.java`

**Acceptance Criteria**:
AC-1: Given a 2-player discard, when the fallback chooses a move, then it discards the 2 lowest legal cards by the ADR-009 ordering [technical]
AC-2: Given 3p/4p card passing, when the fallback chooses a move, then it gives the lowest legal cards by the ADR-009 ordering, assigned in seat order to the left [technical]
AC-3: Given a final contract declaration, when the fallback chooses a move, then it declares exactly the winning bid [technical]
AC-4: Given any decision point, when the fallback chooses a move, then it is never THROW_BOMB, a marriage declaration or REQUEST_REDEAL [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-041, TASK-023, TASK-024
**Blocks**: TASK-068

---

### TASK-043: Login, logout and session invalidation
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
POST /api/auth/login, /api/auth/logout, GET /api/me.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/LoginIT.java`

**Acceptance Criteria**:
AC-1: Given a registered player, when they submit a correct email and password, then they are authenticated and can reach protected pages. [US-002 AC-1]
AC-2: Given a registered player, when they submit a wrong password, then login fails with a generic "invalid credentials" error that doesn't reveal whether the email exists. [US-002 AC-2]
AC-3: Given an authenticated player, when they log out, then their session or token no longer authorizes protected REST or WebSocket requests. [US-002 AC-4]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-012
**Blocks**: TASK-044, TASK-045, TASK-053, TASK-054

---

### TASK-044: Rate limiting on authentication endpoints
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
Bucket4j in memory (ADR-007, A-02).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/RateLimitIT.java`

**Acceptance Criteria**:
AC-1: Given repeated failed login attempts exceeding the configured rate limit (A-02), when another attempt is made, then it is rejected with HTTP 429 without checking the credentials. [US-002 AC-3]
AC-2: Given repeated password-reset requests exceeding the configured limit, when another request is made, then it is rejected with HTTP 429 [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-043
**Blocks**: None

---

## Sprint 8

### TASK-045: Password reset request and email
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Hashed single-use tokens, and no enumeration (A-03, A-04). Tests use a local SMTP test server.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/PasswordResetRequestIT.java`

**Acceptance Criteria**:
AC-1: Given a registered email, when a reset is requested, then one email containing a single-use reset link is sent through the SMTP provider. [US-003 AC-1]
AC-2: Given an unregistered email, when a reset is requested, then the response is identical to the registered-email response and no email is sent. [US-003 AC-2]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-043
**Blocks**: TASK-046, TASK-047, TASK-048

---

### TASK-046: Password reset confirmation and session revocation
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Revoke all sessions via the principal-name index (ADR-003).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/identity/api/PasswordResetConfirmIT.java`

**Acceptance Criteria**:
AC-1: Given a valid, unexpired reset token, when the player submits a new password that meets the policy, then the new password works for login and the old one no longer does. [US-003 AC-3]
AC-2: Given a reset token that is already used or older than 30 minutes (A-03), when it is submitted, then the reset is rejected and the password is unchanged. [US-003 AC-4]
AC-3: Given a successful password reset, when a session issued before the reset is used, then it is rejected (A-03). [US-003 AC-5]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-045
**Blocks**: TASK-073

---

### TASK-047: Mail retry and failure metric
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-003 SMTP failure behavior.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/identity/infrastructure/MailDeliveryIT.java`

**Acceptance Criteria**:
AC-1: Given the SMTP server fails twice then succeeds, when a reset email is sent, then it is delivered on the third attempt [technical]
AC-2: Given the SMTP server fails on all 3 attempts, when a reset email is sent, then mail_send_failures_total increases by 1 and the logged error contains no token [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-045
**Blocks**: None

---

### TASK-048: Retention job for expired reset tokens
**Priority**: P1 | **Estimate**: 1h | **Status**: Ready | **Owner**: dev

**Description**:
persistence.md retention table.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/identity/infrastructure/ResetTokenRetentionIT.java`

**Acceptance Criteria**:
AC-1: Given reset tokens that expired more than 24 h ago, when the daily retention job runs, then they are deleted and unexpired tokens remain [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-045
**Blocks**: None

---

### TASK-049: External accounts: domain, VPS, SMTP provider
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: owner

**Description**:
OWNER TASK. Buy the domain (owner decision, Plan Q2), order the Hetzner 8 GB tier after checking the current price (ADR-008), and create the EU SMTP provider account (tech-stack.md). Total must fit €10/month.

**Canonical test file**: `tests/ops/external-setup.test.ts`

**Acceptance Criteria**:
AC-1: Given the registered domain, when its DNS A record is resolved, then it points to the VPS IP address [technical]
AC-2: Given the SMTP provider credentials in the VPS .env, when a test email is sent through the provider, then it is accepted [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: None
**Blocks**: TASK-050

---

### TASK-050: VPS hardening and Compose production stack
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-008 networking: firewall 22/80/443, key-only SSH, restricted deploy user.

**Canonical test file**: `tests/ops/vps-hardening.test.ts`

**Acceptance Criteria**:
AC-1: Given the VPS, when its open TCP ports are scanned from outside, then only 22, 80 and 443 are open [technical]
AC-2: Given the VPS SSH service, when a password login is attempted, then it is refused [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-049
**Blocks**: TASK-051

---

### TASK-051: Deploy workflow with smoke check and automatic rollback
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-008 delivery.

**Canonical test file**: `tests/ops/deploy-workflow.test.ts`

**Acceptance Criteria**:
AC-1: Given a merge to `main`, when the deploy workflow runs, then the new version is running on the VPS without manual SSH steps. [US-027 AC-3]
AC-2: Given a deployed version whose smoke check fails for 60 s, when the deploy workflow runs, then the previous image tag is restored and the workflow fails [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-050, TASK-015, TASK-020
**Blocks**: TASK-052, TASK-091

---

## Sprint 9

### TASK-052: Caddy HTTPS and routing
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
Caddy serves the SPA and proxies /api and /ws; Actuator not routed.

**Canonical test file**: `tests/ops/https.test.ts`

**Acceptance Criteria**:
AC-1: Given the production domain, when it is requested over HTTP, then it redirects to HTTPS with a valid certificate. [US-027 AC-4]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-051
**Blocks**: TASK-089, TASK-092

---

### TASK-053: Create private table and available player counts
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
POST /api/tables, GET /api/tables/available-player-counts (ADR-006).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/CreateTableIT.java`

**Acceptance Criteria**:
AC-1: Given an authenticated player who is not seated elsewhere (A-10), when they create a table with a player count of 2, 3 or 4, then a table is created with that capacity, the creator seated as host, and a join code and invite link returned. [US-006 AC-1]
AC-2: Given a create request with a player count other than 2, 3 or 4, when it is submitted, then it is rejected with a validation error. [US-006 AC-2]
AC-3: Given a player already seated at another active table or match, when they try to create a table, then the request is rejected. [US-006 AC-3]
AC-4: Given a player count whose variant configuration is incomplete, when GET /api/tables/available-player-counts is called, then that count is not listed and creating a table for it returns 409 PLAYER_COUNT_UNAVAILABLE [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)
- [ ] Write failing canonical test for AC-4
- [ ] Implement AC-4 (red→green)

**Blocked by**: TASK-043, TASK-017
**Blocks**: TASK-054, TASK-055, TASK-076

---

### TASK-054: Account deletion: password confirmation and seated guard
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
DELETE /api/me (A-14).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/user/api/AccountDeletionIT.java`

**Acceptance Criteria**:
AC-1: Given an authenticated player who confirms with their current password (A-14), when they delete their account, then their email, username and password hash no longer exist in the database. [US-004 AC-1]
AC-2: Given a deleted account, when its former credentials are used to log in, then login fails with the generic invalid-credentials error. [US-004 AC-2]
AC-3: Given a player seated at an active table or match, when they request account deletion, then deletion is rejected until they have left or the match has ended (A-14). [US-004 AC-5]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-043, TASK-053
**Blocks**: TASK-072, TASK-075

---

### TASK-055: Join table by code: errors
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
GET /api/tables/{code}, POST /api/tables/{code}/join.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/JoinTableIT.java`

**Acceptance Criteria**:
AC-1: Given a table whose seats are all taken, when another player tries to join, then the join is rejected with a "table full" error. [US-007 AC-3]
AC-2: Given a code that doesn't match any open table, when a player submits it, then the join is rejected with a "table not found" error. [US-007 AC-4]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-053
**Blocks**: TASK-058, TASK-059

---

### TASK-056: MatchStateStore: snapshot on start and per accepted action
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-004 write-through snapshot.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/infrastructure/MatchStateStoreIT.java`

**Acceptance Criteria**:
AC-1: Given a match that has just started, when the store is inspected, then a match_state_snapshot row with version 0 exists [technical]
AC-2: Given an accepted action, when it is processed, then the snapshot version increases by 1 in the same transaction as the in-memory swap [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-003, TASK-021
**Blocks**: TASK-057, TASK-058

---

### TASK-057: Match action pipeline: optimistic version and idempotency
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-004 serial executor, expectedVersion, clientActionId.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ActionPipelineIT.java`

**Acceptance Criteria**:
AC-1: Given an action with an expectedVersion different from the current version, when it is processed, then it is rejected with STALE_VERSION and the state is unchanged [technical]
AC-2: Given an action whose clientActionId was already processed for that seat, when it is received again, then it is ignored and the version does not change [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-056
**Blocks**: TASK-061, TASK-064, TASK-066

---

## Sprint 10

### TASK-058: Start match (host only, full table)
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
POST /api/tables/{code}/start creates the match and the first snapshot.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/StartMatchIT.java`

**Acceptance Criteria**:
AC-1: Given a table with all seats filled, when the host starts the match, then a match begins for that player count with a server-chosen random first dealer (R-010). [US-008 AC-1]
AC-2: Given a table with at least one empty seat, when the host tries to start, then the start is rejected. [US-008 AC-2]
AC-3: Given a seated non-host player, when they try to start the match, then the start is rejected as unauthorized. [US-008 AC-3]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-055, TASK-056, TASK-018
**Blocks**: TASK-060

---

### TASK-059: Host leaving closes the table
**Priority**: P1 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
A-15.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/LeaveTableIT.java`

**Acceptance Criteria**:
AC-1: Given the host of an unstarted table, when they leave, then the table is closed and its join code no longer admits players (A-15). [US-009 AC-2]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-055
**Blocks**: TASK-062

---

### TASK-060: STOMP endpoint: cookie handshake, origin check, membership interceptor
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-005.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/api/StompSecurityIT.java`

**Acceptance Criteria**:
AC-1: Given a WebSocket handshake to /ws without a valid session cookie, when it is attempted, then it is rejected [technical]
AC-2: Given an authenticated user who is not a participant of a match, when they subscribe to that match's view destination, then the subscription is refused with FORBIDDEN [technical]
AC-3: Given a handshake with a foreign Origin header, when it is attempted, then it is rejected [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-011, TASK-058
**Blocks**: TASK-061, TASK-062

---

### TASK-061: STOMP action handling and per-seat view push
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
/app/matches/{id}/actions → pipeline → /user/queue/.../view; rejections to /user/queue/errors.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchRealtimeIT.java`

**Acceptance Criteria**:
AC-1: Given a match in progress, when a player makes a legal move, then every connected participant receives the resulting state update over WebSocket without refreshing. [US-021 AC-1]
AC-2: Given an illegal action sent over STOMP, when it is processed, then the sender receives ActionRejected with a stable code on /user/queue/errors and no view is pushed [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-060, TASK-057
**Blocks**: TASK-063, TASK-065, TASK-068, TASK-078, TASK-089, TASK-092

---

### TASK-062: Realtime seat list updates
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
/topic/tables/{tableId}.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/lobby/api/SeatUpdatesIT.java`

**Acceptance Criteria**:
AC-1: Given an open table with a free seat, when an authenticated player submits its join code, then they are seated and all seated players see the updated seat list. [US-007 AC-1]
AC-2: Given a seated non-host player at an unstarted table, when they leave, then their seat becomes free and the other players see the updated seat list. [US-009 AC-1]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-060, TASK-059
**Blocks**: TASK-077

---

### TASK-063: REST view and current-activity endpoints
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
GET /api/matches/{id}/view, GET /api/me/current.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchViewIT.java`

**Acceptance Criteria**:
AC-1: Given a participant of a live match, when they call GET /api/matches/{id}/view, then they receive the same PlayerView as the STOMP view for their seat [technical]
AC-2: Given a user who is not a participant, when they call GET /api/matches/{id}/view, then the response is 403 [technical]
AC-3: Given a user seated in a live match, when they call GET /api/me/current, then the response contains that matchId [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-061
**Blocks**: TASK-076

---

### TASK-064: Record match results and delete the snapshot at match end
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-004 cleanup.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/application/MatchResultRecordingIT.java`

**Acceptance Criteria**:
AC-1: Given a match that ends at 1000, when it is recorded, then its status is `COMPLETED` with every participant's final score. [US-019 AC-4]
AC-2: Given a match that ends, when its result is recorded, then its match_state_snapshot row no longer exists [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-057, TASK-031
**Blocks**: TASK-069, TASK-070, TASK-071

---

## Sprint 11

### TASK-065: Presence: disconnect marking and grace timer
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
STOMP SessionDisconnectEvent → graceDeadline.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/application/PresenceIT.java`

**Acceptance Criteria**:
AC-1: Given a player who disconnects, when the other participants view the table, then they see that player marked as disconnected with the remaining grace time. [US-023 AC-3]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-061
**Blocks**: TASK-066, TASK-067, TASK-069

---

### TASK-066: Restore live matches after restart
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-004 startup recovery.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/infrastructure/MatchRecoveryIT.java`

**Acceptance Criteria**:
AC-1: Given an in-progress match snapshot, when the application restarts, then the match is restored and each seat's PlayerView equals the view before the restart [technical]
AC-2: Given a turn deadline with 40 s remaining at shutdown, when the match is restored, then the turn timer resumes with 40 s remaining [technical]
AC-3: Given disconnect grace periods at shutdown, when the match is restored, then each grace period restarts at the moment of recovery [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-057, TASK-065
**Blocks**: None

---

### TASK-067: Reconnect within the grace period
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Success metric 2.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ReconnectIT.java`

**Acceptance Criteria**:
AC-1: Given a player who disconnects mid-match, when they reconnect within 2 minutes, then they receive the full current game state for their seat and can continue playing. [US-023 AC-1]
AC-2: Given a player who reconnected within the grace period, when their state is compared with the state before the disconnect, then no cards, scores or turn information were lost. [US-023 AC-2]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-065
**Blocks**: None

---

### TASK-068: Turn timers with automatic fallback moves
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
60 s timers (A-09); triggers game.timeout fallback (ADR-009).

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/application/TurnTimerIT.java`

**Acceptance Criteria**:
AC-1: Given a connected player whose 60-second bidding turn timer (A-09) expires and who is allowed to pass, when the timer elapses, then the server passes for them. [US-022 AC-1]
AC-2: Given a connected player whose 60-second trick-play turn timer (A-09) expires, when the timer elapses, then the server plays their lowest legal card. [US-022 AC-2]
AC-3: Given a player whose turn timer expired once, when the automatic move has been made, then the match continues and the player is still seated. [US-022 AC-3]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-061, TASK-042
**Blocks**: TASK-086, TASK-092

---

### TASK-069: Forfeit and abandonment after the grace period
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
A-08.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/application/ForfeitIT.java`

**Acceptance Criteria**:
AC-1: Given a disconnected player who doesn't reconnect within 2 minutes, when the grace period expires, then the match ends with status `FORFEIT`, that player is recorded as forfeiting, and the remaining players are recorded as winners by forfeit. [US-024 AC-1]
AC-2: Given a match ended by forfeit, when it is recorded, then participants, status, timestamp and the scores at that moment are stored. [US-024 AC-2]
AC-3: Given all remaining participants disconnected beyond the grace period, when the last grace period expires, then the match ends with status `ABANDONED` (A-08). [US-024 AC-3]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-065, TASK-064
**Blocks**: None

---

### TASK-070: Statistics-eligible match query
**Priority**: P1 | **Estimate**: 1h | **Status**: Ready | **Owner**: dev

**Description**:
US-026; internal query only.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/infrastructure/StatsEligibleQueryIT.java`

**Acceptance Criteria**:
AC-1: Given stored matches with statuses `COMPLETED`, `FORFEIT` and `ABANDONED`, when the statistics-eligible match query runs, then only `COMPLETED` matches are returned. [US-026 AC-1]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-064
**Blocks**: None

---

## Sprint 12

### TASK-071: Match history API
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
GET /api/me/matches, GET /api/matches/{id}.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/match/api/MatchHistoryIT.java`

**Acceptance Criteria**:
AC-1: Given a player with finished matches, when they open their history, then each match shows its date, player count, participants, status and final scores, newest first. [US-025 AC-1]
AC-2: Given a player's history, when it is requested, then it contains only matches that player took part in. [US-025 AC-2]
AC-3: Given a match with status `FORFEIT` or `ABANDONED`, when it appears in history, then its status is shown explicitly and distinguished from `COMPLETED`. [US-025 AC-3]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-064
**Blocks**: TASK-072, TASK-085, TASK-086

---

### TASK-072: Account deletion: anonymized shared history
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
"Deleted player" (US-004); no identity snapshot.

**Canonical test file**: `backend/src/test/java/com/lukaszpelikan/thousand/user/api/AccountDeletionHistoryIT.java`

**Acceptance Criteria**:
AC-1: Given a completed match that included the deleted player, when another participant views that match in their history, then the deleted player's seat shows "Deleted player" with the original score and status. [US-004 AC-3]
AC-2: Given a deleted account, when all remaining match history records are inspected, then none contains the deleted user's id, email or username. [US-004 AC-4]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-054, TASK-071
**Blocks**: None

---

### TASK-073: Login, logout, password reset pages
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Polish UI; server error codes mapped to text.

**Canonical test file**: `frontend/src/features/auth/AuthPages.test.tsx`

**Acceptance Criteria**:
AC-1: Given the login page, when the server returns INVALID_CREDENTIALS, then the generic Polish invalid-credentials message is shown [technical]
AC-2: Given the forgot-password page, when any email is submitted and the server returns 202, then the same Polish confirmation message is shown [technical]
AC-3: Given the reset page with a token, when the server returns RESET_TOKEN_INVALID, then the Polish expired-or-used link message is shown [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-014, TASK-046
**Blocks**: TASK-075, TASK-076, TASK-085

---

### TASK-074: Privacy policy page
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
Static page (story-coverage.md).

**Canonical test file**: `frontend/src/features/legal/PrivacyPolicyPage.test.tsx`

**Acceptance Criteria**:
AC-1: Given an unauthenticated visitor on the registration page, when they follow the privacy policy link, then the privacy policy page is displayed without logging in. [US-005 AC-1]
AC-2: Given the privacy policy page, when it is displayed, then it lists the stored data (email, username, password hash, match history), the deletion option, and a contact address for access requests (A-11). [US-005 AC-2]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-013
**Blocks**: None

---

### TASK-075: Account page with deletion
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
DELETE /api/me with password confirmation.

**Canonical test file**: `frontend/src/features/profile/AccountPage.test.tsx`

**Acceptance Criteria**:
AC-1: Given the account page, when the user confirms deletion with their password and the server returns 204, then they are logged out and shown the login page [technical]
AC-2: Given the account page, when the server returns SEATED_CANNOT_DELETE, then the Polish message explaining the seated restriction is shown [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-073, TASK-054
**Blocks**: None

---

### TASK-076: Home, empty state, create and join forms
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
US-010; unavailable player counts disabled (ADR-006).

**Canonical test file**: `frontend/src/features/lobby/Home.test.tsx`

**Acceptance Criteria**:
AC-1: Given a player with no match history, when they open their home or history page, then an empty state is shown with "Create table" and "Join with code" actions. [US-010 AC-1]
AC-2: Given available player counts [2, 3], when the create-table form is shown, then the 4-player option is disabled with an explanation [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-073, TASK-053, TASK-063
**Blocks**: TASK-077

---

## Sprint 13

### TASK-077: Table room and invite-link flow
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
US-007 AC-2 is verified end to end.

**Canonical test file**: `frontend/e2e/invite-link.spec.ts`

**Acceptance Criteria**:
AC-1: Given an unauthenticated visitor opening an invite link, when they log in, then they are returned to that table's join flow. [US-007 AC-2]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-076, TASK-062
**Blocks**: TASK-086

---

### TASK-078: STOMP client service and reconnect banner
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
@stomp/stompjs with backoff; the client replaces local state with each view.

**Canonical test file**: `frontend/src/services/websocket/stompClient.test.ts`

**Acceptance Criteria**:
AC-1: Given an active match subscription, when the connection drops, then the reconnect banner is shown and the client resubscribes after reconnecting [technical]
AC-2: Given a received PlayerView, when it is applied, then the game store state equals that view exactly [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-013, TASK-061
**Blocks**: TASK-079

---

### TASK-079: Game table: layout, hand and trick play
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Cards not in legalActions are dimmed (UX hint only).

**Canonical test file**: `frontend/src/features/game/TrickPlay.test.tsx`

**Acceptance Criteria**:
AC-1: Given a view where it is my turn in trick play, when I click a card listed in legalActions, then a PLAY_CARD intention with that card and the current version is sent [technical]
AC-2: Given a view, when it is rendered, then cards not in legalActions are dimmed and not clickable [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-078
**Blocks**: TASK-080, TASK-081, TASK-083, TASK-084

---

### TASK-080: Game table: bidding and contract panels
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Game table: bidding and contract panels

**Canonical test file**: `frontend/src/features/game/BiddingPanel.test.tsx`

**Acceptance Criteria**:
AC-1: Given my bidding turn, when I choose Pass, then a PASS intention is sent [technical]
AC-2: Given my bidding turn, when I enter a bid value, then a BID intention with that value is sent [technical]
AC-3: Given the contract phase as declarer, when I declare a value, then a DECLARE_CONTRACT intention with that value is sent [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-079
**Blocks**: TASK-082

---

### TASK-081: Game table: musik and card passing (2p, 3p)
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Game table: musik and card passing (2p, 3p)

**Canonical test file**: `frontend/src/features/game/MusikPanels.test.tsx`

**Acceptance Criteria**:
AC-1: Given the 2-player musik phase as declarer, when I choose a musik and then select 2 cards to discard, then CHOOSE_MUSIK and DISCARD intentions are sent in that order [technical]
AC-2: Given the 3-player card-passing phase as declarer, when I assign one card to each opponent, then a GIVE_CARDS intention with both assignments is sent [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-079
**Blocks**: TASK-086

---

### TASK-082: Game table: marriage toggle, bomb and redeal controls (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 2h | **Status**: Blocked | **Owner**: dev

**Description**:
Controls shown only when listed in legalActions.

**Canonical test file**: `frontend/src/features/game/OptionalActions.test.tsx`

**Acceptance Criteria**:
AC-1: Given my lead holding a marriage, when I play its Q or K with the marriage toggle on, then PLAY_CARD is sent with declareMarriage true [technical]
AC-2: Given THROW_BOMB or REQUEST_REDEAL in legalActions, when the view is rendered, then the corresponding button is shown, and otherwise it is hidden [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-080, TASK-037, TASK-039
**Blocks**: TASK-086

---

## Sprint 14

### TASK-083: Game table: timer, scoreboard, round summary, game over, event line
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Game table: timer, scoreboard, round summary, game over, event line

**Canonical test file**: `frontend/src/features/game/GameStatus.test.tsx`

**Acceptance Criteria**:
AC-1: Given a view with a turn deadline less than 15 s away, when it is rendered, then the timer shows the warning state [technical]
AC-2: Given a view with phase GAME_OVER, when it is rendered, then the game-over dialog shows the winner or draw and all final scores [technical]
AC-3: Given a view whose last event is an automatic move, when it is rendered, then the event line shows "Ruch automatyczny (czas minął)" [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)
- [ ] Write failing canonical test for AC-3
- [ ] Implement AC-3 (red→green)

**Blocked by**: TASK-079
**Blocks**: TASK-086

---

### TASK-084: Game table: 4-player layout and sitting-out seat (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 2h | **Status**: Blocked | **Owner**: dev

**Description**:
Game table: 4-player layout and sitting-out seat

**Canonical test file**: `frontend/src/features/game/FourPlayerLayout.test.tsx`

**Acceptance Criteria**:
AC-1: Given a 4-player view, when it is rendered, then four seats are shown and the sitting-out seat is greyed out [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-079, TASK-036
**Blocks**: TASK-088

---

### TASK-085: Match history page
**Priority**: P0 | **Estimate**: 2h | **Status**: Ready | **Owner**: dev

**Description**:
Match history page

**Canonical test file**: `frontend/src/features/profile/MatchHistory.test.tsx`

**Acceptance Criteria**:
AC-1: Given a history page with a FORFEIT match, when it is rendered, then the match shows a FORFEIT badge distinct from COMPLETED [technical]
AC-2: Given a participant with deleted true, when the history is rendered, then that seat shows "Gracz usunięty" [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-073, TASK-071
**Blocks**: None

---

### TASK-086: E2E: complete 2-player match (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3h | **Status**: Blocked | **Owner**: dev

**Description**:
Seeded deck in the test profile (game-domain.md Randomness). **Critical path**: needs all rules that apply to 2p verified and implemented.

**Canonical test file**: `frontend/e2e/full-match-2p.spec.ts`

**Acceptance Criteria**:
AC-1: Given a 2-player table, when a full match is played through the web UI until someone reaches 1000, then the match ends as `COMPLETED` (Playwright). [US-020 AC-1]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-081, TASK-083, TASK-082, TASK-077, TASK-034, TASK-035, TASK-026, TASK-038, TASK-040, TASK-068, TASK-071
**Blocks**: TASK-087

---

### TASK-087: E2E: complete 3-player match (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path**.

**Canonical test file**: `frontend/e2e/full-match-3p.spec.ts`

**Acceptance Criteria**:
AC-1: Given a 3-player table, when a full match is played through the web UI until someone reaches 1000, then the match ends as `COMPLETED` (Playwright). [US-020 AC-2]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-086
**Blocks**: TASK-088

---

### TASK-088: E2E: complete 4-player match (Critical path: Kurnik)
**Priority**: P0 | **Estimate**: 3h | **Status**: Blocked | **Owner**: dev

**Description**:
**Critical path**: needs 4p verification (C-01, C-02, C-06, C-10, R-103).

**Canonical test file**: `frontend/e2e/full-match-4p.spec.ts`

**Acceptance Criteria**:
AC-1: Given a 4-player table, when a full match is played through the web UI until someone reaches 1000, then the match ends as `COMPLETED` (Playwright). [US-020 AC-3]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-087, TASK-084
**Blocks**: None

---

## Sprint 15

### TASK-089: Metrics in Prometheus and Grafana dashboards
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Observability phase 1 (ADR-008).

**Canonical test file**: `tests/ops/observability-metrics.test.ts`

**Acceptance Criteria**:
AC-1: Given the production deployment, when the operator opens Grafana, then application metrics (HTTP latency, active WebSocket sessions, active matches) are visible from Prometheus. [US-028 AC-1]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-052, TASK-061
**Blocks**: TASK-090

---

### TASK-090: Structured logs to Loki via Alloy, secret masking
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Observability phase 1.

**Canonical test file**: `tests/ops/observability-logs.test.ts`

**Acceptance Criteria**:
AC-1: Given the production deployment, when the operator searches logs in Grafana, then application logs from Loki are available and contain no passwords, tokens or secrets. [US-028 AC-2]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-089
**Blocks**: TASK-093

---

### TASK-091: Nightly PostgreSQL backup and tested restore
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
ADR-002/ADR-008. The off-VPS destination is chosen within budget in Build.

**Canonical test file**: `tests/ops/backup-restore.test.ts`

**Acceptance Criteria**:
AC-1: Given the nightly backup job, when it has run, then a compressed pg_dump from the last 24 h exists off the VPS [technical]
AC-2: Given the latest backup, when it is restored into an empty PostgreSQL, then the application starts against it and GET /api/health returns UP [technical]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)
- [ ] Write failing canonical test for AC-2
- [ ] Implement AC-2 (red→green)

**Blocked by**: TASK-051
**Blocks**: None

---

### TASK-092: Load test: p95 ≤ 200 ms at 50 users
**Priority**: P0 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
k6 (tech-stack.md). Success metric 5 (a target, not a guarantee).

**Canonical test file**: `tests/ops/load/k6-50-users.js`

**Acceptance Criteria**:
AC-1: Given 50 concurrent simulated users in a load test, when typical game actions are submitted, then the p95 server processing time is at most 200 ms. [US-021 AC-2]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-061, TASK-068, TASK-052
**Blocks**: None

---

### TASK-093: Tracing to Tempo (observability phase 2)
**Priority**: P1 | **Estimate**: 3h | **Status**: Ready | **Owner**: dev

**Description**:
Incremental, as agreed.

**Canonical test file**: `tests/ops/observability-traces.test.ts`

**Acceptance Criteria**:
AC-1: Given the tracing increment is delivered, when a request is handled, then its trace is viewable in Tempo. [US-028 AC-3]

**TDD Tasks**:
- [ ] Write failing canonical test for AC-1
- [ ] Implement AC-1 (red→green)

**Blocked by**: TASK-090
**Blocks**: None

---

## Task Summary

| Sprint | Tasks | Estimate | of which owner |
|---|---|---|---|
| Sprint 1 | TASK-001…TASK-007 (7) | 17.5h | 7.5h |
| Sprint 2 | TASK-008…TASK-013 (6) | 16.5h | 5.5h |
| Sprint 3 | TASK-014…TASK-020 (7) | 17h | 0h |
| Sprint 4 | TASK-021…TASK-026 (6) | 16h | 0h |
| Sprint 5 | TASK-027…TASK-032 (6) | 17h | 0h |
| Sprint 6 | TASK-033…TASK-038 (6) | 17.5h | 0h |
| Sprint 7 | TASK-039…TASK-044 (6) | 16h | 0h |
| Sprint 8 | TASK-045…TASK-051 (7) | 17h | 2h |
| Sprint 9 | TASK-052…TASK-057 (6) | 16h | 0h |
| Sprint 10 | TASK-058…TASK-064 (7) | 17h | 0h |
| Sprint 11 | TASK-065…TASK-070 (6) | 16h | 0h |
| Sprint 12 | TASK-071…TASK-076 (6) | 16h | 0h |
| Sprint 13 | TASK-077…TASK-082 (6) | 17h | 0h |
| Sprint 14 | TASK-083…TASK-088 (6) | 16h | 0h |
| Sprint 15 | TASK-089…TASK-093 (5) | 15h | 0h |
| **Total** | **93 tasks, 223 ACs** | **247.5h** | **15h** |

## Story Traceability

| Story | Priority | Tasks |
|---|---|---|
| US-001 | P0 | TASK-012, TASK-032 |
| US-002 | P0 | TASK-043, TASK-044 |
| US-003 | P0 | TASK-045, TASK-046 |
| US-004 | P0 | TASK-054, TASK-072 |
| US-005 | P0 | TASK-074 |
| US-006 | P0 | TASK-053 |
| US-007 | P0 | TASK-055, TASK-062, TASK-077 |
| US-008 | P0 | TASK-058 |
| US-009 | P1 | TASK-059, TASK-062 |
| US-010 | P1 | TASK-076 |
| US-011 | P0 | TASK-018, TASK-019 |
| US-012 | P0 | TASK-022, TASK-034 |
| US-013 | P0 | TASK-023, TASK-024 |
| US-014 | P0 | TASK-025, TASK-027 |
| US-015 | P0 | TASK-028 |
| US-016 | P0 | TASK-029, TASK-030 |
| US-017 | P0 | TASK-037 |
| US-018 | P0 | TASK-039 |
| US-019 | P0 | TASK-031, TASK-064 |
| US-020 | P0 | TASK-086, TASK-087, TASK-088 |
| US-021 | P0 | TASK-061, TASK-092 |
| US-022 | P0 | TASK-068 |
| US-023 | P0 | TASK-065, TASK-067 |
| US-024 | P0 | TASK-069 |
| US-025 | P0 | TASK-071 |
| US-026 | P1 | TASK-070 |
| US-027 | P0 | TASK-015, TASK-051, TASK-052 |
| US-028 | P0 | TASK-089, TASK-090, TASK-093 |
