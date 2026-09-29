# ADR-002: Database and Data Access

## Status
Accepted

## Date
2026-09-28

## Context
The system needs durable storage for:
- accounts
- server-side sessions
- password reset tokens
- private tables and seats
- match history with anonymization on deletion
- snapshots of live match state (ADR-004)

The data is relational, with strong integrity needs: unique seats, FK set-null anonymization, and status checks. The constitution forbids JPA/Hibernate and mandates PostgreSQL, jOOQ and Flyway. The budget calls for co-locating the database on the VPS.

## Decision
We will use **PostgreSQL 17** as the single source of truth, **Flyway** for all schema changes, and **jOOQ** for data access, with classes generated from the migrated schema. The reasons:
- Constraints do the heavy lifting on invariants:
  - `UNIQUE(table_seat.user_id)` enforces one active seat per player (A-10).
  - `ON DELETE SET NULL` on `match_participant.user_id` handles GDPR anonymization.
  - `CHECK` constraints guard status and timestamp consistency.
- `JSONB` stores the versioned match-state snapshot without a separate store.
- jOOQ keeps SQL explicit and type-checked, and has no ORM session or lazy-loading surprises. That suits a small, well-understood schema.
- Spring Session JDBC reuses the same database, so no extra service is needed.

Schema baseline: `.prodready/define/data-model/schema.sql`, plus the Design additions:
- `match_state_snapshot` (ADR-004)
- the Spring Session tables (ADR-003)

jOOQ codegen runs against a Testcontainers PostgreSQL instance migrated by Flyway, so the generated code always matches the migrations.

## Consequences

### Positive
- Referential integrity, transactions, and one backup target (`pg_dump`).
- Explicit SQL is easy to reason about and to review in portfolio code.
- A good fit for the testing strategy: real PostgreSQL through Testcontainers.

### Negative
- More hand-written mapping code than with an ORM.
- Codegen adds a build step that depends on Docker (Testcontainers) in CI and locally.

### Risks
- **Risk:** the database shares the VPS with everything else, so a disk failure loses data.
  - **Mitigation:** a nightly `pg_dump` to off-VPS storage (the free-tier or cheap object storage is chosen in Build), with a documented restore procedure.
- **Risk:** snapshot write amplification.
  - **Mitigation:** negligible at the stated scale (a few writes per second). This is monitored with a metric.

## Alternatives Considered
1. **JPA/Hibernate (Spring Data JPA)**: Rejected because it is explicitly excluded by the constitution. Its session and lazy-loading model also adds complexity the domain doesn't need.
2. **Spring Data JDBC**: simpler than JPA. Rejected because the owner chose jOOQ, and jOOQ gives stronger compile-time safety for handwritten queries such as history and statistics.
3. **MongoDB or another document store**: would suit match snapshots. Rejected because accounts, seats and history need relational constraints and transactions, and a second database would breach the budget and simplicity goals.
