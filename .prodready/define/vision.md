# Vision

## Problem Statement
Players who know Tysiąc from Kurnik want to play complete, rule-correct matches with friends online for 2, 3 or 4 players. They need a place where the server enforces the rules faithfully, hidden cards stay hidden, and a dropped connection doesn't ruin a match that can last an hour.

For the author, the project also answers a second need: a serious, production-oriented portfolio system. It demonstrates sound domain modelling, test-first development, security, CI/CD, VPS operations and observability on a real product that real people use.

## Target Users
- **Player**: someone who knows Tysiąc, usually the Kurnik rules. They play private matches with friends they invite by link or code, need an account, and play on desktop or mobile browsers.
- **Operator**: the solo developer who deploys, monitors and maintains the production system on a single VPS. They are not an in-app administrator role.

## Core Value Proposition
A faithful, server-authoritative implementation of standard Kurnik-style Tysiąc for 2, 3 and 4 players. Players can trust every rule, score and hidden card, and play reliably in real time with friends.

## Success Metrics
The MVP is complete when all six criteria hold:
1. **Correctness**: every approved `R-xxx` rule in `ruleset.md` is covered by automated domain tests. Playwright end-to-end tests cover complete successful matches for 2, 3 and 4 players.
2. **Reliability**: a player who disconnects and reconnects within the 2-minute grace period continues the match with no loss of game state, verified by an automated test.
3. **Quality**: CI runs on every PR, with unit and integration tests, dependency and security scanning, and coverage reporting. The domain/game module has at least 90% test coverage. `main` is always deployable.
4. **Operations**: the public VPS deployment is deployable from CI over HTTPS, and metrics and logs are available in Grafana. Tracing is added incrementally.
5. **Performance**: p95 is at most 200 ms for typical application and API operations with up to 50 concurrent users. This is a target, not a guarantee for every operation.
6. **Real use**: complete real matches for 2, 3 and 4 players are played with friends in production, observable as `COMPLETED` match records per player count.

## MVP Scope

### Must Have (MVP)
- Game engine for 2, 3 and 4 players implementing `ruleset.md` (Kurnik rules), fully server-authoritative
- Registration and login with email, username and password
- Password reset by email through an external SMTP provider
- Self-service account deletion, with the user anonymized in shared match history
- Privacy policy page
- Private tables for 2, 3 or 4 players, joined by invite link or code
- Realtime gameplay over WebSocket/STOMP
- Turn timers with an automatic minimal legal move when a timer expires
- Reconnection within a 2-minute grace period, and forfeit after it expires
- Basic match history: participants, status, date and final scores
- Public deployment on one VPS with HTTPS, CI/CD, metrics and logs

### Nice to Have (Future)
- ELO/Glicko rating and leaderboards
- Public table list and automatic matchmaking
- Guest play and social login
- Bots taking over for disconnected players
- In-game chat, spectators, full replays
- Distributed tracing, if not finished in the MVP
- Horizontal scaling, with shared match state
- Additional rule variants (house rules), added as new variant configurations
