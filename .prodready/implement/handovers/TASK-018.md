# TASK-018 Handoff

## Task
- Title: Game domain: dealing for 2p/3p and dealer rotation
- Status: Done
- Completed at: 2026-09-29

## Acceptance Criteria Covered
- AC-1: 3-player deal gives 7 cards each and a 3-card musik from the 24-card deck (R-001, R-020)
- AC-2: 2-player deal gives 10 cards each and two musiks of 2 cards (R-021)
- AC-3: the next round's dealer is the player to the left of the previous dealer (R-010)

## Canonical Tests
All in `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/DealTest.java`:
- `AC-1: a 3-player deal gives each player 7 cards and the musik 3 cards from the 24-card deck (R-001, R-020)`: seats 0-2 each hold 7 cards, one musik of 3, and hands plus musik are exactly `Deck.standard()`
- `AC-2: a 2-player deal gives each player 10 cards and two musiks of 2 cards each (R-021)`: seats 0-1 hold 10 cards each, two musiks of 2, all 24 cards accounted for
- `AC-3: the next round's dealer is the player to the left of the previous dealer (R-010)`: 6 consecutive `next()` deals at 3p, each dealer = previous + 1 mod 3, which covers the wrap

Technical tests (no `AC-` prefix):
- the first dealer is drawn with `nextInt(playerCount)` (R-010), for seats 0, 1 and 2
- the same seed gives an equal `Deal`
- the next round is dealt from a fresh full deck
- a `Randomness` whose shuffle is not a permutation of the deck is rejected (`IllegalStateException`)
- a 4p deal throws `IllegalStateException` until TASK-036
- `Seat.left` wraps around, and rejects a seat outside the table

## Files Changed
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/Randomness.java` (new): the domain port for chance: `shuffle(List<Card>)` and `nextInt(bound)`
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/Seat.java` (new): record `(index)`; `left(playerCount)` = `(index + 1) % playerCount` (R-011 seat order)
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/DealLayout.java` (new): record `(handSize, musikSizes)`
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/Deal.java` (new): record `(dealer, hands: Map<Seat, List<Card>>, musiks: List<List<Card>>)`, deep-immutable; `Deal.first(config, randomness)` draws a random dealer, and `deal.next(config, randomness)` passes the deal to the left; both reshuffle the full deck
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/VariantConfig.java`: `dealLayout()`: 2p → 10 + musiks [2, 2], 3p → 7 + musik [3], 4p → `IllegalStateException` (TASK-036)
- `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/DealTest.java` (new)
- `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/SeededRandomness.java` (new): `java.util.Random`-based test double
- `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/TestRuleOptions.java` (new): `allSet()` fixture, moved out of `VariantsTest` so other domain tests can build 2p/3p configs
- `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/VariantsTest.java`: uses `TestRuleOptions.allSet()`
- `.prodready/plan/backlog.md`

## Commands Run
All run with `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home`. The shell default is JDK 21, which cannot run the tests.
- RED: `./mvnw test` (run by the project's post-edit hook): 30 tests; all 11 new `DealTest` tests fail on the `not implemented` stubs, and the 19 existing tests pass
- GREEN: `./mvnw -B spotless:apply`, then `./mvnw -B verify`: BUILD SUCCESS, 30/30 (DealTest 11, VariantsTest 8, CardsTest 5, ModuleBoundariesTest 3, StackAlignmentTest 3)
- `git diff --check`: clean

## Decisions
- **`Randomness` is an interface in `game.domain` with no production implementation yet.** The only implementation is the seeded test double (game-domain.md: "seeded `Randomness` test double"). A production implementation (e.g. `SecureRandom`-backed) belongs to whichever task first starts a real match (the engine TASK-021 or the `match` pipeline). The domain stays deterministic given its input.
- **The deal is validated, not trusted:** a shuffle that is not a permutation of the 24 cards, or a layout that does not use all cards, throws `IllegalStateException`. This keeps card conservation independent of the `Randomness` implementation.
- **Cards are dealt as contiguous slices of the shuffled deck:** hands for seats 0..n-1, then musiks. Physical packet order doesn't matter once the deck is uniformly shuffled.
- **The 4p deal throws instead of guessing.** It depends on C-01 (who sits out) and C-02 (layout), which TASK-036 implements after verification. `RuleOptions.fourPlayerDealLayout` is not read yet.
- **Dealer rotation covers all `playerCount` seats**, including 4p. Whether the 4p dealer also sits out is C-01 and is not decided here.
- **`Deal` is a value, not the `Round`.** `Round`/`MatchState` (game-domain.md) arrive with the engine (TASK-021). They should hold a `Deal` or its parts, and use `Deal.next` for the next round.
- **The four-9s redeal (R-022/C-03) is not handled here.**

## Follow-ups or Blockers
- A production `Randomness` implementation is needed before a real match can be dealt (see Decisions).
- `CardsTest` has two tests with the `AC-2:` prefix (strength order, and no cross-suit comparison). That breaks the "exactly one canonical test per AC" rule from TASK-016. It's a one-line rename of the second test, and it was left untouched because it's out of scope here.

## Next Recommended Task
- TASK-019: Game domain: PlayerView projection and hidden information (unblocked by this task)
