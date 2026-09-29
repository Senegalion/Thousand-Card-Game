package com.lukaszpelikan.thousand.game.domain;

import com.lukaszpelikan.thousand.game.domain.RuleOptions.BombDecisionPoint;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.FinalContractCap;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.FourPlayerDealLayout;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.FourPlayerMusikProcedure;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.FreeBombScope;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.NegativeScores;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.Permission;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.RedealProcedure;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.SittingOutPolicy;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.UnchosenMusikVisibility;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.VoidSuitObligation;
import com.lukaszpelikan.thousand.game.domain.RuleOptions.YesNo;

/** Rule options for tests of rules that do not depend on a {@code CONFIRM} item. */
final class TestRuleOptions {

    private TestRuleOptions() {}

    /** Every option set. Arbitrary test data, not verified rules. */
    static RuleOptions allSet() {
        return new RuleOptions(
                SittingOutPolicy.DEALER,
                FourPlayerDealLayout.SEVEN_EACH_AND_MUSIK_OF_THREE,
                RedealProcedure.BEFORE_BIDDING_NEXT_DEALER,
                Permission.ALLOWED,
                Permission.ALLOWED,
                UnchosenMusikVisibility.REVEALED_AFTER_ROUND,
                FourPlayerMusikProcedure.REVEAL_TAKE_GIVE_ONE_EACH,
                FinalContractCap.NONE,
                FreeBombScope.PER_GAME,
                BombDecisionPoint.EITHER,
                YesNo.NO,
                YesNo.NO,
                YesNo.YES,
                VoidSuitObligation.MUST_TRUMP,
                YesNo.NO,
                Permission.ALLOWED,
                NegativeScores.ALLOWED,
                YesNo.YES);
    }
}
