# Discovery Record

- Status: completed
- Stop option offered: yes
- Initial request: `/prodready-define` invoked for Thousand Online, an existing (mostly empty) repository whose CLAUDE.md already describes the product, architecture, and technology decisions.

## Known Facts
- UI language: Polish only for MVP (Q13).
- Turn timers: 60 s card play, 60 s bidding/musik decisions (Q13).
- Account deletion: anonymize participant in shared match history ("Deleted player"), keep non-personal game data, no re-identification possible (Q11).
- Accounts: no email verification required; password reset by email in MVP; external SMTP provider (Q10).
- GDPR: privacy policy, self-service account deletion, data minimization (email, username, password hash) (Q10).
- Security: Argon2id, auth rate limiting, HTTPS, server-authoritative, input validation, no secrets in logs/Git, secure session/token handling (Q10).
- Deployment: single public VPS (Hetzner/OVH), Docker Compose, HTTPS, CI/CD; budget ceiling €10/month (Q9).
- Scale: <50 concurrent users, a few tables, 1 app instance; match state designed to allow later move to shared/persistent state (Q9).
- Team: solo developer, ~10–15 h/week; no hard deadline (Q8).
- Scope priority: engine (2/3/4p) + auth + private tables + realtime > full observability; observability incremental (Q8).
- Turn timeout: automatic minimal legal move (bid: pass if allowed; play: lowest legal card); no match loss for a single timeout (Q7).
- Disconnect: 2-minute grace; then forfeit; remaining players win by forfeit; scores stored (Q7).
- History statuses: COMPLETED vs FORFEIT/ABANDONED; non-completed excluded from future ranking/stats (Q7).
- MVP identity: email+password registration/login only (Q6).
- MVP lobby: private tables via invite link or code (Q6).
- MVP match resilience: reconnect to in-progress game; turn timers (Q6).
- MVP history: persisted completed-match results and final scores per player (Q6).
- Deferred: social login, guest play, public table list, matchmaking, rating/leaderboard, bot takeover, chat, spectators, replay (Q6).
- Ruleset: derived rules R-035, R-063, R-073, R-083, R-102, R-112 approved by owner (Q5). The ruleset domain is closed except for 15 Kurnik-verification items.
- Authoritative ruleset = `.prodready/define/ruleset.md` (Q2), based on Kurnik rules text as the canonical source (Q3). Target is faithful standard Kurnik-like Tysiąc, no house rules (Q3).
- MVP includes 2-, 3-, and 4-player variants (Q1).
- Per-player-count rule differences must be explicitly documented and enforced by the backend (Q1).
- No invented house rules; unclear rules must be surfaced, not assumed (Q1).
- Rule variants must be pluggable so new variants don't require an engine rewrite (Q1).
- Product: Thousand Online — online multiplayer implementation of the Polish card game Tysiąc.
- Player count: 2–4 players per game.
- Purpose: production-oriented portfolio project showcasing software engineering, AI-assisted development, testing, DevOps, security, and observability.
- Architecture: modular monolith, package-by-feature; expected modules: identity, user, lobby, match, game, ranking. No microservices without a documented decision.
- Game domain is the authoritative source of Tysiąc rules and is independent of Spring/HTTP/WebSocket/PostgreSQL/Redis.
- Backend is authoritative for game state, rules, authentication, and authorization; the frontend is never trusted.
- Backend stack: Java 25, Spring Boot 4.x, Maven, REST, WebSocket with STOMP, Spring Security.
- Persistence: PostgreSQL, jOOQ, Flyway; no JPA/Hibernate.
- Infrastructure: Redis only for justified ephemeral/realtime use cases; Docker, Docker Compose.
- Frontend: React, TypeScript, Vite, Tailwind CSS, React Router, Zustand.
- Testing: JUnit 5, AssertJ, Mockito where appropriate, Testcontainers, Playwright.
- CI/CD & observability: GitHub Actions, OpenTelemetry, Prometheus, Grafana, Loki, Tempo.
- Git workflow: trunk-based, short-lived feature branches, stable `main`, small focused commits.

## Observed Conflicts in Existing Repository
- `README.md` states Java 21 / Spring Boot 3; `CLAUDE.md` states Java 25 / Spring Boot 4.x. CLAUDE.md treated as authoritative; README to be updated later.
- Backend skeleton packages are `room`, `player`, `game`, `shared`; CLAUDE.md expects `identity`, `user`, `lobby`, `match`, `game`, `ranking`. To be resolved in Design.
- Stray empty `thousand-backend/` directory and empty `docker-compose.yml` exist.

## Questions and Answers
1. Q: Which rule set is authoritative, and must the first version support 2, 3 and 4 players at launch?
   A: MVP must support 2-, 3-, and 4-player games; all three are core scope, not future features. Rules may differ per player count and every difference must be explicitly documented in the domain specification; the backend enforces the rules for the selected player count. Use a clearly defined ruleset as the authoritative source — do not invent house rules or silently assume rules where unclear. Architecture must model player-count-specific rules cleanly so the domain stays testable and future rule variants can be added without rewriting the engine.
2. Q: What should the authoritative ruleset be — an existing source, or a drafted ruleset.md with every disputed rule flagged?
   A: (b) Draft ruleset.md and flag every disputed rule. → Drafted `.prodready/define/ruleset.md` with 20 decision points (D-01..D-20), each with options and a proposed default pending owner approval.
3. Q: Approve the 20 proposed defaults (D-01..D-20) or list overrides?
   A: Owner asked to see the full list first (shown), then rejected auto-approval: the target is a faithful standard/classic Tysiąc as played on Kurnik, not a new variant. Each decision must be classified as standard / non-standard / player-count-dependent / needing Kurnik-like definition; uncertain rules must be marked for confirmation, not defaulted. → Fetched Kurnik rules (https://www.kurnik.pl/tysiac/zasady.phtml, 2026-09-28) and rewrote `ruleset.md` with Kurnik as the authoritative source. Result: 8 of 20 old defaults were wrong (D-02, D-04, D-05, D-07, D-09, D-17, D-19) or unclear (D-12, D-13); 13 open CONFIRM items (C-01..C-13) remain where Kurnik text is silent/ambiguous.
4. Q: How does Kurnik actually behave for C-01..C-13? (answer or "check")
   A: "check" for all 13 — owner does not know; they must be verified in real Kurnik gameplay, not assumed. Owner also asked to see full text and evidence for derived rules R-035, R-063, R-073, R-083, R-085, R-102, R-103, R-112 before signing off (shown).
5. Q: Approve derived rules R-035, R-063, R-073, R-083, R-102, R-112? Keep R-085 and R-103 or move them to verification?
   A: Approved R-035, R-063, R-073, R-083, R-102, R-112. R-085 (negative scores) and R-103 (4p sit-out on barrel) moved to the Kurnik verification list with C-01..C-13.
6. Q: Which of identity / lobby / ranking / match resilience / extras are MVP?
   A: Identity: email+password registered accounts only; no social login; guest play later. Lobby: private tables joined by link or code; public table list and matchmaking later. Ranking: no rating, no leaderboard in MVP (later). Resilience: reconnect to in-progress game + turn timers in MVP; bot takeover later. Other: no chat, no spectators, no replay; basic game history (completed match results and scores) IS in MVP for account view and future ranking/statistics.
7. Q: What happens on turn-timer expiry, on disconnect, and how do abandoned/forfeited matches appear in history?
   A: (a) Timer expiry while connected → server makes the smallest legal/default move: pass in bidding if passing is allowed; lowest legal card in trick play. A single expiry never loses the match. (b) Disconnect → 2-minute grace period; reconnect within it continues normally; otherwise the disconnected player forfeits, remaining players win by forfeit, and current scores are stored; match must not continue indefinitely. (c) Abandoned/forfeited matches appear in basic history with explicit status (e.g. ABANDONED / FORFEIT), storing participants, status, timestamp, available scores/results; they must be excluded from future ranking/statistics as normal completed matches.
8. Q: Who is building this, what is the time budget/deadline, and what gives if scope conflicts?
   A: Solo developer, ~10–15 h/week alongside studies and full-time work. No hard deadline; goal is a serious portfolio project and learning production-oriented SE, DevOps, AI-assisted development, testing, security, observability. Core game scope (2/3/4-player engine) must not be reduced. Priority on conflict: complete and correct 2/3/4-player engine, authentication, private tables, reliable realtime gameplay > full observability stack. Observability stays in the architecture but components may be delivered incrementally. MVP stays production-oriented while avoiding complexity that does not support the core product.
9. Q: Deployment target, monthly budget, and load assumptions?
   A: (a) Single small VPS (Hetzner or OVH) with Docker Compose, public MVP deployment; app, PostgreSQL and other infra on the same VPS. Budget ceiling €10/month. Load: <50 concurrent users, a few simultaneous tables, one app instance; no horizontal scaling now, but avoid assumptions that make scaling impossible — match state must be designed so moving to shared/persistent state later is possible. VPS deployment (Docker, HTTPS, CI/CD, observability) is part of the portfolio/learning value.
10. Q: Email verification / password reset, GDPR minimum, and security requirements?
    A: (a) No email verification required to play; password reset by email IS in MVP; use external SMTP/email provider. (b) GDPR minimum accepted: privacy policy, self-service account deletion, store only email, username, password hash; no unnecessary personal data. (c) Security: Argon2id preferred; rate limiting on login and other auth-sensitive endpoints; HTTPS everywhere in production; backend authoritative for every game action; validate all client input; never log passwords/tokens/secrets; no secrets in Git; secure session/token handling appropriate to the chosen auth architecture.
11. Q: What happens to a user's matches when they delete their account?
    A: (a) Anonymize. Permanently remove personal/account data; preserve match history for other players; replace the deleted player's identity with "Deleted player" (non-identifying placeholder); keep score, status, date and other non-personal game data; the deleted account must not be recoverable or identifiable via remaining history. Never delete shared matches because one participant deleted their account.
12. Q: Which of six proposed success criteria (correctness, reliability, quality, operations, performance, real use) are accepted, and with what thresholds?
    A: All six accepted with clarifications: (1) every approved R-xxx covered by domain tests; Playwright E2E for complete 2/3/4-player matches. (2) Reconnect within grace period continues with no state loss, covered by automated test. (3) CI on every PR: unit/integration tests, dependency/security scanning, coverage reporting; ≥90% coverage target for the domain/game module only; main always deployable. (4) VPS deployment deployable from CI; metrics and logs in Grafana; tracing incremental. (5) p95 ≤ 200 ms for typical application/API operations at ≤50 concurrent users — a target, not an absolute guarantee. (6) Validated by complete real matches with friends in production for 2, 3 and 4 players. These criteria define MVP completion; no optimization beyond MVP load.
13. Q (Define gate audit): Confirm assumptions A-01..A-15? UI language? Turn timer duration?
    A: All assumptions A-01..A-15 confirmed; exact implementation details may be refined in Design without changing product intent. UI language: Polish only for MVP. Turn timer: 60 s for normal turn/card-play decisions and 60 s for bidding and musik-related decisions. Timeout behavior for special decisions (automatic-100 opener, 2p musik selection and discard, 3/4p card passing, final contract, bomb, marriage, four-9s redeal) is a Design decision that must follow the ruleset without inventing game rules.
14. Q (post-gate ruleset review): Approve R-064 (a card from another non-trump suit never wins a trick) and add C-12 (iii)?
    A: R-064 approved. C-12 (iii) added: whether the obligation to beat the led suit still applies once the trick has been trumped. C-12 stays CONFIRM until verified against Kurnik.

## Assumptions
All assumptions below were confirmed by the owner at the Define gate (2026-09-28); implementation details may be refined in Design without changing product intent.
Inferred by the agent to complete the Define artifacts; each is conservative, non-blocking and open to owner correction.
- A-01: Minimum password length 8 characters, maximum at least 64, no composition rules (NIST SP 800-63B guidance); exact policy confirmed in Design.
- A-02: Login rate limiting threshold (e.g. 5 failed attempts per account/IP per 15 minutes) is configurable; exact values set in Design.
- A-03: Password reset tokens are single-use and expire after 30 minutes; a successful reset invalidates the user's existing sessions.
- A-04: Password reset request returns the same response whether or not the email exists (no account enumeration).
- A-05: Usernames are unique and public within the product; emails are never shown to other players.
- A-06: The table creator (host) starts the match once all seats are filled; a match cannot start with empty seats.
- A-07: Private table codes are random, unguessable, and expire when the table closes; a table with no players or an unstarted table older than a configurable period is closed.
- A-08: Match status set: COMPLETED (someone reached 1000), FORFEIT (a player forfeited after the disconnect grace period), ABANDONED (all remaining participants disconnected beyond the grace period, so nobody can be credited with a win). Only COMPLETED counts for future ranking/statistics.
- A-09: Turn timer duration is configurable per decision type. Owner set MVP values: 60 s for card play and 60 s for bidding/musik decisions.
- A-10: A player may be seated at only one active table/match at a time.
- A-11: GDPR data access/portability requests (Art. 15/20) are handled manually on request via a contact address in the privacy policy; no self-service export in MVP.
- A-12: Live in-progress match state is held by the single application instance behind a repository abstraction so it can later move to shared/persistent storage (Redis/PostgreSQL); the concrete choice is a Design decision.
- A-14: Account deletion requires re-entering the current password and is rejected while the user is seated at an active table or match (prevents breaking live matches; aligns with `table_seat.user_id ON DELETE RESTRICT`).
- A-15: When the host leaves an unstarted table the table closes (no host migration in MVP).
- A-13: UI language — RESOLVED by owner: Polish only for the MVP.

## Open Questions
- C-01..C-13: pending verification against actual Kurnik gameplay (owner: "check"). Blocking for implementation of the affected rules; handled as explicit, unset variant switches.
- R-085, R-103: pending Kurnik verification (same treatment as C-items).
- Timeout default moves not yet specified for non-bid/non-play decisions: mandatory-100 opener (cannot pass), 2p musik choice + 2-card discard, 3p/4p card passing, final contract declaration, bomb decision, marriage declaration on lead, four-9s redeal request. Also definition of "lowest legal card" (by strength; tie-break across suits). Owner decision: a Design-phase decision that must follow ruleset.md and invent no game rules.
- Risk: full observability stack (Prometheus, Grafana, Loki, Tempo) + PostgreSQL + Redis + app on one €10 VPS is memory-tight (~4–8 GB RAM tier); consistent with incremental observability (Q8). To be sized in Design.
