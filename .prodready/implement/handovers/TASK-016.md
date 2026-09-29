# TASK-016 Handoff

## Task
- Title: Game domain: cards, deck, strength, points, marriage values
- Status: Done
- Completed at: 2026-09-29

## Acceptance Criteria Covered
- AC-1: A new deck contains exactly the 24 distinct cards 9-A in four suits (R-001)
- AC-2: Cards of the same suit compare in strength as 9 < J < Q < K < 10 < A (R-002)
- AC-3: All 24 cards are worth 120 points in total (R-003)
- AC-4: Marriage values are ♥ 100, ♦ 80, ♣ 60, ♠ 40 (R-004)

## Canonical Tests
All in `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/CardsTest.java`:
- `AC-1: a new deck contains exactly the 24 distinct cards 9-A in four suits (R-001)`: 24 cards, no duplicates, each suit holds exactly the six ranks
- `AC-2: cards of the same suit compare in strength as 9 < J < Q < K < 10 < A (R-002)`: every ordered pair in every suit, plus a card is not stronger than itself
- `AC-2: strength is not compared across suits`: `IllegalArgumentException`
- `AC-3: all 24 cards are worth 120 points in total (R-003)`: each card's value per rank (A 11, 10 10, K 4, Q 3, J 2, 9 0) and the 120 total
- `AC-4: marriage values are hearts 100, diamonds 80, clubs 60, spades 40 (R-004)`

## Files Changed
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/Suit.java` (new): enum with `marriageValue()`
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/Rank.java` (new): enum declared weakest to strongest, with `points()`
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/Card.java` (new): record `Card(Suit, Rank)` with `points()` and `isStrongerThan(Card)`
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/Deck.java` (new): `Deck.standard()`, an immutable 24-card list in fixed order
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/.gitkeep` (deleted; the package now has classes)
- `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/CardsTest.java` (new)
- `.prodready/plan/backlog.md`

## Commands Run
- Each AC: RED `./mvnw -B test -Dtest=CardsTest` (`UnsupportedOperationException: not implemented` from a stub; for the cross-suit case, an assertion failure), then GREEN
- `./mvnw -B test -Dtest=CardsTest`: 5/5 pass
- `./mvnw -B verify`: first run failed on Spotless formatting in `CardsTest` only; after `spotless:apply`, BUILD SUCCESS, 11/11 tests (CardsTest 5, ModuleBoundariesTest 3, StackAlignmentTest 3)
- ArchUnit `ModuleBoundariesTest` passes with real classes in `game.domain` (AC-1 of TASK-002 is no longer vacuous for `game`)
- `git diff --check`: clean
- In the sandbox, Maven ran with `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home`

## Decisions
- `Rank` is declared from weakest to strongest (9, J, Q, K, 10, A), so its natural enum order is the R-002 strength. The javadoc states this, and AC-2 guards it.
- Strength is exposed as `Card.isStrongerThan(Card)` for cards of the same suit only. A different suit throws `IllegalArgumentException`: comparing across suits depends on the led suit and trump, which is trick resolution (TASK-027/TASK-028).
- Point values live on `Rank`, and `Card.points()` delegates. Marriage values live on `Suit`, following game-domain.md.
- `Deck.standard()` returns one shared immutable list in fixed order (suit, then rank). There is no shuffling or `Randomness`; that is TASK-018.
- R-005 (no other melds) needs no code: no meld type besides the suit marriage value exists.
- Out of scope, as agreed: dealing, VariantConfig, marriage/trump logic, jqwik. `game/api|application|infrastructure` placeholders left unchanged.

## Follow-ups or Blockers
- None.

## Next Recommended Task
- TASK-017: Game domain: VariantConfig and fail-fast on unverified rules
