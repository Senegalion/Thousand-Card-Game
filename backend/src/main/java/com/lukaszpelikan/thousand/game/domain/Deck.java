package com.lukaszpelikan.thousand.game.domain;

import java.util.ArrayList;
import java.util.List;

/** The Tysiąc deck: 24 cards, 9 to A in four suits (R-001). */
public final class Deck {

    private static final List<Card> STANDARD = createStandard();

    private Deck() {}

    /** Returns the full deck in a fixed order. Shuffling is not the deck's concern. */
    public static List<Card> standard() {
        return STANDARD;
    }

    private static List<Card> createStandard() {
        List<Card> cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        return List.copyOf(cards);
    }
}
