package com.lukaszpelikan.thousand.game.domain;

/**
 * One switch per {@code CONFIRM} item of {@code ruleset.md} (ADR-006). {@code null} means not yet verified in Kurnik;
 * there are no defaults. {@link RuleOption} states which player counts require each switch.
 *
 * <p>The constants of each choice are the readings {@code ruleset.md} names as candidates, not rules. If Kurnik turns
 * out to behave differently, add the observed reading.
 */
public record RuleOptions(
        SittingOutPolicy sittingOutPolicy,
        FourPlayerDealLayout fourPlayerDealLayout,
        RedealProcedure redealProcedure,
        Permission sittingOutRedeal,
        Permission auctionReentry,
        UnchosenMusikVisibility unchosenMusikVisibility,
        FourPlayerMusikProcedure fourPlayerMusikProcedure,
        FinalContractCap finalContractCap,
        FreeBombScope freeBombScope,
        BombDecisionPoint bombDecisionPoint,
        YesNo sittingOutBombBonus,
        YesNo sittingOutMusikPointsOnBomb,
        YesNo barrelPlayersGetBombBonus,
        VoidSuitObligation voidSuitObligation,
        YesNo beatLedSuitAfterTrumped,
        Permission marriageOnFirstLead,
        NegativeScores negativeScores,
        YesNo sittingOutGainsOnBarrel) {

    /** No option verified. */
    public static final RuleOptions UNVERIFIED = new RuleOptions(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

    public enum Permission {
        ALLOWED,
        NOT_ALLOWED
    }

    public enum YesNo {
        YES,
        NO
    }

    /** C-01: who sits out in a 4-player round. */
    public enum SittingOutPolicy {
        DEALER,
        NOT_DEALER
    }

    /** C-02. */
    public enum FourPlayerDealLayout {
        SEVEN_EACH_AND_MUSIK_OF_THREE
    }

    /** C-03 (i) and (ii): when a four-9s redeal may be requested and who redeals. */
    public enum RedealProcedure {
        BEFORE_BIDDING_SAME_DEALER,
        BEFORE_BIDDING_NEXT_DEALER
    }

    /** C-05. */
    public enum UnchosenMusikVisibility {
        HIDDEN,
        REVEALED_AFTER_ROUND
    }

    /** C-06. */
    public enum FourPlayerMusikProcedure {
        REVEAL_TAKE_GIVE_ONE_EACH
    }

    /** C-07. */
    public enum FinalContractCap {
        SAME_AS_BID_CAP_WITH_MUSIK,
        NONE
    }

    /** C-08: whether the free first bomb is counted per player or once per game. */
    public enum FreeBombScope {
        PER_PLAYER,
        PER_GAME
    }

    /** C-09: when, after taking the musik and before the first lead, the declarer may throw a bomb. */
    public enum BombDecisionPoint {
        AFTER_TAKING_MUSIK,
        AFTER_PASSING_CARDS,
        EITHER
    }

    /** C-12 (i) and (ii): what a player void in the led suit must play. */
    public enum VoidSuitObligation {
        ANY_CARD,
        MUST_TRUMP,
        MUST_TRUMP_AND_OVERTRUMP
    }

    /** R-085. */
    public enum NegativeScores {
        ALLOWED,
        FLOOR_AT_ZERO
    }
}
