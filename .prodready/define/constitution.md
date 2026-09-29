# Constitution

## Non-Negotiables
- **Faithful rules**: the game implements standard Tysiąc as defined in `ruleset.md`, with Kurnik as the authoritative source. There are no invented house rules. A rule marked `CONFIRM` is not implemented until it has been verified against real Kurnik gameplay.
- **2, 3 and 4 players in the MVP**: all three player counts are core scope. Per-player-count differences are documented in `ruleset.md` and enforced by the backend.
- **Pluggable variants**: player-count-specific rules are modelled as variant configurations over a shared engine core, so new variants don't require an engine rewrite.
- **Server authority**: the backend is authoritative for game state, rules, authentication and authorization. Every game action is validated on the server, and the client is never trusted.
- **Hidden information**: a player never receives another player's hand, an unrevealed musik, or face-down discards.
- **Domain isolation**: the game domain is independent of Spring, HTTP, WebSocket, PostgreSQL and Redis, and is testable in isolation.
- **Test-first**: every acceptance criterion has one canonical `AC-N:` test that fails (RED) before implementation. Every approved `R-xxx` has domain tests.
- **Security baseline**:
  - Argon2id password hashing
  - rate limiting on authentication endpoints
  - HTTPS everywhere in production
  - input validation
  - no secrets in logs or Git
  - secure session/token handling
- **Privacy (GDPR minimum)**:
  - collect only email, username and password hash
  - privacy policy
  - self-service account deletion that anonymizes shared match history irreversibly
- **Stable main**: trunk-based development and small focused commits, with `main` always deployable.

## Explicit Non-Goals
- No house rules or custom variants in the MVP. No "improvements" to the Kurnik rules.
- No rating (ELO/Glicko), leaderboards, or statistics dashboards in the MVP.
- No public table list or automatic matchmaking in the MVP.
- No guest play and no social login in the MVP.
- No bots, AI opponents, or bot takeover for disconnected players.
- No chat, spectators, or full replays.
- No partnerships or teams in the 4-player game.
- No stakes, betting, payments, or monetization.
- No in-app administrator panel. Operations are done by the operator through infrastructure tooling.
- No microservices, and no horizontal scaling in the MVP.
- No native mobile apps. The product is a responsive web app only.

## Technical Constraints
- Modular monolith, package-by-feature: identity, user, lobby, match, game, ranking (ranking module deferred).
- Backend: Java 25, Spring Boot 4.x, Maven, REST, WebSocket/STOMP, Spring Security.
- Persistence: PostgreSQL, jOOQ and Flyway. No JPA/Hibernate.
- Redis only for justified ephemeral or realtime use cases.
- Frontend: React, TypeScript, Vite, Tailwind CSS, React Router, Zustand.
- Testing: JUnit 5, AssertJ, Mockito where appropriate, Testcontainers, Playwright.
- CI/CD and observability: GitHub Actions, OpenTelemetry, Prometheus, Grafana, Loki, Tempo. These are delivered incrementally.
- Match state lives behind an abstraction so it can move to shared or persistent storage later.

## Timeline & Resources
- Timeline: no hard deadline. The MVP is complete when the success metrics in `vision.md` hold.
- Team: one solo developer, about 10–15 hours a week, working alongside studies and a full-time job, with AI-assisted development.
- Scope priority when there is a conflict:
  1. correct 2/3/4-player engine
  2. authentication
  3. private tables
  4. reliable realtime gameplay
  5. the full observability stack, which may be delivered incrementally

  The game engine scope is never cut for observability.
- Blockers: 15 ruleset items (C-01..C-13, R-085, R-103) must be verified against Kurnik before the rules they affect are implemented.
