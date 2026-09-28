# ADR-008: Deployment Topology and Phased Observability

## Status
Accepted

## Date
2026-09-28

## Context
The constraints are:
- one public VPS (Hetzner or OVH, in the EU) with a budget of at most €10/month;
- Docker Compose, HTTPS, and deployment from CI;
- metrics and logs in Grafana, with tracing added incrementally;
- `main` is always deployable.

The main risk is memory: running the JVM, PostgreSQL, a proxy and the LGTM components together on one small VPS.

## Decision

### Topology (single Compose project)
| Service | Purpose | Memory limit |
|---|---|---|
| `caddy` | TLS (Let's Encrypt), serves the SPA `dist/`, proxies `/api/*` and `/ws` to the app | 64 MB |
| `app` | Spring Boot (JRE 25, `-XX:MaxRAMPercentage=60`) | 768 MB |
| `postgres` | PostgreSQL 17 with a named volume | 512 MB |
| `prometheus` | Scrapes `app:8081/actuator/prometheus` over the internal network; 15-day retention | 384 MB |
| `grafana` | Dashboards. Exposed only at `grafana.<domain>` through Caddy, with Grafana login and admin credentials from secrets. | 256 MB |
| `loki` | Log store, 14-day retention | 384 MB |
| `alloy` | Collects container logs, ships them to Loki | 192 MB |
| `tempo` (phase 2) | Traces over OTLP from the app | 384 MB |

- Phase 1 totals about 2.6 GB, and about 3 GB with Tempo, plus the OS.
- **Server size:** the 8 GB RAM tier within budget, which leaves headroom for builds and spikes. A 4 GB tier is feasible for phase 1 only, and without headroom. The current price must be verified before ordering.

### Networking
- The app is reachable only on the internal Docker network.
- Actuator runs on a separate management port (8081) that isn't routed by Caddy.
- The firewall allows only 22, 80 and 443.
- SSH is key-only, and the deploy user has restricted permissions.

### Delivery
- On each PR, GitHub Actions runs:
  - build
  - tests
  - coverage gates
  - dependency and security scanning
  - the Playwright E2E suite against a Compose stack
- On merge to `main`:
  1. Build and push the images (`app` and a `caddy` image containing the SPA build) to GHCR, tagged with the commit SHA.
  2. SSH to the VPS.
  3. `docker compose pull && docker compose up -d`.
  4. A smoke check (`/api/health` through Caddy).
  5. **On a failed smoke check:** retry up to 60 s, then automatically roll back to the previously deployed image tag, which is recorded on the VPS in `.deployed-tag`. Mark the workflow as failed so the operator is notified by GitHub. Flyway migrations must therefore be backward-compatible with the previous app version (the expand/contract pattern).
- Match durability across the restart comes from ADR-004.

### CI cost (owner answer, 2026-09-28)
- The GitHub repository is **public**, so GitHub Actions minutes for standard runners and GHCR storage for public packages are free.
- The full PR pipeline, including the Playwright E2E suite on a Compose stack, runs on every PR with no effect on the €10 budget.
- Images in GHCR are public. They contain no secrets, which are injected only at runtime on the VPS.

### Secrets and backups
- **Secrets:** stored in GitHub Actions secrets and in a root-only `.env` on the VPS. They are never in Git (the repo has `.env.example`).
- **Backups:** a nightly `pg_dump`, compressed and copied off the VPS. The destination is decided in Build and must fit the budget. A restore procedure is documented in `DEPLOYMENT.md`.

### Observability phases
1. **Phase 1 (MVP gate, success metric 4):**
   - Micrometer metrics in Prometheus, shown in Grafana:
     - HTTP latency (p95)
     - JVM
     - active WebSocket sessions
     - active matches
     - rejected actions
     - mail failures
   - JSON logs through Alloy into Loki, shown in Grafana.
   - Basic alerts: app down, p95 above 200 ms, disk above 80%.
2. **Phase 2 (incremental):** OpenTelemetry traces (Micrometer Tracing + OTLP) sent to Tempo, with trace IDs correlated in the logs.

## Consequences

### Positive
- The whole production system is reproducible from Git plus secrets.
- Observability is real rather than aspirational, while staying within budget.
- There is no managed-cloud lock-in, and the operations are themselves a portfolio asset.

### Negative
- A single point of failure: VPS downtime means product downtime. That is acceptable for the MVP.
- Each deploy causes a restart blip of a few seconds. Matches survive (ADR-004), but REST calls made during the blip fail and the SPA retries them.
- The operator maintains the OS patches (unattended-upgrades).

### Risks
- **Risk:** running out of memory.
  - **Mitigation:** per-container limits, Grafana memory panels, Tempo deferred, and the option of an 8 GB tier.
- **Risk:** Let's Encrypt rate limits on repeated redeploys.
  - **Mitigation:** Caddy's certificate storage lives on a persistent volume.
- **Risk:** GHCR or SSH secrets leak.
  - **Mitigation:** least-privilege deploy key, GitHub environment protection on `production`, and secret rotation documented.

## Alternatives Considered
1. **Nginx + Certbot:** Rejected because it takes more configuration and a separate certificate-renewal process. Caddy does automatic HTTPS in a few lines.
2. **Traefik:** Rejected because its label-driven configuration is powerful but heavier to reason about for a single static topology.
3. **Grafana Cloud free tier instead of self-hosted LGTM:** saves VPS RAM. Rejected for now because self-hosting is part of the learning and portfolio goal (Q9), and it keeps personal data such as logs with IP addresses on EU infrastructure the operator controls. It remains the fallback if memory becomes a problem.
4. **Managed PaaS (Fly.io, Render):** Rejected because the owner chose a VPS for the operations learning value, and it risks cost growth beyond €10.
