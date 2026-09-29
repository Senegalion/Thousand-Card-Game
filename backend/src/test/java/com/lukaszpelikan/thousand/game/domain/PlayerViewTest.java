package com.lukaszpelikan.thousand.game.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerViewTest {

    private static final VariantConfig TWO_PLAYERS = Variants.forPlayerCount(2, TestRuleOptions.allSet());
    private static final VariantConfig THREE_PLAYERS = Variants.forPlayerCount(3, TestRuleOptions.allSet());

    @Test
    @DisplayName(
            "AC-1: a player's view of a dealt round holds only their own cards, never another hand or the musik (US-011 AC-4)")
    void ac1_viewHoldsOnlyOwnCards() {
        for (VariantConfig config : List.of(TWO_PLAYERS, THREE_PLAYERS)) {
            Deal deal = Deal.first(config, new SeededRandomness(3));

            for (Seat seat : deal.hands().keySet()) {
                PlayerView view = PlayerView.project(deal, seat);

                assertThat(view.myHand())
                        .containsExactlyInAnyOrderElementsOf(deal.hands().get(seat));
                assertThat(view.revealedMusik()).isEmpty();
                assertThat(visibleCards(view)).doesNotContainAnyElementsOf(hiddenFrom(deal, seat));
            }
        }
    }

    @Test
    @DisplayName("the view shows the dealer and how many cards every seat holds")
    void viewShowsPublicInformation() {
        Deal deal = Deal.first(THREE_PLAYERS, new SeededRandomness(5));

        PlayerView view = PlayerView.project(deal, new Seat(1));

        assertThat(view.mySeat()).isEqualTo(new Seat(1));
        assertThat(view.dealer()).isEqualTo(deal.dealer());
        assertThat(view.cardCounts()).isEqualTo(Map.of(new Seat(0), 7, new Seat(1), 7, new Seat(2), 7));
    }

    @Test
    @DisplayName("a seat that is not at the table has no view")
    void seatNotAtTheTableIsRejected() {
        Deal deal = Deal.first(TWO_PLAYERS, new SeededRandomness(5));

        assertThatIllegalArgumentException().isThrownBy(() -> PlayerView.project(deal, new Seat(2)));
    }

    /** Every card a view shows. */
    static List<Card> visibleCards(PlayerView view) {
        List<Card> visible = new ArrayList<>(view.myHand());
        visible.addAll(view.revealedMusik());
        return visible;
    }

    /** Every card {@code seat} must not see in a freshly dealt round: other hands and all musiks. */
    static List<Card> hiddenFrom(Deal deal, Seat seat) {
        List<Card> hidden = new ArrayList<>();
        deal.hands().forEach((other, hand) -> {
            if (!other.equals(seat)) {
                hidden.addAll(hand);
            }
        });
        deal.musiks().forEach(hidden::addAll);
        return hidden;
    }
}
