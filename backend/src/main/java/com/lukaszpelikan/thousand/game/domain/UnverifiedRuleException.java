package com.lukaszpelikan.thousand.game.domain;

import java.util.List;

/** A player count cannot be played because rule options it requires are not verified yet (ADR-006). */
public class UnverifiedRuleException extends RuntimeException {

    private final int playerCount;
    private final List<RuleOption> unsetOptions;

    public UnverifiedRuleException(int playerCount, List<RuleOption> unsetOptions) {
        super("Unverified rule options for " + playerCount + " players: " + unsetOptions);
        this.playerCount = playerCount;
        this.unsetOptions = List.copyOf(unsetOptions);
    }

    public int playerCount() {
        return playerCount;
    }

    /** Every unset option the player count requires, in {@link RuleOption} order. */
    public List<RuleOption> unsetOptions() {
        return unsetOptions;
    }
}
