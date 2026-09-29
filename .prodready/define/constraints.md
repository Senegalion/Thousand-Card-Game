# Constraints

## Deployment
- Target: a single small public VPS (Hetzner or OVH) running Docker Compose.
- Co-located on the VPS: the application, PostgreSQL, and the required infrastructure (Redis if justified, observability stack).
- HTTPS is required in production.
- Deployment is triggered from CI (GitHub Actions).
- Region: EU, following from the Hetzner/OVH choice and GDPR.

## Scale
- Launch: fewer than 50 concurrent users, a few simultaneous tables, one application instance.
- 6 months: the same order of magnitude. Horizontal scaling is not required.
- Design rule: avoid assumptions that make scaling impossible later. In particular, in-progress match state must be able to move to shared or persistent storage.
- Performance target: p95 of at most 200 ms for typical application and API operations at this load.

## Budget
- Infrastructure: at most €10 per month for the initial MVP (VPS and any paid services).
- Email: an external SMTP provider, which must fit within the budget (a free tier is expected at MVP volume).
- Tooling: free tiers (GitHub Actions, open-source observability stack).
- Risk: running the app, PostgreSQL, Redis, Prometheus, Grafana, Loki and Tempo on one €10 VPS is memory-tight. Observability components may be phased in (see `constitution.md`), and sizing is decided in Design.

## Compliance & Security
- GDPR minimum:
  - privacy policy
  - self-service account deletion with irreversible anonymization of shared match history
  - personal data limited to email, username and password hash
  - data access and portability requests handled manually (assumption A-11)
- Passwords hashed with Argon2id.
- Rate limiting on login, password reset and other authentication-sensitive endpoints.
- HTTPS everywhere in production.
- Server-authoritative for all game actions and rules; all client input validated.
- Hidden game information is never sent to clients who aren't entitled to it.
- Passwords, tokens and secrets are never logged, and secrets are never committed to Git.
- Session/token handling appropriate to the auth architecture chosen in Design.
- Password reset must not reveal whether an account exists (assumption A-04).

## Tech Stack Preferences
- Language: Java 25 (backend), TypeScript (frontend)
- Framework: Spring Boot 4.x (REST, WebSocket/STOMP, Spring Security); React with Vite, Tailwind CSS, React Router and Zustand
- Database: PostgreSQL, with migrations in Flyway
- ORM: none. jOOQ is used for type-safe SQL, and JPA/Hibernate is explicitly excluded.
- Build: Maven (backend)
- Testing: JUnit 5, AssertJ, Mockito, Testcontainers, Playwright
- UI language: Polish only for the MVP
- Additional:
  - Redis only for justified ephemeral/realtime needs
  - Docker and Docker Compose
  - GitHub Actions
  - OpenTelemetry, Prometheus, Grafana, Loki and Tempo
  - the architecture is a modular monolith, with no microservices
