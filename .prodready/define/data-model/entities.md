# Data Model

Scope: MVP entities only. Persistence is PostgreSQL with jOOQ and Flyway (no ORM). The DDL is in `schema.sql`.

Two kinds of state:
- **Persistent relational state** (below): accounts, reset tokens, private tables, seats, match records and history.
- **Live match state** (`MatchState`): the in-progress game aggregate owned by the pure `game` domain. It covers hands, musik, bids, tricks, trump, round and game scores, barrel status, bomb counts, timers and connection state. In the MVP it lives in the single application instance behind a repository abstraction (assumption A-12), so it can later move to Redis or PostgreSQL. It is **not** part of the relational schema below.

## Entities

### AppUser (module: identity/user)
- id: UUID (PK)
- email: String (unique, case-insensitive; personal data)
- username: String (unique, case-insensitive; public display name; personal data)
- passwordHash: String (Argon2id encoded hash)
- credentialsChangedAt: DateTime (sessions issued before this are invalid; supports US-003 AC-5)
- createdAt: DateTime
- updatedAt: DateTime

Account deletion hard-deletes this row. There is no soft delete, so no personal data is retained.

### PasswordResetToken (module: identity)
- id: UUID (PK)
- userId: UUID (FK → AppUser, cascade on delete)
- tokenHash: String (hash of the emailed token; the raw token is never stored)
- expiresAt: DateTime (createdAt + 30 min, A-03)
- usedAt: DateTime? (single use)
- createdAt: DateTime

### GameTable (module: lobby)
A private table that players join by code or link.
- id: UUID (PK)
- joinCode: String (unique, random, unguessable; A-07)
- playerCount: Integer (2, 3 or 4)
- hostUserId: UUID? (FK → AppUser, set null on delete)
- status: Enum (OPEN, IN_MATCH, CLOSED)
- createdAt: DateTime
- closedAt: DateTime?

### TableSeat (module: lobby)
The current occupancy of a table that is not closed. Seat rows are removed when the table closes, which enforces "one active table per player" (A-10).
- tableId: UUID (FK → GameTable, cascade on delete)
- seatIndex: Integer (0 .. playerCount−1, in the order players go to the left)
- userId: UUID (FK → AppUser, unique across all seats)
- joinedAt: DateTime

### Match (module: match)
The record of one match played at a table. It is persistent history, not live state.
- id: UUID (PK)
- tableId: UUID? (FK → GameTable, set null on delete)
- playerCount: Integer (2, 3 or 4)
- rulesetVersion: String (identifies the `ruleset.md` / variant configuration version used)
- status: Enum (IN_PROGRESS, COMPLETED, FORFEIT, ABANDONED; A-08)
- startedAt: DateTime
- endedAt: DateTime?

### MatchParticipant (module: match)
- id: UUID (PK)
- matchId: UUID (FK → Match, cascade on delete)
- seatIndex: Integer
- userId: UUID? (FK → AppUser, **set null on delete**. Null means "Deleted player". No username or email snapshot is stored, so the deleted user can't be re-identified from history; US-004.)
- finalScore: Integer? (the score at match end or at forfeit time; may be negative, pending R-085)
- outcome: Enum? (WIN, LOSS, DRAW, FORFEITED, WIN_BY_FORFEIT, ABANDONED)

## Relationships
- AppUser 1:N PasswordResetToken
- AppUser 1:N GameTable (as host; nullable)
- GameTable 1:N TableSeat
- AppUser 1:0..1 TableSeat (at most one active seat)
- GameTable 1:N Match
- Match 1:N MatchParticipant (2–4 rows)
- AppUser 1:N MatchParticipant (nullable after account deletion)

## Indexes
- AppUser.email: unique, case-insensitive (`lower(email)`)
- AppUser.username: unique, case-insensitive (`lower(username)`)
- PasswordResetToken.tokenHash: unique
- PasswordResetToken.userId
- GameTable.joinCode: unique
- TableSeat (tableId, seatIndex): PK
- TableSeat.userId: unique
- Match.status: for statistics-eligible queries (US-026)
- MatchParticipant (matchId, seatIndex): unique
- MatchParticipant.userId: for per-player history (US-025)

## Success-metric traceability
- Real use (metric 6): `Match` rows with `status = COMPLETED` grouped by `playerCount`.
- Statistics eligibility (US-026): `Match.status = COMPLETED`.
- GDPR deletion: the AppUser row is deleted, tokens cascade, `MatchParticipant.userId` is set to null, and `GameTable.hostUserId` is set to null. No personal field remains anywhere.
- Performance, reliability and operations metrics are measured through tests and telemetry, not stored entities.
