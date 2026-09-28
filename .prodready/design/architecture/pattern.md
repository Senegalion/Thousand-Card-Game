# Architecture Pattern

## Selected Pattern: Modular Monolith (package-by-feature) with a pure game domain

## Rationale
Based on:
- **Deployment**: one public VPS running Docker Compose, with a budget of at most €10/month (`constraints.md`).
- **Scale**: fewer than 50 concurrent users, a few tables, one application instance. No horizontal scaling in the MVP.
- **Team**: one developer, 10–15 h/week, AI-assisted, with no deadline.
- **Non-negotiables**:
  - The domain is isolated from infrastructure.
  - The server is authoritative.
  - Variants are pluggable.
  - Microservices are explicitly excluded unless an ADR justifies them.

The modular monolith was selected for these reasons:
- It gives one deployable unit, one database and one process, which is the only realistic operational load for a solo developer on a €10 VPS.
- Explicit module boundaries (identity, user, lobby, match, game) keep the codebase navigable and testable, and leave a documented seam should a split ever be justified.
- A realtime card game needs strongly consistent, low-latency state transitions. In-process calls between `match` and `game` avoid distributed-consistency problems entirely.

## Structure

```
                 Browser (React SPA, Polish UI)
                   │  HTTPS: /        static assets
                   │  HTTPS: /api/**  REST (JSON)
                   │  WSS:   /ws      STOMP over WebSocket
                   ▼
            ┌──────────────┐
            │    Caddy     │  TLS (Let's Encrypt), static files, reverse proxy
            └──────┬───────┘
                   ▼
┌──────────────────────────────────────────────────────────────────────┐
│ thousand-backend (Spring Boot 4, Java 25) - one process              │
│                                                                      │
│  identity ── credentials, sessions, password reset, rate limiting    │
│  user ────── account view, self-service deletion (anonymization)     │
│  lobby ───── private tables, seats, join codes, start                │
│  match ───── match lifecycle, MatchState store + snapshots, turn     │
│              timers, presence/grace period, STOMP endpoints, history │
│  game ────── PURE DOMAIN: Tysiąc rules, variant configs, state       │
│              machine. No Spring/DB/WebSocket imports.                │
│  shared ──── small cross-cutting pieces (error model, clock, ids)    │
│                                                                      │
│  Allowed dependencies (enforced by ArchUnit):                        │
│   match → game, lobby(api) · lobby → identity(api)                   │
│   user → identity(api), lobby(api), match(api)                       │
│   game → nothing (JDK only)                                          │
└───────────┬──────────────────────────────────────┬───────────────────┘
            ▼                                      ▼
     PostgreSQL 17                       SMTP provider (external)
     accounts, sessions, tables,         password reset emails
     match history, match-state snapshots

 Observability (phased): Prometheus → Grafana, Loki (logs) → Grafana, later Tempo (traces)
```

### Module internals
Each module has the sub-packages it actually needs, drawn from `domain/`, `application/`, `infrastructure/` and `api/`. The `api/` package holds REST controllers, STOMP controllers and the module's public facade for other modules. Layers are not created speculatively. A module talks to another module only through that module's public `api` or application service, never through its infrastructure.

### Mapping from the current repository
| Current package | Target | Action (in Scaffold/Implement) |
|---|---|---|
| `room` | `lobby` | rename |
| `player` | `user` (+ `identity`) | rename/split |
| `game` | `game` (pure domain) + `match` (orchestration) | split orchestration out |
| `shared` | `shared` | keep, minimal |
| `ranking` | not created in MVP | deferred |

## Key Decisions
- **Pure game domain** (ADR-006). `game` is plain Java 25 with records and sealed interfaces, and has zero framework dependencies. It exposes `apply(state, action) → Result(newState, events) | Rejection`. Every rule in `ruleset.md` is tested here without Spring.
- **Variant configuration per player count** (ADR-006). The engine core is shared. `VariantConfig` holds the player-count differences and one explicit switch for every `CONFIRM` item. Any unset switch makes the variant non-startable, so there are no silent defaults.
- **Match state** (ADR-004). An in-memory `MatchState` is held per active match and serialized per match. After every accepted action a versioned snapshot is written to PostgreSQL, and the state is restored at startup. Matches therefore survive deploys, and the design is ready for later scaling.
- **Realtime** (ADR-005). STOMP runs over WebSocket with Spring's simple in-memory broker. Each player receives their own personalized view on a user queue, so hidden information can never be broadcast.
- **Auth** (ADR-003). Server-side sessions are stored in PostgreSQL (Spring Session JDBC) and carried in an HttpOnly cookie, with CSRF protection and Argon2id hashing.
- **No Redis in the MVP** (ADR-007). No use case justifies it at a single instance, and it would cost RAM on the €10 VPS.
- **Same-origin deployment** (ADR-008). Caddy serves the SPA and proxies `/api` and `/ws`, so there is no CORS and the cookies can be `SameSite=Strict`.
- **Timeout fallback** (ADR-009). A technical fallback, not a Tysiąc rule. On a timeout the server picks a deterministic, minimal legal move and never takes an optional action (bomb, marriage, redeal). It lives in `game.timeout`, separate from the rules.

## Authorization Model
Authentication proves who the user is. Authorization lives in the application services, never in controllers or the frontend.

| Action | Rule | Enforced in |
|---|---|---|
| Read or delete my account | Only the authenticated user, on their own account. Deletion requires the current password and no active seat (A-14). | `user` application service |
| Create a table | Authenticated, and not seated elsewhere (A-10) | `lobby` service + unique `table_seat.user_id` |
| View a table by code or join it | Authenticated. Joining requires a free seat and no other active seat. | `lobby` service |
| Leave a table | Only a seated player. A host leaving closes the table (A-15). | `lobby` service |
| Start a match | Only the host, and only when the table is full (A-06) | `lobby` service |
| Subscribe to or act in a match | Only a participant of that match. Each action is also validated by the `game` domain for turn and legality. | STOMP `ChannelInterceptor` (membership) + `match` service + `game` domain |
| Read match history | Only matches the user took part in | `match` query service |
| Actuator and metrics endpoints | Not exposed publicly; reachable only on the internal Docker network | Caddy routing + management port |

## First Bottleneck at the Stated Scale
- At fewer than 50 users and a few tables, **CPU and database load are negligible**. There is one snapshot write per game action, a few per second at peak.
- The first real limit is **VPS memory** once the JVM, PostgreSQL, Caddy, Prometheus, Grafana, Loki and later Tempo are co-located. This is handled in ADR-008 with a memory budget per container and phased observability.
- It is **consciously deferred**: multi-instance scaling. The seams are already in place: the `MatchStateStore` interface, the snapshot in PostgreSQL, and the STOMP broker relay option.

## Future Considerations
- **Second instance**: needed only if concurrent users grow by orders of magnitude. It would require:
  - per-match ownership, meaning routing a match to one node, or a distributed lock;
  - moving the STOMP broker to a relay (RabbitMQ) or Redis pub/sub;
  - moving the rate limiter to Redis.
  All three sit behind existing interfaces.
- **Ranking module**: consumes the `COMPLETED` match results that `match` already stores.
- **Extracting a service**: only with a new ADR showing a concrete need. No such need is foreseeable.
