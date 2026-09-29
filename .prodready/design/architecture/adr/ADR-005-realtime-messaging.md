# ADR-005: Realtime Messaging (STOMP over WebSocket)

## Status
Accepted

## Date
2026-09-28

## Context
Players need live updates (US-021), presence and disconnect detection (US-023 AC-3, US-024), and a channel to submit game actions. The constitution requires:
- **hidden information**: a client must never receive other hands, the unrevealed musik, or discards;
- **server authority**: every client message is an intention, validated server-side.

`CLAUDE.md` fixes WebSocket with STOMP.

## Decision
Use **Spring WebSocket with STOMP** and the **simple in-memory broker**. The design follows these rules:
- **Endpoint:** `/ws`, same-origin. The handshake is authenticated by the session cookie (ADR-003), and a `ChannelInterceptor` rejects any SUBSCRIBE or SEND for a match the user doesn't participate in.
- **Client → server (application destinations):** `/app/matches/{matchId}/actions`. The payload is a tagged union:
  - `BID`
  - `PASS`
  - `CHOOSE_MUSIK`
  - `DISCARD`
  - `GIVE_CARDS`
  - `DECLARE_CONTRACT`
  - `THROW_BOMB`
  - `PLAY_CARD` (with `declareMarriage`)
  - `REQUEST_REDEAL`

  Every action carries `expectedVersion` and `clientActionId`.
- **Server → client:**
  - `/user/queue/matches/{matchId}/view` carries the **full personalized `PlayerView`** after every state change: own hand, public state, timers and presence.
  - `/user/queue/errors` carries rejections, each with a stable error code.
  - Nothing match-specific is sent to a shared `/topic`, so hidden information can't leak through broadcasting.
  - `/topic/tables/{tableId}` carries lobby seat updates. These are public to seated players and checked by the interceptor.
- **Full views, not deltas:** a view is small (about 2 KB), and full views make reconnection and stale clients trivial. The client replaces its state with every message. This fits the rule that the frontend never assumes a predicted action was accepted.
- **Presence:**
  - STOMP heartbeats run every 10 s.
  - On the `SessionDisconnectEvent`, the `match` module starts the 2-minute grace timer.
  - On reconnect and re-subscribe, the timer is cancelled and the current view is sent immediately (US-023 AC-1).
- **Reconnection fallback:** `GET /api/matches/{id}/view` returns the same `PlayerView` over REST, for the initial load and for debugging.

## Consequences

### Positive
- Per-user destinations make hidden information structurally safe.
- A single message type, the full view, simplifies both the client and the tests.
- There is no external broker to run or pay for.

### Negative
- The simple broker is in-process, so it cannot fan out across several instances.
- Sending full views costs more bandwidth than deltas. That is negligible at this scale.

### Risks
- **Risk:** a future second instance.
  - **Mitigation:** switch to the STOMP broker relay (RabbitMQ) or Redis pub/sub. This is a configuration change behind Spring's broker abstraction, and it is documented in `pattern.md`.
- **Risk:** proxy timeouts dropping idle WebSockets.
  - **Mitigation:** 10 s heartbeats, plus Caddy's WebSocket support with no read timeout on `/ws`.

## Alternatives Considered
1. **Raw WebSocket with a custom protocol:** Rejected because it means re-implementing subscriptions, user destinations and heartbeats, which STOMP already provides, for no gain.
2. **Server-Sent Events + REST for actions:** Rejected because it is one-way. Presence detection is weaker, and the owner fixed WebSocket/STOMP.
3. **External broker relay (RabbitMQ) now:** Rejected because it adds a service and about 150 MB of RAM that are unjustified at a single instance. It is kept as the scaling path.
