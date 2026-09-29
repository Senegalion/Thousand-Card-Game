package com.lukaszpelikan.thousand.game.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CardsTest {

    @Test
    @DisplayName("AC-1: a new deck contains exactly the 24 distinct cards 9-A in four suits (R-001)")
    void ac1_R001_deckHas24DistinctCards9ToAceInFourSuits() {
        List<Card> deck = Deck.standard();

        assertThat(deck).hasSize(24).doesNotHaveDuplicates();
        for (Suit suit : Suit.values()) {
            assertThat(deck)
                    .filteredOn(card -> card.suit() == suit)
                    .extracting(Card::rank)
                    .containsExactlyInAnyOrder(Rank.NINE, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.TEN, Rank.ACE);
        }
    }

    @Test
    @DisplayName("AC-2: cards of the same suit compare in strength as 9 < J < Q < K < 10 < A (R-002)")
    void ac2_R002_strengthOrderWithinSuit() {
        List<Rank> weakestToStrongest = List.of(Rank.NINE, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.TEN, Rank.ACE);

        for (Suit suit : Suit.values()) {
            for (int weaker = 0; weaker < weakestToStrongest.size(); weaker++) {
                for (int stronger = weaker + 1; stronger < weakestToStrongest.size(); stronger++) {
                    Card low = new Card(suit, weakestToStrongest.get(weaker));
                    Card high = new Card(suit, weakestToStrongest.get(stronger));

                    assertThat(high.isStrongerThan(low))
                            .as("%s > %s", high, low)
                            .isTrue();
                    assertThat(low.isStrongerThan(high))
                            .as("%s < %s", low, high)
                            .isFalse();
                }
            }
            Card ace = new Card(suit, Rank.ACE);
            assertThat(ace.isStrongerThan(ace)).isFalse();
        }
    }

    @Test
    @DisplayName("AC-2: strength is not compared across suits")
    void ac2_R002_strengthIsNotComparedAcrossSuits() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Card(Suit.HEARTS, Rank.ACE).isStrongerThan(new Card(Suit.SPADES, Rank.NINE)));
    }

    @Test
    @DisplayName("AC-3: all 24 cards are worth 120 points in total (R-003)")
    void ac3_R003_deckIsWorth120Points() {
        Map<Rank, Integer> expectedPoints =
                Map.of(Rank.ACE, 11, Rank.TEN, 10, Rank.KING, 4, Rank.QUEEN, 3, Rank.JACK, 2, Rank.NINE, 0);

        for (Card card : Deck.standard()) {
            assertThat(card.points()).as("%s", card).isEqualTo(expectedPoints.get(card.rank()));
        }
        assertThat(Deck.standard().stream().mapToInt(Card::points).sum()).isEqualTo(120);
    }

    @Test
    @DisplayName("AC-4: marriage values are hearts 100, diamonds 80, clubs 60, spades 40 (R-004)")
    void ac4_R004_marriageValuePerSuit() {
        assertThat(Suit.HEARTS.marriageValue()).isEqualTo(100);
        assertThat(Suit.DIAMONDS.marriageValue()).isEqualTo(80);
        assertThat(Suit.CLUBS.marriageValue()).isEqualTo(60);
        assertThat(Suit.SPADES.marriageValue()).isEqualTo(40);
    }
}
