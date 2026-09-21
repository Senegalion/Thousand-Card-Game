# Backend Development Context

## Stack

The backend uses:

- Java 25
- Spring Boot 4.x
- Maven
- PostgreSQL
- jOOQ
- Flyway
- Redis
- Spring Security
- REST
- WebSocket with STOMP

## Architecture

The backend is a modular monolith organized by business feature.

Expected modules include:

- identity
- user
- lobby
- match
- game
- ranking

Avoid creating a global structure such as:

    controller/
    service/
    repository/
    entity/

for the entire application.

Technical layers should exist inside meaningful feature/module boundaries.

A module may use structures such as:

    feature/
    ├── domain/
    ├── application/
    ├── infrastructure/
    └── presentation/

Do not create all layers automatically. Add them when they provide a meaningful responsibility boundary.

## Domain

The `game` module contains the authoritative Tysiąc game rules.

The game domain must not depend on:

- Spring
- Spring Boot
- PostgreSQL
- jOOQ
- Redis
- HTTP
- WebSocket

Game rules must be testable without starting Spring or external infrastructure.

Controllers, WebSocket handlers, and repositories must not contain game rules.

## Persistence

Use PostgreSQL as the persistent source of truth.

Use jOOQ for database access.

Do not use JPA or Hibernate.

Use Flyway for database schema migrations.

Do not manually modify the database schema without a corresponding migration.

Keep persistence concerns separate from the domain model where appropriate.

## Redis

Redis is not the primary persistent database.

Use Redis only for justified ephemeral/realtime concerns such as:

- active room state
- player presence
- temporary data
- caching
- Pub/Sub
- future WebSocket scaling

Every Redis use should have a clear reason.

## REST

REST controllers should remain thin.

A controller should:

1. receive and validate input,
2. delegate to application logic,
3. map the result to an API response.

Business rules must not be implemented in controllers.

## WebSocket

WebSocket is used for real-time game communication.

The server is authoritative.

A client message represents an intention, not an already-valid action.

The server must validate:

- authentication
- authorization
- game membership
- current game state
- current turn
- action legality
- relevant Tysiąc rules

Consider reconnects, duplicate messages, stale clients, and concurrent actions when designing realtime functionality.

## Security

Use Spring Security.

Use JWT-based authentication where appropriate.

Never trust frontend authorization decisions.

Never store passwords in plaintext.

Never commit or log secrets, credentials, passwords, or tokens.

## Testing

Prefer fast unit tests for the domain.

Infrastructure-dependent tests should use Testcontainers with real PostgreSQL and Redis where appropriate.

Important boundaries should have integration tests.

WebSocket behavior should be tested separately from the pure domain logic.

## Java Style

Prefer:

- clear names
- small focused classes
- immutable data where practical
- records where appropriate
- constructor injection
- explicit types when inference would reduce readability

Avoid:

- field injection
- giant service classes
- unnecessary abstractions
- static global state
- business logic in infrastructure classes
- speculative functionality