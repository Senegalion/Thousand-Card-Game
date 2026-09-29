# Story → Interface Coverage

Every P0 story is mapped to the interface that implements it. REST endpoints are in `openapi.yaml`, and STOMP destinations are in `websocket.md`.

| Story | Priority | Interface |
|---|---|---|
| US-001 Register | P0 | `POST /auth/register`, `GET /auth/csrf` |
| US-002 Login/logout | P0 | `POST /auth/login`, `POST /auth/logout`, `GET /me` |
| US-003 Password reset | P0 | `POST /auth/password-reset/request`, `POST /auth/password-reset/confirm` |
| US-004 Delete account | P0 | `DELETE /me`; anonymization in history is visible through `GET /me/matches` and `GET /matches/{id}` (`PlayerRef.deleted`) |
| US-005 Privacy policy | P0 | Static SPA route `/polityka-prywatnosci`. No API needed, since the content is static. |
| US-006 Create table | P0 | `POST /tables`, `GET /tables/available-player-counts` |
| US-007 Join table | P0 | `GET /tables/{joinCode}`, `POST /tables/{joinCode}/join`, STOMP `/topic/tables/{tableId}` |
| US-008 Start match | P0 | `POST /tables/{joinCode}/start` |
| US-009 Leave table | P1 | `POST /tables/{joinCode}/leave` |
| US-010 Empty states | P1 | `GET /me/current`, `GET /me/matches` |
| US-011 Deal | P0 | STOMP view `/user/queue/matches/{id}/view`, `GET /matches/{id}/view` |
| US-012 Bidding | P0 | STOMP `BID`, `PASS` |
| US-013 Musik / contract | P0 | STOMP `CHOOSE_MUSIK`, `DISCARD`, `GIVE_CARDS`, `DECLARE_CONTRACT` |
| US-014 Trick play | P0 | STOMP `PLAY_CARD`, `/user/queue/errors` |
| US-015 Marriages | P0 | STOMP `PLAY_CARD{declareMarriage}` |
| US-016 Round scoring | P0 | Domain. The result is exposed in the view (`seats[].gameScore`, round summary). |
| US-017 Bomb | P0 | STOMP `THROW_BOMB` |
| US-018 Redeal | P0 | STOMP `REQUEST_REDEAL` |
| US-019 Game end | P0 | Domain + view `phase=GAME_OVER`; recorded to history (`GET /matches/{id}`) |
| US-020 Full matches E2E | P0 | All of the above (Playwright) |
| US-021 Realtime | P0 | STOMP view push |
| US-022 Turn timer | P0 | Server-side timer (ADR-009); `turnDeadline` in the view |
| US-023 Reconnect | P0 | STOMP reconnect + re-subscribe; `GET /me/current`; `GET /matches/{id}/view` |
| US-024 Forfeit | P0 | Server-side grace timer; history status `FORFEIT` / `ABANDONED` |
| US-025 History | P0 | `GET /me/matches`, `GET /matches/{id}` |
| US-026 Stats-eligible | P1 | Internal query in the `match` module. No public endpoint, because ranking is deferred. |
| US-027 CI/CD | P0 | GitHub Actions + `GET /health` smoke check |
| US-028 Observability | P0 | `/actuator/prometheus` on the internal management port → Grafana |
