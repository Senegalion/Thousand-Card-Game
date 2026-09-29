# TASK-019 Handoff

## Task
- Title: Game domain: PlayerView projection and hidden information
- Status: Done
- Completed at: 2026-09-29

## Acceptance Criteria Covered
- AC-1: in a dealt round, a player's view holds only that player's own cards, and never another hand or the musik contents (US-011 AC-4)

## Canonical Tests
- `AC-1: a player's view of a dealt round holds only their own cards, never another hand or the musik (US-011 AC-4)` in `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/PlayerViewTest.java`: for every seat at 2p and 3p (seed 3), `myHand` equals that seat's hand, `revealedMusik` is empty, and no visible card belongs to another hand or a musik

Technical tests (no `AC-` prefix):
- `PlayerViewTest`: the view shows the dealer and every seat's card count; a seat that is not at the table throws `IllegalArgumentException`
- `PlayerViewPropertyTest` (jqwik, 1000 tries): "no hidden card ever appears in another seat's view" over random seeds, player counts 2–3 and seats. The visible cards are exactly the viewer's hand and contain nothing hidden from them. A mutation check (leaking the first musik into `revealedMusik`) made the property fail, and the code was then restored

## Files Changed
- `backend/pom.xml`: `net.jqwik:jqwik` 1.9.3, test scope (`jqwik.version` property; tech-stack.md pins 1.9.x)
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/PlayerView.java` (new): record `(mySeat, dealer, myHand, cardCounts: Map<Seat, Integer>, revealedMusik)`, immutable; `PlayerView.project(Deal, Seat)`
- `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/PlayerViewTest.java` (new): also holds the `visibleCards` / `hiddenFrom` helpers shared with the property test
- `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/PlayerViewPropertyTest.java` (new)
- `.gitignore`: ignore `.jqwik-database` (jqwik's local failure cache)
- `.prodready/plan/backlog.md`

## Commands Run
All run with `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home` (the shell default is JDK 21).
- RED: `./mvnw test` (post-edit hook): the 3 new `PlayerViewTest` tests fail on the `not implemented` stub; the 30 existing tests pass
- GREEN: `./mvnw -B spotless:apply`, then `./mvnw -B verify`: BUILD SUCCESS, 34/34, 0 Checkstyle violations
- Mutation check: `./mvnw -B test -Dtest=PlayerViewPropertyTest` with a leaky `project` → property FAILS as expected; code restored

## Decisions
- **`project` takes a `Deal`, not a `MatchState`.** game-domain.md specifies `project(MatchState, Seat)`, but `MatchState`/`Round` only arrive with the engine in TASK-021, which this task blocks. AC-1 covers a dealt round, which is exactly what a `Deal` holds. TASK-021 should replace or overload this with `project(MatchState, Seat)` and add the remaining `PlayerView` fields from openapi.yaml (matchId, version, phase, scores, bids, contract, trump, trick, legalActions). Presence and deadlines (connected, graceDeadline, turnDeadline) come from `match`, not the domain.
- **`revealedMusik` is always empty for a fresh deal.** A musik is revealed only after the auction (R-040/R-041). The field is there so that later phases fill it rather than change the shape.
- **Other seats are exposed only as card counts,** which matches `seats[].cardCount` in openapi.yaml.
- **The property test class is named `*Test`,** so that Surefire's default includes pick it up. A class named `PlayerViewProperties` is silently skipped.
- **jqwik and Jupiter are in separate classes.** Jupiter-only `PlayerViewTest` keeps the canonical `AC-1:` test in the `@DisplayName` style used elsewhere, and jqwik-only `PlayerViewPropertyTest` holds the property.

## Follow-ups or Blockers
- TASK-021: generalize `project` to `MatchState` (see Decisions).
- Carried over from TASK-018: no production `Randomness` implementation yet; `CardsTest` still has two `AC-2:`-prefixed tests.
- JDK 21 is the shell default. `make test-backend` fails unless `JAVA_HOME` points to JDK 25.

## Next Recommended Task
- TASK-021: Game domain: GameEngine.apply, phases, turn and version handling (unblocked by this task)
