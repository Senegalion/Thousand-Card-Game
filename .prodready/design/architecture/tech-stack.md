# Tech Stack

The stack is fixed by `CLAUDE.md` and `constraints.md`. This document validates it, fills the gaps, and resolves conflicts found in the repository. Exact patch versions are pinned in Scaffold. The lines below are the targets.

## Core

| Layer | Technology | Rationale |
|---|---|---|
| Language (backend) | Java 25 (LTS) | The owner's chosen stack, and the current LTS. Records, sealed interfaces and pattern matching suit an immutable game domain. **Fixes** `backend/pom.xml` `java.version=21`. |
| Runtime | Eclipse Temurin 25 JRE (container) | An LTS JRE with a small runtime image |
| Framework | Spring Boot 4.1.x (Web MVC, WebSocket/STOMP, Security, Validation, Actuator, Mail) | The owner's choice (ADR-001). The pom is already on 4.1.0. |
| Database | PostgreSQL 17 | Relational integrity for accounts, history and snapshots (ADR-002). The Define schema was validated on 17. |
| Data access | jOOQ (code generated from Flyway migrations via Testcontainers) | Type-safe SQL without an ORM (ADR-002) |
| ORM | none (JPA/Hibernate excluded) | Constitution |
| Migrations | Flyway | Versioned schema, including Spring Session tables |
| Sessions | Spring Session JDBC | Server-side sessions in PostgreSQL (ADR-003) |
| Password hashing | Spring Security `Argon2PasswordEncoder` + BouncyCastle | Argon2id is a non-negotiable. The encoder needs BouncyCastle on the classpath. |
| Rate limiting | Bucket4j (in-memory) | Single instance, so no Redis is needed (ADR-007) |
| Mail | Spring Mail (SMTP) + an EU-based provider's free tier (e.g. Brevo) | Provider-agnostic SMTP. EU hosting fits GDPR. Well within budget at MVP volume. |
| Language (frontend) | TypeScript 5.x (strict) | Owner's choice |
| Frontend | React 19, Vite 7, React Router 7, Zustand 5, Tailwind CSS 4 | Owner's choice. An SPA served as static files. |
| STOMP client | `@stomp/stompjs` | A maintained STOMP client with reconnect support |
| i18n | none (Polish strings only, centralized in one module) | The MVP is Polish-only (A-13). Centralized strings make adding i18n later a mechanical change. |

## Infrastructure

| Component | Technology | Rationale |
|---|---|---|
| Container | Docker, multi-stage builds | Portability, and the same images in CI and production |
| Orchestration | Docker Compose on one VPS | Constraint (ADR-008) |
| Reverse proxy | Caddy 2 | Automatic Let's Encrypt HTTPS, a simple config, serves the SPA and proxies `/api` and `/ws` with WebSocket upgrade |
| Registry | GitHub Container Registry (GHCR) | Free, and integrated with Actions |
| CI/CD | GitHub Actions | Constraint. Deploys to the VPS over SSH with a restricted deploy key. |
| VPS | Hetzner (EU), 8 GB RAM tier within €10/month | The memory budget is in ADR-008. The current price must be verified before ordering. |
| Metrics | Micrometer → Prometheus → Grafana | Phase 1 observability |
| Logs | Structured JSON logs → Grafana Alloy → Loki → Grafana | Phase 1 observability. Alloy also collects Docker logs. |
| Traces | OpenTelemetry (Micrometer Tracing / OTLP) → Tempo | Phase 2, added incrementally as agreed |

## Development

| Tool | Purpose |
|---|---|
| Maven Wrapper | Backend build |
| Spotless (palantir-java-format) | Java formatting |
| Checkstyle (light ruleset) | Java linting |
| Error Prone (optional, off by default) | Bug-pattern checks. Enabled only if it proves stable with Java 25. |
| ArchUnit | Enforces module boundaries and game-domain purity (no Spring, jOOQ or `java.net` in `game`) |
| JUnit 5 + AssertJ | Unit and integration tests |
| jqwik | Property-based tests for domain invariants (card conservation, hidden information) |
| Mockito | Only where a collaborator can't reasonably be real |
| Testcontainers (PostgreSQL) | Integration tests and jOOQ codegen |
| JaCoCo | Coverage, with a ≥ 90% gate on the `game` module |
| OWASP Dependency-Check or GitHub Dependabot + CodeQL | Dependency and security scanning in CI (success metric 3) |
| ESLint + Prettier | Frontend linting and formatting |
| Vitest + React Testing Library | Frontend unit tests |
| Playwright | End-to-end tests, including complete 2/3/4-player matches (US-020) |
| k6 | Load test for p95 ≤ 200 ms with 50 users (US-021 AC-2) |
| Lefthook | Git hooks: format and lint on commit |

**Removed:** Lombok, which the current pom includes. It isn't in the agreed stack, and Java records and plain constructors cover the need. This follows the rule of adding no technology without a concrete reason.

## Versions

```json
{
  "java": "25",
  "spring-boot": "4.1.x",
  "postgresql": "17",
  "jooq": "3.20.x (Spring Boot managed)",
  "flyway": "Spring Boot managed",
  "spring-session-jdbc": "Spring Boot managed",
  "bouncycastle": "bcprov-jdk18on 1.8x",
  "bucket4j": "8.x",
  "archunit": "1.4.x",
  "jqwik": "1.9.x",
  "testcontainers": "Spring Boot managed",
  "node": "24 LTS",
  "typescript": "5.x",
  "react": "19.x",
  "vite": "7.x",
  "react-router": "7.x",
  "zustand": "5.x",
  "tailwindcss": "4.x",
  "@stomp/stompjs": "7.x",
  "vitest": "3.x",
  "playwright": "1.x",
  "caddy": "2.x",
  "prometheus": "3.x",
  "grafana": "12.x",
  "loki": "3.x",
  "alloy": "1.x",
  "tempo": "2.x"
}
```

## Authentication

- Strategy: server-side session (ADR-003).
- Library: Spring Security 7 + Spring Session JDBC.
- Storage:
  - A `SESSION` cookie that is `HttpOnly`, `Secure`, `SameSite=Strict` and `Path=/`.
  - Session data lives in the PostgreSQL tables `spring_session` and `spring_session_attributes`.
- CSRF:
  - Spring Security `CookieCsrfTokenRepository`: the SPA reads `XSRF-TOKEN` and sends `X-XSRF-TOKEN` on mutating REST calls.
  - The STOMP CONNECT frame is authenticated by the session cookie on the same-origin handshake.
- Session invalidation:
  - Logout deletes the session.
  - Password reset and account deletion delete **all** of the user's sessions, found through the Spring Session principal-name index.
- Session timeout: 7 days idle, configurable.

## Monitoring

- Logging: SLF4J with Logback JSON encoding. Secret-bearing fields are never logged: a Logback masking pattern plus a test asserts that no password or token appears in logs (US-028 AC-2).
- Metrics: HTTP latency, JVM, active WebSocket sessions, active matches, game actions per second, rejected actions, and SMTP failures.
- Tracing: phase 2 (Tempo).
- Error tracking: none. Logs and Grafana alerts are enough for the MVP.
- Analytics: none. This is data minimization under GDPR.
