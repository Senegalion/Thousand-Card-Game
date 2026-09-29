package com.lukaszpelikan.thousand.game.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

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
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The values below are arbitrary test data, not verified rules. Only {@link Variants#VERIFIED} holds verified values.
 */
class VariantsTest {

    @Test
    @DisplayName("AC-1: throws UnverifiedRuleException listing every unset option required by the player count")
    void ac1_throwsListingEveryUnsetRequiredOption() {
        RuleOptions threePlayerOptionsOnly = new RuleOptions(
                null,
                null,
                RedealProcedure.BEFORE_BIDDING_SAME_DEALER,
                null,
                Permission.NOT_ALLOWED,
                UnchosenMusikVisibility.HIDDEN,
                null,
                FinalContractCap.SAME_AS_BID_CAP_WITH_MUSIK,
                FreeBombScope.PER_PLAYER,
                BombDecisionPoint.AFTER_TAKING_MUSIK,
                null,
                null,
                YesNo.NO,
                VoidSuitObligation.MUST_TRUMP_AND_OVERTRUMP,
                YesNo.YES,
                Permission.ALLOWED,
                NegativeScores.ALLOWED,
                null);

        UnverifiedRuleException exception = catchThrowableOfType(
                UnverifiedRuleException.class, () -> Variants.forPlayerCount(4, threePlayerOptionsOnly));

        assertThat(exception.playerCount()).isEqualTo(4);
        assertThat(exception.unsetOptions())
                .containsExactly(
                        RuleOption.SITTING_OUT_POLICY,
                        RuleOption.FOUR_PLAYER_DEAL_LAYOUT,
                        RuleOption.SITTING_OUT_REDEAL,
                        RuleOption.FOUR_PLAYER_MUSIK_PROCEDURE,
                        RuleOption.SITTING_OUT_BOMB_BONUS,
                        RuleOption.SITTING_OUT_MUSIK_POINTS_ON_BOMB,
                        RuleOption.SITTING_OUT_GAINS_ON_BARREL);
        assertThat(exception).hasMessageContaining("C-01").hasMessageContaining("R-103");
    }

    @Test
    @DisplayName("AC-2: returns the configuration when every option required by the player count is set")
    void ac2_returnsConfigurationWhenRequiredOptionsAreSet() {
        RuleOptions options = TestRuleOptions.allSet();

        VariantConfig config = Variants.forPlayerCount(3, options);

        assertThat(config.playerCount()).isEqualTo(3);
        assertThat(config.rules()).isEqualTo(options);
    }

    @Test
    @DisplayName("an option not required by the player count may stay unset")
    void optionNotRequiredByPlayerCountMayStayUnset() {
        RuleOptions withoutTwoPlayerOnlyOption = new RuleOptions(
                SittingOutPolicy.DEALER,
                FourPlayerDealLayout.SEVEN_EACH_AND_MUSIK_OF_THREE,
                RedealProcedure.BEFORE_BIDDING_SAME_DEALER,
                Permission.NOT_ALLOWED,
                Permission.NOT_ALLOWED,
                null,
                FourPlayerMusikProcedure.REVEAL_TAKE_GIVE_ONE_EACH,
                FinalContractCap.NONE,
                FreeBombScope.PER_GAME,
                BombDecisionPoint.AFTER_PASSING_CARDS,
                YesNo.YES,
                YesNo.YES,
                YesNo.YES,
                VoidSuitObligation.ANY_CARD,
                YesNo.NO,
                Permission.NOT_ALLOWED,
                NegativeScores.FLOOR_AT_ZERO,
                YesNo.NO);

        assertThat(Variants.forPlayerCount(4, withoutTwoPlayerOnlyOption).playerCount())
                .isEqualTo(4);
        assertThat(catchThrowableOfType(
                                UnverifiedRuleException.class,
                                () -> Variants.forPlayerCount(2, withoutTwoPlayerOnlyOption))
                        .unsetOptions())
                .containsExactly(RuleOption.UNCHOSEN_MUSIK_VISIBILITY);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4})
    @DisplayName("no player count is startable until its CONFIRM items are verified in Kurnik")
    void verifiedOptionsDoNotYetCoverAnyPlayerCount(int playerCount) {
        UnverifiedRuleException exception =
                catchThrowableOfType(UnverifiedRuleException.class, () -> Variants.forPlayerCount(playerCount));

        assertThat(exception.unsetOptions())
                .containsExactlyElementsOf(Arrays.stream(RuleOption.values())
                        .filter(option -> option.isRequiredFor(playerCount))
                        .toList());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    @DisplayName("rejects a player count other than 2, 3 or 4")
    void rejectsUnsupportedPlayerCount(int playerCount) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Variants.forPlayerCount(playerCount, TestRuleOptions.allSet()));
    }
}
