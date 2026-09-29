# ADR-007: No Redis in the MVP

## Status
Accepted

## Date
2026-09-28

## Context
`CLAUDE.md` and `backend/CLAUDE.md` allow Redis "only for justified ephemeral/realtime use cases", with active room state, presence, caching, pub/sub and future WebSocket scaling given as examples. Each candidate use case was evaluated against the MVP constraints: a single instance, fewer than 50 users, a €10 VPS and a solo developer.

## Decision
**Do not deploy Redis in the MVP.** Each candidate use case is covered as follows:

| Candidate use | MVP solution | Why Redis isn't justified yet |
|---|---|---|
| Active match state | In memory + PostgreSQL snapshot (ADR-004) | A single instance needs no shared cache, and durability comes from PostgreSQL |
| Presence / connection state | In memory in `match`, with deadlines in the snapshot | Only one node observes connections |
| Sessions | Spring Session JDBC (ADR-003) | PostgreSQL already provides shared, durable storage |
| Rate limiting | Bucket4j in memory | A single instance, and losing counters on restart is acceptable |
| Pub/sub, WebSocket fan-out | Simple STOMP broker (ADR-005) | A single instance |
| Caching | None needed | The load is trivial |

Redis is reintroduced by a new ADR when a **second application instance** becomes necessary. At that point it becomes the natural choice for rate-limit buckets, pub/sub fan-out, and possibly match ownership or locks.

## Consequences

### Positive
- One less service to run, secure, monitor and back up, and about 50–100 MB of RAM saved.
- A simpler local setup and simpler CI.

### Negative
- Rate-limit counters reset on restart. That is acceptable, since deploys are infrequent and the limits are short-window.
- `backend/CLAUDE.md` lists Redis in the stack. It must be updated to "Redis: deferred (ADR-007)" so that the documentation isn't misleading.

### Risks
- **Risk:** a scaling need arises suddenly.
  - **Mitigation:** every Redis candidate already sits behind an interface (`MatchStateStore`, the rate-limiter port, the broker configuration). The switch is contained work.

## Alternatives Considered
1. **Redis for match state and presence now:** Rejected because it duplicates what ADR-004 provides and costs RAM and operational effort without benefit at a single instance.
2. **Redis only for rate limiting:** Rejected because in-memory Bucket4j is sufficient for one instance, and a whole service for counters isn't justified.
