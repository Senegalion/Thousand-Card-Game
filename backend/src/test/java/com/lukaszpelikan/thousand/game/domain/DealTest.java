package com.lukaszpelikan.thousand.game.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DealTest {

    private static final VariantConfig TWO_PLAYERS = Variants.forPlayerCount(2, TestRuleOptions.allSet());
    private static final VariantConfig THREE_PLAYERS = Variants.forPlayerCount(3, TestRuleOptions.allSet());

    @Test
    @DisplayName(
            "AC-1: a 3-player deal gives each player 7 cards and the musik 3 cards from the 24-card deck (R-001, R-020)")
    void ac1_R020_threePlayerDeal() {
        Deal deal = Deal.first(THREE_PLAYERS, new SeededRandomness(1));

        assertThat(deal.hands()).containsOnlyKeys(new Seat(0), new Seat(1), new Seat(2));
        assertThat(deal.hands().values()).allSatisfy(hand -> assertThat(hand).hasSize(7));
        assertThat(deal.musiks())
                .singleElement()
                .satisfies(musik -> assertThat(musik).hasSize(3));
        assertThat(allCards(deal)).containsExactlyInAnyOrderElementsOf(Deck.standard());
    }

    @Test
    @DisplayName("AC-2: a 2-player deal gives each player 10 cards and two musiks of 2 cards each (R-021)")
    void ac2_R021_twoPlayerDeal() {
        Deal deal = Deal.first(TWO_PLAYERS, new SeededRandomness(1));

        assertThat(deal.hands()).containsOnlyKeys(new Seat(0), new Seat(1));
        assertThat(deal.hands().values()).allSatisfy(hand -> assertThat(hand).hasSize(10));
        assertThat(deal.musiks())
                .hasSize(2)
                .allSatisfy(musik -> assertThat(musik).hasSize(2));
        assertThat(allCards(deal)).containsExactlyInAnyOrderElementsOf(Deck.standard());
    }

    @Test
    @DisplayName("AC-3: the next round's dealer is the player to the left of the previous dealer (R-010)")
    void ac3_R010_dealerRotatesToTheLeft() {
        Randomness randomness = new SeededRandomness(7);
        Deal deal = Deal.first(THREE_PLAYERS, randomness);

        for (int round = 0; round < 6; round++) {
            Deal next = deal.next(THREE_PLAYERS, randomness);

            assertThat(next.dealer().index()).isEqualTo((deal.dealer().index() + 1) % 3);
            deal = next;
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("the first dealer is drawn from the randomness among all seats (R-010)")
    void R010_firstDealerIsRandom(int drawn) {
        Randomness drawing = new FixedDraw(drawn, 3);

        assertThat(Deal.first(THREE_PLAYERS, drawing).dealer()).isEqualTo(new Seat(drawn));
    }

    @Test
    @DisplayName("the same seed gives the same deal")
    void sameSeedGivesSameDeal() {
        assertThat(Deal.first(THREE_PLAYERS, new SeededRandomness(42)))
                .isEqualTo(Deal.first(THREE_PLAYERS, new SeededRandomness(42)));
    }

    @Test
    @DisplayName("each round is dealt from a fresh shuffle of the full deck")
    void nextRoundIsDealtFromTheFullDeck() {
        Randomness randomness = new SeededRandomness(3);
        Deal next = Deal.first(TWO_PLAYERS, randomness).next(TWO_PLAYERS, randomness);

        assertThat(allCards(next)).containsExactlyInAnyOrderElementsOf(Deck.standard());
    }

    @Test
    @DisplayName("rejects a shuffle that is not a permutation of the deck")
    void rejectsShuffleThatIsNotAPermutation() {
        Randomness broken = new Randomness() {
            @Override
            public List<Card> shuffle(List<Card> cards) {
                List<Card> duplicated = new ArrayList<>(cards);
                duplicated.set(0, duplicated.get(1));
                return duplicated;
            }

            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };

        assertThatIllegalStateException().isThrownBy(() -> Deal.first(THREE_PLAYERS, broken));
    }

    @Test
    @DisplayName("the 4-player deal is not available until C-01 and C-02 are implemented (TASK-036)")
    void fourPlayerDealIsNotYetAvailable() {
        VariantConfig fourPlayers = Variants.forPlayerCount(4, TestRuleOptions.allSet());

        assertThatIllegalStateException().isThrownBy(() -> Deal.first(fourPlayers, new SeededRandomness(1)));
    }

    @Test
    @DisplayName("the seat to the left wraps around the table")
    void seatToTheLeftWrapsAround() {
        assertThat(new Seat(0).left(2)).isEqualTo(new Seat(1));
        assertThat(new Seat(1).left(2)).isEqualTo(new Seat(0));
        assertThat(new Seat(2).left(3)).isEqualTo(new Seat(0));
        assertThatIllegalArgumentException().isThrownBy(() -> new Seat(3).left(3));
    }

    private static List<Card> allCards(Deal deal) {
        return Stream.concat(
                        deal.hands().values().stream().flatMap(List::stream),
                        deal.musiks().stream().flatMap(List::stream))
                .toList();
    }

    /** Draws a fixed first dealer and does not shuffle. */
    private record FixedDraw(int drawn, int expectedBound) implements Randomness {

        @Override
        public List<Card> shuffle(List<Card> cards) {
            return List.copyOf(cards);
        }

        @Override
        public int nextInt(int bound) {
            assertThat(bound).isEqualTo(expectedBound);
            return drawn;
        }
    }
}
