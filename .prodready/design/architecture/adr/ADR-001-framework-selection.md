# ADR-001: Framework Selection

## Status
Accepted

## Date
2026-09-28

## Context
The backend must serve REST, realtime WebSocket/STOMP, session-based security, SMTP email, and metrics, all in a single deployable unit on a small VPS. The owner has fixed the language and framework in `CLAUDE.md`: Java 25 and Spring Boot 4.x. One goal of the portfolio is to show production-grade Java engineering. The repository already contains a Spring Boot 4.1.0 skeleton, although its `pom.xml` targets Java 21 and includes Lombok.

## Decision
We will use **Java 25 with Spring Boot 4.1.x** (Web MVC, WebSocket/STOMP messaging, Security 7, Validation, Actuator, Mail) for the backend, and **React 19 + TypeScript + Vite** for the SPA frontend, because:
- It is the owner's explicit, portfolio-relevant stack, and the owner has chosen to learn it deeply.
- Spring provides integrated, mature building blocks for every requirement: STOMP with user destinations for per-player views, Spring Security with Spring Session for revocable sessions, Actuator/Micrometer for Prometheus, and Spring Mail.
- Java 25 records and sealed types are a good fit for an immutable, rule-heavy domain model that stays free of any framework.

The skeleton is aligned to this decision:
- `java.version` goes from 21 to 25.
- Lombok is removed.
- The README is corrected to say Java 25 and Spring Boot 4.

## Consequences

### Positive
- One coherent ecosystem for the web, security, messaging and observability concerns.
- Strong testing support (Spring Boot test slices, Testcontainers integration).
- The game domain stays plain Java and so is portable across frameworks.

### Negative
- JVM memory footprint (about 300–500 MB of heap plus overhead) on a memory-constrained VPS. This is mitigated by container limits and JVM flags (ADR-008).
- Spring Boot 4 is relatively new, so some third-party libraries may lag in compatibility.

### Risks
- **Risk:** library incompatibility with Boot 4 or Java 25, for example Error Prone or jOOQ codegen plugins.
  - **Mitigation:** verify each library during Scaffold. Keep optional tools off by default.

## Alternatives Considered
1. **Quarkus (Java 25)**: faster startup and a lower memory footprint. Rejected because:
   - the owner explicitly chose Spring Boot;
   - its WebSocket/STOMP and session ecosystems are thinner;
   - the memory saving isn't decisive at a single instance.
2. **Node.js (NestJS) + TypeScript full-stack**: would share one language with the frontend. Rejected because:
   - it contradicts the owner's fixed stack;
   - it loses the portfolio goal of Java/Spring depth.
3. **Keep Java 21 as in the current pom**: Rejected because `CLAUDE.md` specifies Java 25, which is the current LTS, and nothing requires 21.
