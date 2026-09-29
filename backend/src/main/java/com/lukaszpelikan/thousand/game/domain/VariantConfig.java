package com.lukaszpelikan.thousand.game.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The rules for one player count (ADR-006). Obtain it from {@link Variants}. It cannot be created while an option its
 * player count requires is unset.
 */
public record VariantConfig(int playerCount, RuleOptions rules) {

    public VariantConfig {
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("Player count must be 2, 3 or 4: " + playerCount);
        }
        Objects.requireNonNull(rules, "rules");
        List<RuleOption> unset = Arrays.stream(RuleOption.values())
                .filter(option -> option.isRequiredFor(playerCount) && !option.isSetIn(rules))
                .toList();
        if (!unset.isEmpty()) {
            throw new UnverifiedRuleException(playerCount, unset);
        }
    }

    /** How the cards are dealt (R-020, R-021). */
    public DealLayout dealLayout() {
        return switch (playerCount) {
            case 2 -> new DealLayout(10, List.of(2, 2));
            case 3 -> new DealLayout(7, List.of(3));
            default ->
                throw new IllegalStateException(
                        "The 4-player deal depends on C-01 and C-02 and is not implemented yet (TASK-036)");
        };
    }
}
