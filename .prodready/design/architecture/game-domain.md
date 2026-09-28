# Game Domain Design (`game` module)

The authoritative implementation of `.prodready/define/ruleset.md`. It is pure Java with no framework, I/O or clock access. Time is passed in by `match`.

## Core concepts
- `Card(Suit, Rank)`:
  - Rank strength follows R-002.
  - Point values follow R-003.
  - `Suit` carries its marriage value (R-004).
- `Seat`: an index from 0 to playerCount−1, in the order players go to the left (R-011).
- `MatchState`: an immutable record containing:
  - variant config
  - game scores
  - dealer
  - bomb counters
  - barrel info
  - the current `Round`
  - status
  - a monotonically increasing `version`
- `Round`:
  - phase
  - hands
  - musik(s)
  - auction
  - declarer
  - contract
  - trump
  - tricks
  - declared marriages
  - the discard pile (2p)
- `Action`: a sealed interface. Its implementations are:
  - `Bid(seat, value)`
  - `Pass(seat)`
  - `ChooseMusik(seat, index)` [2p]
  - `Discard(seat, cards)` [2p]
  - `GiveCards(seat, Map<Seat, Card>)` [3p/4p]
  - `DeclareContract(seat, value)`
  - `ThrowBomb(seat)`
  - `PlayCard(seat, card, declareMarriage)`
  - `RequestRedeal(seat)`
- `GameEngine.apply(MatchState, Action, Randomness) → Accepted(MatchState, List<DomainEvent>) | Rejected(reason)`:
  - The engine is deterministic given its `Randomness`, which is used for shuffling and the first dealer.
- `game.timeout.TimeoutFallback.choose(MatchState, Seat) → Action`:
  - This is a **technical fallback, not a rule** (ADR-009).
  - It chooses among `GameEngine.legalActions(state, seat)` using the implementation-detail ordering documented in ADR-009, and never returns an optional action (bomb, marriage, redeal).
  - `match` calls it when a turn deadline passes and submits the result through the normal `apply`.
- `PlayerView project(MatchState, Seat)`:
  - This is the only way state leaves the domain toward clients.
  - It contains the player's own hand, public information and the revealed musik, and nothing hidden. This enforces the hidden-information non-negotiable.

## Round state machine

```
DEALT ──(four-9s redeal window, R-022/C-03)──► BIDDING ──► MUSIK ──► CONTRACT ──► TRICKS ──► SCORED
  ▲                                                          │   (bomb point: C-09)            │
  └───────────────────────────── next round (dealer → left, R-010) ◄──────────────────────────┘
                                                                                     └─► GAME_OVER (R-110/R-111)
MUSIK sub-steps: 3p/4p: reveal → take → give 1 card to each opponent (R-040, C-06)
                 2p: choose 1 of 2 → reveal chosen → discard 2 face down (R-041)
```

Where exactly the redeal window and the bomb point sit in this machine depends on C-03 and C-09. Their positions are parameters of `VariantConfig`, not hard-coded.

## Variant configuration

`VariantConfig` is a record built per player count by `Variants.forPlayerCount(n)`.

| Field | 2p | 3p | 4p | Source |
|---|---|---|---|---|
| `activePlayers` | 2 | 3 | 3 | R-020/R-021, C-01 |
| `sittingOutPolicy` | NONE | NONE | **unset (C-01)** | C-01 |
| `dealLayout` | 10+10, musiks [2,2] | 7+7+7, musik [3] | **unset (C-02)** | R-020, R-021, C-02 |
| `musikProcedure` | CHOOSE_ONE_DISCARD_TWO | REVEAL_TAKE_GIVE_ONE_EACH | **unset (C-06)** | R-040, R-041, C-06 |
| `leftoverMusikPointsTo` | LAST_TRICK_WINNER | n/a | n/a | R-081 |
| `sittingOutScoring` | n/a | n/a | ACE_50_PLUS_MARRIAGES | R-091 |
| `bombPenaltyPerOpponent` | 60 | 60 | 40 | R-051 |

The `RuleOptions` fields below are shared by all counts. Every one of them is **unset until verified in Kurnik**.

| Option | Verifies | Possible values (from `ruleset.md`, not defaults) |
|---|---|---|
| `redealProcedure` | C-03 | timing / redealer / sitting-out applicability, to be defined from observation |
| `auctionReentry` | C-04 | ALLOWED / NOT_ALLOWED |
| `unchosenMusikVisibility` | C-05 | HIDDEN / REVEALED_AFTER_ROUND / … |
| `finalContractCap` | C-07 | SAME_AS_BID_CAP_WITH_MUSIK / NONE |
| `freeBombScope` | C-08 | PER_PLAYER / PER_GAME |
| `bombDecisionPoint` | C-09 | as observed |
| `sittingOutBombBonus` | C-10 | as observed |
| `barrelPlayersGetBombBonus` | C-11 | YES / NO |
| `voidSuitObligation` | C-12 (i)(ii) | as observed |
| `beatLedSuitAfterTrumped` | C-12 (iii) | YES / NO |
| `marriageOnFirstLead` | C-13 | ALLOWED / NOT_ALLOWED |
| `negativeScores` | R-085 | ALLOWED / FLOOR_AT_ZERO |
| `sittingOutGainsOnBarrel` | R-103 | YES / NO |

**Fail-fast rule:**
- `Variants.forPlayerCount(n)` throws `UnverifiedRuleException` listing every unset option.
- Until an option has been verified and set in code, citing its evidence in `ruleset.md`, matches of the affected player counts cannot start. The lobby shows that player count as unavailable.
- This makes the "no silent defaults" constitution rule mechanically enforced rather than a convention.

Adding a future variant, such as a house-rules table, means adding a new `VariantConfig` and nothing more. The engine code does not change.

## Testing strategy for the domain
- One test class per rule area. Tests are named after rule IDs, for example `R061_mustFollowSuit`.
- Canonical `AC-N:` tests live alongside them.
- Deterministic deals through a seeded `Randomness` test double.
- Property-based checks with jqwik:
  - card conservation (24 cards always accounted for)
  - no hidden card ever appears in another seat's `PlayerView`
- Coverage gate of at least 90% for the `game` module (JaCoCo, success metric 3).
