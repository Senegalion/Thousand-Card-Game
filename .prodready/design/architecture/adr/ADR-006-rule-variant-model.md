# ADR-006: Game Rule Variant Model

## Status
Accepted

## Date
2026-09-28

## Context
The requirements are:
- The MVP must support 2, 3 and 4 players, and their rule differences must be explicit and enforced (Q1).
- Future variants must be addable without an engine rewrite.
- No rule may be silently assumed. Fifteen ruleset items (C-01..C-13, R-085, R-103) are pending Kurnik verification.
- The game domain must be independent of all infrastructure.

## Decision
The rules are modelled as **one engine core plus an immutable `VariantConfig`** (see `game-domain.md`):
- **Core:** cards, the auction, trick resolution, marriages and trump, scoring, the barrel and end-of-game. These implement the `KURNIK` and `DERIVED — APPROVED` rules.
- **`VariantConfig`:** holds the player-count differences (deal layout, musik procedure, sitting-out policy and scoring, bomb penalty) and a `RuleOptions` record with **one field per CONFIRM item**, typed as a nullable enum.
- **Fail-fast:** `Variants.forPlayerCount(n)` throws `UnverifiedRuleException` if any option required by that player count is unset.
  - The lobby exposes only the player counts whose configuration is complete.
  - Setting an option is a reviewed code change that must cite its verification evidence in `ruleset.md`.
- **Strategies, not flags scattered through the code:** behaviour that differs by count, such as the musik procedure and sitting-out scoring, is implemented as a small strategy interface selected by the config. It is not implemented as `if (playerCount == 4)` checks throughout the engine.
- **Architecture test:** an ArchUnit rule forbids `game` from importing anything outside the JDK. A second rule forbids checking `playerCount` in `game` outside `Variants` and the strategies.

## Consequences

### Positive
- "No silent defaults" becomes a property the compiler and the tests enforce, not a convention.
- New variants, such as house rules, can be added purely by configuration and strategies.
- Every rule difference is visible in one place, which mirrors section 12 of `ruleset.md`.

### Negative
- A match **cannot start** for any player count until the CONFIRM items that apply to it are verified. Several items (C-04, C-07, C-08, C-09, C-11, C-12, C-13, R-085) affect **all** counts, so Kurnik verification is on the critical path of the first playable match.
- Slightly more upfront structure than hard-coding a single variant.

### Risks
- **Risk:** Kurnik verification stalls implementation.
  - **Mitigation:**
    - Plan the verification as an explicit early task, with an evidence template recording screenshots and observed behaviour.
    - Engine work on the rules that are already approved proceeds in parallel, because they don't depend on the unset options.

## Alternatives Considered
1. **Separate engine per player count (`TwoPlayerGame`, `ThreePlayerGame`, `FourPlayerGame`):** Rejected because it triplicates the shared rules and makes divergence bugs likely. It also contradicts "no engine rewrite for new variants".
2. **Rules engine / DSL (for example Drools or a rules table):** Rejected because it is heavy infrastructure for about 60 well-defined rules, harder to test and read, and conflicts with the goal of a simple, pure domain.
3. **Sensible defaults for CONFIRM items until verified:** Rejected because the owner explicitly forbade silent assumptions (Q3, Q4).
