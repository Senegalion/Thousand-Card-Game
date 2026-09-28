# ADR-004: Live Match State and Durability

## Status
Accepted (owner decision, 2026-09-28)

## Date
2026-09-28

## Context
An in-progress match (hands, musik, auction, tricks, scores, timers) changes several times a minute and must be:
- strongly consistent and processed one action at a time;
- recoverable after a player reconnects (US-023);
- able to survive application restarts, because CI deploys every merge to `main` (US-027 AC-3);
- ready to move to shared or persistent state later (Q9 in `discovery.md`).

There is one application instance and fewer than 50 users.

## Decision
The **in-memory authoritative state** is backed by a **write-through PostgreSQL snapshot after every accepted action**.

**Components:**
- **`MatchStateStore` interface** (in `match`), with the operations `load(matchId)`, `save(matchId, expectedVersion, state)` and `delete(matchId)`. The MVP implementation is `PostgresMatchStateStore` with a `ConcurrentHashMap` cache in front.
- **Table `match_state_snapshot`**:
  - `match_id` (PK, FK → `match` ON DELETE CASCADE)
  - `version` (bigint)
  - `schema_version` (int)
  - `state` (JSONB)
  - `updated_at`

  The JSON mapping lives in `match/infrastructure`, so the `game` domain stays free of Jackson.

**Action processing:**
- Each match has a **serial executor**, which guarantees single-threaded processing per match.
- The pipeline for one action runs in this order:
  1. Check membership and version. The action carries `expectedVersion` and a `clientActionId` for idempotency against duplicates or stale clients.
  2. `GameEngine.apply`.
  3. Inside one transaction, `UPDATE match_state_snapshot … WHERE version = expected`, plus any `match`/`match_participant` updates.
  4. Swap the in-memory reference.
  5. Publish per-seat `PlayerView`s (ADR-005).
- If persisting fails, the action is rejected and the in-memory state is untouched.

**Timers and restart:**
- Deadlines for turn timers and disconnect grace periods are stored in the snapshot as absolute instants.
- On startup, every `IN_PROGRESS` snapshot is loaded.
  - Turn timers resume from the remaining time.
  - Disconnect grace periods **restart at the moment of recovery**, since every client dropped at the same time because of the server.
- A deploy then looks like a short network blip. STOMP clients reconnect automatically and receive a fresh view.

**Cleanup:**
- When a match ends (`COMPLETED`, `FORFEIT` or `ABANDONED`), the final results go to `match_participant` and the snapshot row is deleted.
- This follows data minimization: history keeps only results. Full replay is out of scope.

## Consequences

### Positive
- Matches survive deploys and crashes. Reconnection after a restart reuses the same code path as a network drop.
- No Redis is needed, which saves RAM and cost.
- The snapshot and the `MatchStateStore` seam are exactly what a later multi-instance setup needs.

### Negative
- One transactional write per action. That is a few per second at peak, which is negligible here.
- The snapshot JSON schema must be versioned. `schema_version` and a migration or upcasting step are needed when `MatchState` changes, or in-progress matches are drained before deploys that change the state shape.

### Risks
- **Risk:** a deploy that changes `MatchState` breaks restoring old snapshots.
  - **Mitigation:**
    - `schema_version` plus upcasters.
    - CI tests that restore snapshots from fixtures of the previous version.
    - As a fallback, a deploy switch that waits until there are no active matches.
- **Risk:** the in-memory cache and the database diverge.
  - **Mitigation:** the database write succeeds before the in-memory swap, and the optimistic version check prevents lost updates.

## Alternatives Considered
1. **In-memory only:** Rejected because every deploy or restart would end all running matches, which contradicts "main always deployable" in practice. It also has no scaling path.
2. **Redis as the state store (with AOF persistence):** Rejected because at a single instance it adds a service and RAM without a benefit PostgreSQL doesn't already provide. It remains the likely choice if horizontal scaling is ever needed.
3. **Full event sourcing (append every action, rebuild by replay):** Rejected for the MVP. It would enable replays, which are a non-goal, but it adds event versioning and projection complexity. The snapshot approach can later be extended with an action log if replay becomes a goal.
