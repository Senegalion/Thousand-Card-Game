# Product Requirements Document (PRD)

## 1. Executive Summary

**Product**: Thousand Online
**Problem**: People who play Tysiąc on Kurnik have no rule-faithful, reliable place to play private matches with friends for every player count. The author also needs a production-grade portfolio system.
**Solution**: A web app with a server-authoritative Tysiąc engine that implements the Kurnik rules for 2, 3 and 4 players. It has private invite-only tables, realtime play with timers and reconnection, and is deployed publicly on a single VPS with CI/CD and observability.
**Target Users**: Players who know Kurnik-style Tysiąc, and the solo operator.
**Success Metric**: Complete 2-, 3- and 4-player matches played with friends in production, backed by full rule test coverage and green CI.

## 2. Goals & Non-Goals

### Goals
- Implement standard Tysiąc exactly as specified in `ruleset.md`, with Kurnik as the source, for 2, 3 and 4 players.
- Make the server the only authority over rules, state, hidden cards and permissions.
- Let friends meet at private tables through a code or link and play live, even with shaky connections.
- Keep a basic, privacy-safe match history.
- Operate the product like a real production system: HTTPS, CI/CD, metrics and logs, with tracing added incrementally.

### Non-Goals
- House rules or custom variants. Rules that Kurnik doesn't define are verified before implementation, never invented.
- Rating, leaderboards, public lobby, matchmaking, guest play, social login.
- Bots, chat, spectators, replays, 4-player partnerships, stakes or payments, and an admin panel.
- Microservices and horizontal scaling.

## 3. User Personas

### Persona 1: Player
- **Context**: Knows Tysiąc from Kurnik and wants to play with a specific group of friends from a desktop or mobile browser.
- **Pain Point**: Other implementations bend the rules, leak information, or leave a match stuck when someone drops.
- **Desired Outcome**: Creates a table, shares a link, plays a full match to 1000 with correct scoring, and later sees the result in their history.

### Persona 2: Operator (the author)
- **Context**: A solo developer working 10–15 h/week who deploys and runs the system.
- **Pain Point**: Manual deploys and invisible failures.
- **Desired Outcome**: Merges to `main` deploy automatically, and Grafana shows health and logs.

## 4. Functional Requirements

### FR-1: Game engine (US-011 to US-020)
- Deal, bidding, musik, final contract, bomb, trick obligations, marriages and trump, round scoring, the barrel, the four-nines redeal and the end at 1000 all follow `ruleset.md`.
- Player-count differences cover the deal, musik handling, the 4-player sitting-out player and the bomb penalty. They are modelled as variant configurations over one engine core.
- Illegal actions are rejected without changing state, and clients receive only information they are entitled to see.
- **Acceptance**: every approved `R-xxx` has domain tests, and Playwright plays complete matches for each player count.
- **Blocker**: 15 items (C-01..C-13, R-085, R-103) must be verified against real Kurnik gameplay before the rules they affect are implemented. The 4-player flow depends most heavily on them.

### FR-2: Identity (US-001 to US-005)
- Registration and login with email, username and password.
- Password reset by email through an external SMTP provider: single-use, expiring links, and no account enumeration.
- Self-service account deletion.
- A public privacy policy.
- **Acceptance**: Argon2id hashes, rate-limited auth endpoints, and sessions invalidated on logout and on password reset.

### FR-3: Private tables (US-006 to US-010)
- Create a 2-, 3- or 4-player table and receive a join code and invite link.
- Join, leave, and let the host start once the table is full.
- A player can be at only one active table.
- Empty states guide first-time users.

### FR-4: Realtime and resilience (US-021 to US-024)
- State updates are pushed over WebSocket/STOMP.
- Turn timers (60 s for card play, 60 s for bidding and musik decisions) trigger a minimal legal move: pass in bidding, the lowest legal card in play.
- A 2-minute reconnect grace period restores full state. After it expires the player forfeits, the others win by forfeit, and if everyone is gone the match is `ABANDONED`.

### FR-5: Match history (US-025, US-026)
- History shows participants, date, player count, status (`COMPLETED`, `FORFEIT` or `ABANDONED`) and final scores.
- Only `COMPLETED` matches are eligible for future statistics.
- Deleted users appear as "Deleted player" and are not re-identifiable.

### FR-6: Operations (US-027, US-028)
- CI on every PR runs tests, dependency and security scanning, and coverage, including the 90% gate on the game module.
- Deployment to the VPS is automatic from CI, with HTTPS.
- Prometheus metrics and Loki logs are shown in Grafana. Tempo tracing is added incrementally.

## 5. Non-Functional Requirements

- **Performance**: p95 of at most 200 ms for typical operations with at most 50 concurrent users. This is a target, not an absolute guarantee.
- **Security**:
  - the server is authoritative
  - input is validated
  - hidden information is protected
  - Argon2id and rate limiting are used
  - HTTPS is used everywhere
  - no secrets appear in logs or Git
- **Privacy**: GDPR minimum. Data is limited to email, username and password hash. There is a privacy policy, and deletion irreversibly anonymizes the user.
- **Availability**: a single VPS and a single instance, which is acceptable for the MVP. Live match state sits behind an abstraction so it can later move to shared storage.
- **Budget**: at most €10 per month. Running the full observability stack on one VPS is memory-tight, so it may be phased in.
- **Localization**: the UI is Polish only in the MVP.
- **Maintainability**: a modular monolith with a pure game domain, test-first development, and a trunk-based workflow with a deployable `main`.

## 6. Data Model Summary

### Entities
- **AppUser**: email, username, Argon2id hash, and a credentials-changed timestamp.
- **PasswordResetToken**: a hashed, expiring, single-use token.
- **GameTable**: join code, player count, host and status.
- **TableSeat**: a player's current seat; unique per user.
- **Match**: player count, ruleset version and status.
- **MatchParticipant**: seat, nullable user (null means "Deleted player"), final score and outcome.
- **MatchState** (not relational): the live game aggregate in the domain.

### Key Relationships
- A GameTable has seats and produces matches.
- A Match has 2–4 participants.
- Deleting an AppUser cascades to its tokens and nulls its participant links.

## 7. Scope & Timeline

- **MVP Features**: the engine for 2, 3 and 4 players, accounts with reset and deletion, private tables, realtime play with timers and reconnection, basic history, and CI/CD, HTTPS, metrics and logs on the VPS.
- **Future Features**: rating and leaderboards, public lobby and matchmaking, guest play and social login, bots, chat, spectators, replays, full tracing, scaling, rule variants.
- **Timeline**: no hard deadline. The MVP is done when the six success criteria in `vision.md` hold.
- **Team**: solo, about 10–15 h/week, AI-assisted. When scope conflicts, the priority order is engine, then auth, then tables, then realtime, then observability.

## 8. Open Questions & Risks

- **Rule verification (blocking for the affected rules)**: C-01..C-13, R-085 and R-103. The 4-player variant carries the most risk.
- **Timeout defaults** for decisions other than bidding and card play, and a precise definition of "lowest legal card". These go in the Design game-flow spec.
- **Risk**: memory limits on the €10 VPS for the full stack. **Risk**: solo capacity against a large MVP. It is mitigated by the fixed priority order.

## 9. References

- Rules (authoritative): `ruleset.md`
- Discovery trail and assumptions: `discovery.md`
- Detailed user stories: `requirements/user-stories.md`
- Data model details: `data-model/entities.md`, `data-model/schema.sql`
- Test scenarios: `test-scenarios/*.feature`
- Constraints: `constraints.md`
- Constitution: `constitution.md`
