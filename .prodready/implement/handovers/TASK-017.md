# TASK-017 Handoff

## Task
- Title: Game domain: VariantConfig and fail-fast on unverified rules
- Status: Done
- Completed at: 2026-09-29

## Acceptance Criteria Covered
- AC-1: Given a player count with at least one required rule option unset, when `Variants.forPlayerCount` is called, then `UnverifiedRuleException` is thrown listing every unset option
- AC-2: Given a player count whose required options are all set, when `Variants.forPlayerCount` is called, then it returns the configuration for that count

## Canonical Tests
All in `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/VariantsTest.java`:
- `AC-1: throws UnverifiedRuleException listing every unset option required by the player count`: 4p with only the options 3p needs set; `unsetOptions()` is exactly the 7 unset 4p-required options, in `RuleOption` order, and the message names their ruleset IDs
- `AC-2: returns the configuration when every option required by the player count is set`: 3p; the config carries the player count and options

Technical tests (no `AC-` prefix):
- an option not required by the player count may stay unset (C-05 unset: 4p builds, 2p throws listing only C-05)
- no player count is startable until its CONFIRM items are verified (`Variants.forPlayerCount(n)` for n = 2, 3, 4 throws listing every option required for n). **This test must be updated whenever an option is verified in `Variants.VERIFIED`.**
- rejects a player count other than 2, 3 or 4 (`IllegalArgumentException`)

## Files Changed
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/RuleOptions.java` (new): record, one nullable enum field per CONFIRM item, with nested choice enums; `RuleOptions.UNVERIFIED` (all null)
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/RuleOption.java` (new): enum of the CONFIRM items, each with its ruleset ID, accessor and required player counts
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/VariantConfig.java` (new): record `(playerCount, rules)`; the compact constructor validates 2..4 and throws `UnverifiedRuleException` for unset required options
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/UnverifiedRuleException.java` (new): exposes `playerCount()` and `unsetOptions()`
- `backend/src/main/java/com/lukaszpelikan/thousand/game/domain/Variants.java` (new): `VERIFIED` (currently `RuleOptions.UNVERIFIED`), public `forPlayerCount(int)`, package-private `forPlayerCount(int, RuleOptions)` for tests
- `backend/src/test/java/com/lukaszpelikan/thousand/game/domain/VariantsTest.java` (new)
- `.prodready/plan/backlog.md`

## Commands Run
- RED: `./mvnw -B test -Dtest=VariantsTest`: 8 tests run, all fail (`UnsupportedOperationException: not implemented` from the `Variants` stub)
- GREEN: `./mvnw -B spotless:apply`, then `./mvnw -B verify`: BUILD SUCCESS, 19/19 tests (VariantsTest 8, CardsTest 5, ModuleBoundariesTest 3, StackAlignmentTest 3)
- `git diff --check`: clean
- Maven ran with `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home`

## Decisions
- **All 15 CONFIRM items are in `RuleOptions`**, following ADR-006 ("one field per CONFIRM item"). `game-domain.md` instead lists C-01, C-02 and C-06 as `VariantConfig` fields. The ADR is the accepted decision, and one list keeps the fail-fast check in one place.
- **Multi-part items are split** so that no sub-question is silently assumed:
  - C-03 → `redealProcedure` (i/ii, all counts) + `sittingOutRedeal` (iii, 4p)
  - C-10 → `sittingOutBombBonus` + `sittingOutMusikPointsOnBomb`
  - C-12 → `voidSuitObligation` (i/ii) + `beatLedSuitAfterTrumped` (iii)
  - This gives 18 fields.
- **Required player counts come from ruleset.md §14:**
  - all counts: C-03 (i/ii), C-04, C-07, C-08, C-09, C-11, C-12, C-13, R-085
  - 2p only: C-05
  - 4p only: C-01, C-02, C-03 (iii), C-06, C-10, R-103
- **Enum constants are the candidate readings named in ruleset.md, not rules.** If Kurnik behaves differently, add the observed constant when verifying. Yes/no items share `Permission` (ALLOWED/NOT_ALLOWED) or `YesNo`.
- **The fail-fast check lives in `VariantConfig`'s constructor**, so an incomplete config cannot exist at all. `Variants.forPlayerCount` is the entry point.
- **`VariantConfig` holds only `playerCount` and `rules` for now.** The confirmed per-count fields from game-domain.md (deal layout, musik procedure, bomb penalty, sitting-out scoring) are added by the tasks that use them, e.g. deal layout in TASK-018. Nothing speculative was added.
- **Setting a verified option means editing `Variants.VERIFIED`**, with evidence cited in ruleset.md (ADR-006).

## Follow-ups or Blockers
- **No player count is currently startable**, because no CONFIRM item is verified. This is by design (ADR-006). TASK-053's lobby will list no available counts until Kurnik verification sets `Variants.VERIFIED`.
- ADR-006's second ArchUnit rule (no `playerCount` checks in `game` outside `Variants` and the strategies) is not part of this task's ACs and is not implemented yet.

## Next Recommended Task
- TASK-018: Game domain: dealing for 2p/3p and dealer rotation
