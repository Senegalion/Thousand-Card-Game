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
}
