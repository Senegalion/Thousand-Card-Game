# ADR-003: Authentication Strategy

## Status
Accepted (owner decision, 2026-09-28)

## Date
2026-09-28

## Context
The requirements are:
- Email and password accounts, with Argon2id hashing.
- Revocation on logout (US-002 AC-4).
- Revocation of **all** of a user's sessions on password reset (US-003 AC-5) and on account deletion.
- Rate limiting of authentication endpoints (US-002 AC-3).
- A WebSocket/STOMP connection that is authenticated as the same user.
- The browser SPA and the API are served from the same origin (ADR-008).

`backend/CLAUDE.md` mentions "JWT-based authentication where appropriate". This ADR records why JWT is not appropriate here.

## Decision
We will use **server-side sessions** with **Spring Security 7 + Spring Session JDBC**, stored in PostgreSQL:
- **Cookie:** `SESSION`, `HttpOnly`, `Secure`, `SameSite=Strict`. It is never readable from JavaScript.
- **CSRF:** a `CookieCsrfTokenRepository` for REST. The SPA echoes `X-XSRF-TOKEN`. The STOMP handshake is same-origin and cookie-authenticated, and the handshake also checks the `Origin` header.
- **Password hashing:** `Argon2PasswordEncoder` (Argon2id). Its parameters are tuned so a hash takes about 250 ms or less on the VPS.
- **Revocation:** `FindByIndexNameSessionRepository` is used to delete every session of a user on password reset and on account deletion. `credentials_changed_at` is kept as a second line of defence.
- **Rate limiting:** Bucket4j buckets keyed by IP and by account on `/api/auth/login`, `/api/auth/register` and `/api/auth/password-reset/*`. Exceeding a limit returns HTTP 429 (A-02).
- **Password reset:**
  - A 256-bit random token is emailed, and only its SHA-256 hash is stored.
  - The token is single-use and expires after 30 minutes.
  - The request endpoint always returns 202, so accounts can't be enumerated (A-03, A-04).
- **`backend/CLAUDE.md`:** to be updated to say "server-side sessions (see ADR-003)" instead of JWT.

## Consequences

### Positive
- Revocation requirements are met trivially: delete the rows.
- No token can be stolen by XSS, because the cookie is HttpOnly.
- The WebSocket authenticates automatically through the cookie, with no token-in-URL or CONNECT-header handling.
- There is no extra service (sessions live in PostgreSQL).

### Negative
- A database lookup per request. This is negligible at 50 users, and Spring Session can cache it.
- The server is stateful. Going multi-instance works because the sessions are already in shared PostgreSQL.
- A future native mobile client would need a token-based flow. That client is out of scope, so this is acceptable.

### Risks
- **Risk:** CSRF misconfiguration on the SPA.
  - **Mitigation:** integration tests that call mutating endpoints without the CSRF header and expect 403.
- **Risk:** Argon2 is too slow on a small VPS.
  - **Mitigation:** benchmark the parameters in Scaffold, and use a login rate limit to prevent CPU exhaustion.
- **Risk:** the SMTP provider is down during a password reset.
  - **Mitigation:**
    - The user still sees the generic "if the account exists, an email was sent" message.
    - The send is retried with backoff (3 attempts).
    - A failure increments `mail_send_failures_total` and logs an error without the token.
    - The user can request again later.

## Alternatives Considered
1. **JWT access token + refresh token:** Rejected because:
   - The revocation requirements force a server-side refresh-token table or a denylist, so it ends up stateful anyway, with more moving parts.
   - Storing tokens in JS-readable storage exposes them to XSS.
   - The WebSocket would need token handling in the CONNECT frame.
2. **Plain JWT (no refresh), short expiry:** Rejected because logout and password reset could not take effect immediately, which violates US-002 AC-4 and US-003 AC-5.
3. **Spring Session with Redis:** Rejected because it adds a service and uses RAM for no benefit at a single instance (ADR-007). PostgreSQL is sufficient.
4. **External identity provider (Keycloak, Auth0):** Rejected because:
   - Keycloak's memory footprint doesn't fit the €10 VPS.
   - SaaS options add cost and processing of personal data by a third party.
   - Social login is a non-goal for the MVP.
