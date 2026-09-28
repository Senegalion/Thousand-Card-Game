# Implementation Plan

## Overview

- **Project**: Thousand Online. Server-authoritative Tysiąc (Kurnik rules) for 2, 3 and 4 players (`define/vision.md`).
- **Pattern**: modular monolith, package-by-feature, with a pure `game` domain (`design/architecture/pattern.md`).
- **Stack**:
  - Backend: Java 25, Spring Boot 4.1.x, PostgreSQL 17 + jOOQ + Flyway, Spring Session JDBC, STOMP over WebSocket.
  - Frontend: React 19 + TypeScript + Vite + Tailwind + Zustand.
  - Operations: Docker Compose on one VPS, Caddy, GitHub Actions; Prometheus, Loki and Grafana, with Tempo in phase 2.
- **Scope source**: only the accepted Define and Design artifacts. This plan makes no new product or architectural decisions.

**Numbers**
- 93 tasks and 223 acceptance criteria, totalling 247.5 h (15 h of that is owner work).
- 15 two-week sprints.
- Planning load per sprint is at most 17.5 h, which is 70% of about 25 h of capacity at 10–15 h a week. That is roughly 30 weeks of calendar time. There is no hard deadline (`constitution.md`).

Details: `backlog.md` (tasks and ACs), `dependencies.mmd` (graph), `test-plan.md` (AC → canonical test traceability).

## Plan-level decisions recorded (owner answers, 2026-09-28)

| # | Question | Answer | Effect on the plan |
|---|---|---|---|
| P-Q1 | When to verify the 15 Kurnik items | **Sprints 1–2, in parallel** with the engine core built from approved rules | TASK-005..TASK-009 are scheduled in Sprints 1–2 |
| P-Q2 | Domain for HTTPS | **Buy a domain**, about €10–15 a year, within the €10/month ceiling together with the VPS | External dependency in TASK-049 |

## Critical path

Two chains determine when the MVP is complete:

1. **Engine chain (about 63 h of tasks):**
   - TASK-001 → 002 → 016 → 017 → 018 → 019 → 021 → 022 → 023 → 025 → 027 → 028 → 029 → 030 → 031 → 033 → 041 → 042 → 068 → 086 → 087 → 088
   - That is: alignment, cards, variants, deal, view, engine, auction, musik, tricks, marriages, scoring, end of game, legal actions, timeout fallback, timers, and then the E2E tests for 2p, 3p and 4p.
2. **Kurnik chain.** All 15 pending items are **explicitly on the critical path**. No player count can complete a match until the items that apply to it are verified and implemented, because ADR-006 fail-fast refuses to start a variant with unset options.

| Verification task (owner) | Items | Unblocks | Player counts affected |
|---|---|---|---|
| TASK-005 | C-04, C-07 | TASK-034 | **all** |
| TASK-006 | C-12 (i)–(iii), C-13 | TASK-035 | **all** |
| TASK-007 | C-01, C-02, C-05, C-06 | TASK-026 (C-05, 2p), TASK-036 (4p) | 2p (C-05), 4p |
| TASK-008 | C-08, C-09, C-10, C-11 | TASK-037, TASK-038 | **all** (C-10: 4p) |
| TASK-009 | C-03, R-085, R-103 | TASK-039, TASK-040 | **all** (R-103: 4p) |

- **Kurnik-blocked tasks** (orange in the graph): 13. These are the option implementations, the UI controls that depend on them, and all three E2E matches.
- **Rule for Kurnik-blocked ACs:** an AC whose text says "exact expected behavior = the VERIFIED entry for C-xx" is rewritten with the concrete observed behavior as soon as verification completes. That spec update happens **before** its RED test is written, and it never assumes a behavior.

## Phases

### Phase 1: Foundation, Kurnik verification and walking skeleton (Sprints 1–3)
**Goal:** the skeleton is aligned with the ADRs, the unknowns are removed early, and one thin slice works end to end.
- Stack and module alignment (Java 25, no Lombok, module packages), ArchUnit rules, Flyway and jOOQ baseline.
- **All five Kurnik verification tasks**, plus the verification harness (TASK-004) that checks the ruleset in CI.
- Error model, security foundation (sessions, CSRF), registration API, SPA foundation.
- **Walking skeleton:**
  - End of Sprint 2: register → API → PostgreSQL → session (integration test).
  - Sprint 3: the same slice through the Polish register page (TASK-014).
  - Sprint 1 intentionally has **no** end-to-end slice. Its owner capacity (7.5 of 17.5 h) goes to Kurnik verification, the biggest unknown (P-Q1), and its developer work builds the base every slice needs (alignment, architecture rules, migrations, verification harness). The first moment the pieces are shown to fit together is the TASK-012 integration test at the end of Sprint 2.
- CI quality gates (TASK-015), cards, variant fail-fast, deal, PlayerView, health endpoint.

### Phase 2: Game engine (Sprints 4–7)
**Goal:** a complete, pure, fully tested rules engine for 2, 3 and 4 players, with every verified option implemented.
- Engine core, auction, musik (2p and 3p), tricks, marriages, scoring, barrel, end of game, legalActions.
- Kurnik option implementations (TASK-026, TASK-034..TASK-040).
- ADR-009 timeout fallback (a technical fallback, not rules).
- Identity continues in parallel: login, logout, rate limiting.

### Phase 3: Identity completion and early production (Sprints 8–9)
**Goal:** accounts are complete, and `main` is deployed to the real VPS early, so production problems surface early.
- Password reset with mail retries, retention.
- External accounts (domain, VPS, SMTP), VPS hardening, deploy with automatic rollback, HTTPS.
- Private tables (create, join), MatchStateStore snapshots and the action pipeline.

### Phase 4: Matches and realtime (Sprints 10–12)
**Goal:** reliable realtime play from start to finish on the server.
- Start and leave, STOMP security and view push, seat updates, REST views, result recording.
- Presence, reconnect, recovery after restart, turn timers, forfeit and abandonment.
- History API, statistics-eligible query, anonymized deletion.

### Phase 5: UI and end-to-end (Sprints 12–14)
**Goal:** a playable Polish UI, and complete 2p, 3p and 4p matches passing in Playwright.
- Auth, privacy, account and lobby pages; STOMP client; the game table (hand, bidding, musik, status, optional actions, 4p layout); history page.
- E2E complete matches: TASK-086, 087 and 088 (success metric 1).

### Phase 6: Observability and performance (Sprint 15)
**Goal:** success metrics 4 and 5.
- Metrics and logs in Grafana, backups with a tested restore, a k6 load test at 50 users, and Tempo tracing (P1, incremental).

## Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Kurnik verification can't settle an item (behavior unobservable or inconsistent) | **High**: blocks every player count affected | Verify in Sprints 1–2 so there is maximum slack. Record partial evidence. An item that stays unresolvable goes back to the owner as an explicit decision; it is never defaulted (ADR-006). |
| **The single task most likely to take 3× longer:** the engine core and state machine (TASK-021), or the ADR-004 action pipeline (TASK-056/057) | High: both sit on the critical chain | Built test-first against a seeded deck. The pipeline is covered by integration tests before any UI. The engine is pure Java, so it can't be blocked by infrastructure. |
| The estimate of 247.5 h slips for a solo developer at 10–15 h a week | Medium: no deadline, but motivation | The priority order from the constitution holds: engine, then auth, then tables, then realtime, then observability. Observability is last and tracing is P1. |
| A late surprise when deploying to production | Medium | Deployment moved to Sprints 8–9. From then on, every merge to `main` deploys with a smoke check and rollback. |
| Memory limits on the €10 VPS | Medium | ADR-008 budgets memory per container. Observability comes last, and Tempo is optional. |
| Snapshot schema changes break restoring in-progress matches | Medium | `schema_version` plus upcaster tests (ADR-004) are part of TASK-066. |
| SMTP provider unavailable | Low | Retries with backoff plus a failure metric (TASK-047). Password reset responses are unaffected. |
| Spring Boot 4 or Java 25 library compatibility (jOOQ codegen, ArchUnit, jqwik) | Medium | Scaffold verifies the toolchain before Implement. Optional tools stay off by default (tech-stack.md). |

## Dependencies

External dependencies:
- [ ] **Kurnik access** for the owner to observe games (TASK-005..TASK-009). Needed from Sprint 1.
- [ ] **GitHub**: a public repository (confirmed), GHCR, Actions, and branch protection on `main` configured by the owner (TASK-015).
- [ ] **Domain name** purchased by the owner (P-Q2), with DNS pointing to the VPS (TASK-049). Needed by Sprint 8.
- [ ] **Hetzner VPS**, 8 GB tier; the price is checked against the €10/month total before ordering (TASK-049, ADR-008).
- [ ] **EU SMTP provider account** on the free tier, e.g. Brevo (TASK-049). Tests use a local SMTP test server, so development isn't blocked.
- [ ] **Off-VPS backup destination** within budget, chosen in Build (TASK-091).

Internal prerequisites:
- `prodready-gate plan`, then `prodready-scaffold` (Docker, Compose, CI, Makefile), then `prodready-gate scaffold`, before any backlog task starts.
