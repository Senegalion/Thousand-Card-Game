# ADR-009: Turn Timeout Fallback Policy

## Status
Accepted with owner changes (2026-09-28)

## Date
2026-09-28

## Context
The owner's decisions so far:
- Turn timers are 60 s for card play and 60 s for bidding and musik decisions (Q13, A-09).
- When a timer expires for a connected player, the server makes "the smallest legal/default move": pass in bidding if passing is allowed, and the lowest legal card in trick play. A single expiry never loses the match (Q7).
- The owner delegated the behaviour for the other decision points to Design, on condition that it follows the ruleset and invents no game rules (Q13).

## Nature of this policy (owner clarification)
**These are technical fallback behaviours of the application, not Tysiąc rules.**
- They do not belong in `ruleset.md`, and they must never be presented to players as rules of the game.
- They only choose, on an absent player's behalf, one move from the set of moves `ruleset.md` already allows at that moment.
- Wherever the policy takes no automatic action, the game simply continues according to the existing game flow and `ruleset.md`, exactly as if the player had declined that optional action. No new rule is created.
- In code, the policy lives in its own package (`game.timeout`), separate from the rule implementation, and it depends only on the engine's "legal moves" query.

## Decision

| Decision point | Fallback on timeout | Basis |
|---|---|---|
| Bidding turn | **Pass** | Owner (Q7, Q13). Passing is legal under R-031. The automatic-100 opener needs no decision, because their 100 is placed by R-030 (and see R-035). |
| 2p: choose a musik | Choose the **first musik** in the implementation's deterministic ordering | Owner (Q13). Either choice is legal under R-041. The ordering is only a technical tie-break with no gameplay meaning. |
| 2p: discard 2 cards | Discard the **2 lowest legal cards** (see the ordering below) | Owner (Q13). Any 2 cards are legal under R-041. |
| 3p/4p: give 1 card to each opponent | Give the **lowest legal cards** (see the ordering below), assigned to opponents in seat order to the left | Owner (Q13). Any cards are legal under R-040 / C-06. The assignment order is technical. |
| Declare the final contract | Declare **exactly the winning bid** | Owner (Q13). The minimum legal value under R-042. |
| Bomb | **Never** declared automatically. The flow proceeds as if the player chose not to bomb. | Owner (Q13). The bomb is optional under R-050. |
| Marriage | **Never** declared automatically. The card is played without a declaration. | Owner (Q13). Declaring is optional under R-070. |
| Four-9s redeal | **Never** requested automatically. The flow proceeds as if no redeal was requested. | Owner (Q13). The request is optional under R-022. |
| Trick play | Play the **lowest legal card** (see the ordering below) | Owner (Q7). The legal set comes from the rules (R-061/R-062/R-064, C-12 once verified). |

If a decision point depends on a rule that hasn't been verified yet (for example the bomb point, C-09), the fallback applies at whatever point the verified rule defines. The policy never decides the rule itself.

The UI labels an automatic move neutrally as "Ruch automatyczny (czas minął)", with no rule wording.

## Implementation detail: deterministic "lowest card" ordering

**Not a Tysiąc rule.** It is used only to pick a card deterministically when a fallback needs "the lowest" one.

1. **Primary key: rank strength**, as defined by R-002: 9 < J < Q < K < 10 < A.
2. **Tie-break (technical only): suit** in the fixed order ♠ < ♣ < ♦ < ♥.
   - This order exists solely to make the choice deterministic and testable.
   - It carries no gameplay meaning, is never shown to players, and must not appear in `ruleset.md`.
3. **The candidates are always the legal moves** computed by the engine. The ordering never makes an illegal card eligible.
4. **Musik choice (2p):** the first musik means the one with the lowest index in the engine's internal musik list. This is technical and invisible to players.

**Change from the proposal:** the proposed extra key "non-trump before trump" has been removed. The owner specified rank strength with suit only as a tie-break.

## Consequences

### Positive
- The game never stalls, and every fallback move is legal, deterministic and exactly testable.
- The fallback commits the absent player to the minimum and never grants optional advantages or takes optional risks.
- There is a clear separation between the rules (`ruleset.md`, `game` rules) and the application fallback (`game.timeout`).

### Negative
- Fallback moves can be strategically poor, for example discarding a trump 9 or breaking a marriage. This is intentional: the fallback isn't meant to play well.

### Risks
- **Risk:** players exploit timeouts.
  - **Mitigation:** none needed in the MVP, since this is private play among friends. Revisit if public tables arrive.
- **Risk:** the tie-break ordering is later mistaken for a rule.
  - **Mitigation:** it is documented only here as an implementation detail, and a code comment in `game.timeout` references this ADR.

## Alternatives Considered
1. **Random legal move:** Rejected because it isn't deterministic, is harder to test, and could take optional actions.
2. **Forfeit the match on the first timeout:** Rejected because it was explicitly ruled out by the owner (Q7).
3. **A strategic bot move:** Rejected because bots are a non-goal, and a strategic move would effectively be a game-playing agent.
4. **Non-trump before trump as a primary key (the original proposal):** Rejected by the owner in favour of rank strength with a technical suit tie-break only.
