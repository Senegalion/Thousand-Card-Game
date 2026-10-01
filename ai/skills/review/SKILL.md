---
name: review
description: Review code in Thousand Online for architecture, correctness, security, testing, and maintainability.
---

# Code Review

Review the requested code or changes against the project architecture and engineering rules.

## Review priorities

1. Correctness and potential bugs
2. Security issues
3. Architecture violations
4. Missing or weak tests
5. Maintainability and unnecessary complexity

## Project-specific rules

- The backend is a modular monolith.
- Prefer package-by-feature/module.
- Keep game domain logic independent of Spring, HTTP, WebSocket, PostgreSQL, Redis, and infrastructure.
- The backend is authoritative for game rules, state, authentication, and authorization.
- Do not introduce JPA/Hibernate.
- Use jOOQ for PostgreSQL persistence.
- Do not put business logic in controllers.
- Do not introduce abstractions or technologies without a concrete reason.
- Keep changes focused and consistent with existing architecture.

## Review process

1. Inspect the relevant code and surrounding context.
2. Identify concrete problems rather than stylistic preferences.
3. Prioritize findings by severity.
4. Explain why each finding matters.
5. Suggest a concrete fix when appropriate.

Do not modify files during a review unless explicitly requested.

## Output

Return:

### Findings

List concrete findings from most important to least important.

For each finding include:
- Severity
- Location
- Problem
- Why it matters
- Suggested fix

### Positive aspects

Mention important things that are implemented correctly.

### Verdict

Give a short summary of the overall state of the reviewed code.
