package com.lukaszpelikan.thousand.game.domain;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/** Hidden-information invariant of {@link PlayerView} over random deals (game-domain.md, jqwik). */
class PlayerViewPropertyTest {

    @Property
    @Label("no hidden card ever appears in another seat's view")
    void noHiddenCardInAnotherSeatsView(
            @ForAll long seed,
            @ForAll @IntRange(min = 2, max = 3) int playerCount,
            @ForAll @IntRange(max = 2) int seat) {
        VariantConfig config = Variants.forPlayerCount(playerCount, TestRuleOptions.allSet());
        Deal deal = Deal.first(config, new SeededRandomness(seed));
        Seat viewer = new Seat(seat % playerCount);

        PlayerView view = PlayerView.project(deal, viewer);

        assertThat(PlayerViewTest.visibleCards(view))
                .containsExactlyInAnyOrderElementsOf(deal.hands().get(viewer))
                .doesNotContainAnyElementsOf(PlayerViewTest.hiddenFrom(deal, viewer));
    }
}
