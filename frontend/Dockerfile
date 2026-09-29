# syntax=docker/dockerfile:1
# Frontend image. Build context: ./frontend.
# Stages: deps -> dev (compose.override.yaml) -> build -> production (default target).
# Base images are Docker Hardened Images (dhi.io):
#   - node:24-alpine3.23-dev: Node 24 with npm and a shell, used only to install and build.
#   - caddy:2.11-debian13: minimal runtime (no shell, no package manager), non-root by default.
# Production serves the static SPA bundle with Caddy (ADR-008).

FROM dhi.io/node:24-alpine3.23-dev AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --no-audit --no-fund

FROM dhi.io/node:24-alpine3.23-dev AS dev
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
EXPOSE 5173
CMD ["npm", "run", "dev"]

FROM dhi.io/node:24-alpine3.23-dev AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM dhi.io/caddy:2.11-debian13 AS production
COPY Caddyfile /etc/caddy/Caddyfile
COPY --from=build /app/dist /srv
# DHI Caddy already defaults to this user; stated explicitly so the image never runs as root.
USER 65532:65532
EXPOSE 8080
