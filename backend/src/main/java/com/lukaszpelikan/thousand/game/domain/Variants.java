package com.lukaszpelikan.thousand.game.domain;

/** Builds the {@link VariantConfig} for a player count, failing fast on unverified rules (ADR-006). */
public final class Variants {

    /** The options verified in Kurnik. Setting one is a reviewed change citing its evidence in ruleset.md. */
    public static final RuleOptions VERIFIED = RuleOptions.UNVERIFIED;

    private Variants() {}

    /** The configuration for {@code playerCount} under the {@link #VERIFIED} options. */
    public static VariantConfig forPlayerCount(int playerCount) {
        return forPlayerCount(playerCount, VERIFIED);
    }

    static VariantConfig forPlayerCount(int playerCount, RuleOptions options) {
        return new VariantConfig(playerCount, options);
    }
}
