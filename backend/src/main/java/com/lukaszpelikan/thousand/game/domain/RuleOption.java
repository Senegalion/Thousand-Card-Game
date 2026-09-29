package com.lukaszpelikan.thousand.game.domain;

import java.util.Set;
import java.util.function.Function;

/** The {@code CONFIRM} items of {@code ruleset.md}, each with the player counts whose rules depend on it. */
public enum RuleOption {
    SITTING_OUT_POLICY("C-01", RuleOptions::sittingOutPolicy, 4),
    FOUR_PLAYER_DEAL_LAYOUT("C-02", RuleOptions::fourPlayerDealLayout, 4),
    REDEAL_PROCEDURE("C-03", RuleOptions::redealProcedure, 2, 3, 4),
    SITTING_OUT_REDEAL("C-03", RuleOptions::sittingOutRedeal, 4),
    AUCTION_REENTRY("C-04", RuleOptions::auctionReentry, 2, 3, 4),
    UNCHOSEN_MUSIK_VISIBILITY("C-05", RuleOptions::unchosenMusikVisibility, 2),
    FOUR_PLAYER_MUSIK_PROCEDURE("C-06", RuleOptions::fourPlayerMusikProcedure, 4),
    FINAL_CONTRACT_CAP("C-07", RuleOptions::finalContractCap, 2, 3, 4),
    FREE_BOMB_SCOPE("C-08", RuleOptions::freeBombScope, 2, 3, 4),
    BOMB_DECISION_POINT("C-09", RuleOptions::bombDecisionPoint, 2, 3, 4),
    SITTING_OUT_BOMB_BONUS("C-10", RuleOptions::sittingOutBombBonus, 4),
    SITTING_OUT_MUSIK_POINTS_ON_BOMB("C-10", RuleOptions::sittingOutMusikPointsOnBomb, 4),
    BARREL_PLAYERS_GET_BOMB_BONUS("C-11", RuleOptions::barrelPlayersGetBombBonus, 2, 3, 4),
    VOID_SUIT_OBLIGATION("C-12", RuleOptions::voidSuitObligation, 2, 3, 4),
    BEAT_LED_SUIT_AFTER_TRUMPED("C-12", RuleOptions::beatLedSuitAfterTrumped, 2, 3, 4),
    MARRIAGE_ON_FIRST_LEAD("C-13", RuleOptions::marriageOnFirstLead, 2, 3, 4),
    NEGATIVE_SCORES("R-085", RuleOptions::negativeScores, 2, 3, 4),
    SITTING_OUT_GAINS_ON_BARREL("R-103", RuleOptions::sittingOutGainsOnBarrel, 4);

    private final String rulesetId;
    private final Function<RuleOptions, Enum<?>> value;
    private final Set<Integer> requiredFor;

    RuleOption(String rulesetId, Function<RuleOptions, Enum<?>> value, Integer... requiredFor) {
        this.rulesetId = rulesetId;
        this.value = value;
        this.requiredFor = Set.of(requiredFor);
    }

    /** The item's ID in {@code ruleset.md}, where its verification evidence is recorded. */
    public String rulesetId() {
        return rulesetId;
    }

    public boolean isRequiredFor(int playerCount) {
        return requiredFor.contains(playerCount);
    }

    boolean isSetIn(RuleOptions options) {
        return value.apply(options) != null;
    }

    @Override
    public String toString() {
        return rulesetId + " " + name();
    }
}
