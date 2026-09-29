# Realtime API: STOMP over WebSocket

This complements `openapi.yaml`. The design rationale is in ADR-005, and the timeout behaviour is in ADR-009.

## Connection
- URL: `wss://<domain>/ws`. This is the same origin as the SPA, and the `Origin` header is checked.
- Authentication: the `SESSION` cookie on the handshake (ADR-003). An unauthenticated handshake is rejected with 401.
- Heartbeats: 10 000 ms in both directions.
- Client library: `@stomp/stompjs`, with automatic reconnect using backoff of 1 s, 2 s, 5 s and then every 5 s.

## Destinations

| Direction | Destination | Who | Payload |
|---|---|---|---|
| SUBSCRIBE | `/user/queue/matches/{matchId}/view` | participants of the match | `PlayerView` (as in `openapi.yaml`) after every state change, and immediately on subscribe |
| SUBSCRIBE | `/user/queue/errors` | any authenticated user | `ActionRejected` |
| SUBSCRIBE | `/topic/tables/{tableId}` | players seated at that table | `Table` (seat list, status, `matchId` once started) |
| SEND | `/app/matches/{matchId}/actions` | participants of the match | `GameActionRequest` |

A `ChannelInterceptor` authorizes every SUBSCRIBE and SEND against match or table membership. Frames that fail authorization are dropped, and an `ActionRejected{code: FORBIDDEN}` is returned.

## GameActionRequest

```json
{
  "clientActionId": "uuid",
  "expectedVersion": 42,
  "action": { "type": "PLAY_CARD", "card": { "suit": "HEARTS", "rank": "QUEEN" }, "declareMarriage": true }
}
```

| `action.type` | Fields | Rules |
|---|---|---|
| `BID` | `value` | R-030..R-033 |
| `PASS` | none | R-031 |
| `REQUEST_REDEAL` | none | R-022, C-03 |
| `CHOOSE_MUSIK` | `index` (0 or 1) | R-041 [2p] |
| `DISCARD` | `cards[2]` | R-041 [2p] |
| `GIVE_CARDS` | `gifts: [{toSeat, card}]` | R-040 [3p], C-06 [4p] |
| `DECLARE_CONTRACT` | `value` | R-042, C-07 |
| `THROW_BOMB` | none | R-050, R-051, C-08..C-11 |
| `PLAY_CARD` | `card`, `declareMarriage` | R-060..R-064, R-070..R-073, C-12, C-13 |

**Processing guarantees** (ADR-004):
- Actions for one match are processed serially.
- An action whose `expectedVersion` doesn't match the current version is rejected with `STALE_VERSION`. The client then waits for, or re-fetches, the latest view.
- A duplicate `clientActionId` from the same seat is ignored, which makes actions idempotent.
- A rejected action never changes state (US-014 AC-5).

## ActionRejected

```json
{ "clientActionId": "uuid", "code": "MUST_FOLLOW_SUIT", "message": "..." }
```

Codes are stable identifiers. Examples:
- `NOT_YOUR_TURN`
- `STALE_VERSION`
- `ILLEGAL_PHASE`
- `BID_NOT_MULTIPLE_OF_10`
- `BID_TOO_LOW`
- `BID_ABOVE_120_REQUIRES_MARRIAGE`
- `BID_ABOVE_CAP`
- `CONTRACT_BELOW_BID`
- `MUST_FOLLOW_SUIT`
- `MUST_BEAT`
- `NO_MARRIAGE_IN_HAND`
- `REDEAL_NOT_ALLOWED`
- `CARD_NOT_IN_HAND`
- `FORBIDDEN`

The Polish UI maps each code to text.

## Presence and timers
- On a STOMP disconnect, the seat is marked `connected=false` with a `graceDeadline` of now + 2 minutes, and a view is pushed to the others (US-023 AC-3).
- On reconnect and subscribe, the grace timer is cancelled and the view is pushed (US-023 AC-1).
- If the grace period expires, the match ends as `FORFEIT` (US-024 AC-1). If all participants are gone, it ends as `ABANDONED` (A-08).
- On a turn timeout, the server applies the ADR-009 technical fallback move (not a game rule) and shows it neutrally in the view's event line as "Ruch automatyczny (czas minął)".
