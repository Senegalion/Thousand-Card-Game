
# Thousand Online

## Project

Thousand Online is an online multiplayer implementation of the Polish card game Tysiąc for 2–4 players.

The project is a production-oriented portfolio project focused on sound software engineering, AI-assisted development, testing, DevOps, security, and observability.

## Architecture

The backend is a modular monolith.

Do not introduce microservices unless there is a concrete architectural reason and the decision is explicitly discussed and documented.

Use package-by-feature/module boundaries rather than one global technical-layer structure.

The main backend modules are expected to include:

- identity
- user
- lobby
- match
- game
- ranking

The exact module structure may evolve as the system grows.

The game domain is the authoritative source of Tysiąc rules.

Core game logic must remain independent of Spring, HTTP, WebSocket, PostgreSQL, Redis, and other infrastructure.

The backend is always authoritative for game state, game rules, authentication, and authorization.

## Technology Decisions

### Backend

- Java 25
- Spring Boot 4.1.x
- Maven
- REST
- WebSocket with STOMP
- Spring Security with server-side sessions (Spring Session JDBC), no JWT (ADR-003)

### Persistence

- PostgreSQL
- jOOQ
- Flyway
- No JPA/Hibernate

### Infrastructure

- Redis for justified ephemeral/realtime use cases (deferred, not part of the MVP stack; see ADR-007)
- Docker
- Docker Compose

### Frontend

- React
- TypeScript
- Vite
- Tailwind CSS
- React Router
- Zustand

### Testing

- JUnit 5
- AssertJ
- Mockito where appropriate
- Testcontainers
- Playwright for end-to-end tests

### CI/CD and Observability

- GitHub Actions
- OpenTelemetry
- Prometheus
- Grafana
- Loki
- Tempo

## Engineering Principles

- Prefer simple, maintainable solutions over unnecessary complexity.
- Do not introduce technologies or abstractions without a concrete reason.
- Keep business logic out of controllers and infrastructure code.
- Keep domain logic independently testable.
- The frontend must never be trusted to enforce business rules or authorization.
- Make incremental, focused changes.
- Inspect existing code before modifying it.
- Run relevant tests and checks after changes.
- Do not assume existing code is architecturally correct.
- If existing code conflicts with the intended architecture, identify the conflict instead of blindly preserving it.

## Git Workflow

Use trunk-based development with short-lived feature branches.

Keep `main` stable.

Prefer small, focused commits.

Do not mix unrelated changes in one commit.

## Agent Behavior

Before implementing a non-trivial task:

1. Inspect the relevant project structure and existing code.
2. Identify applicable architectural constraints.
3. Form a short implementation plan.
4. Implement incrementally.
5. Run relevant tests and checks.
6. Review the final Git diff.
7. Report what changed and what was verified.

When an architectural decision is unclear or would materially affect the system, explain the trade-offs and ask before making a significant change.
