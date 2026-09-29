package com.lukaszpelikan.thousand.game.domain;

import java.util.Objects;

/** A playing card. */
public record Card(Suit suit, Rank rank) {

    public Card {
        Objects.requireNonNull(suit, "suit");
        Objects.requireNonNull(rank, "rank");
    }

    /** Card point value (R-003). */
    public int points() {
        return rank.points();
    }

    /**
     * Compares strength with another card of the same suit (R-002). Comparing different suits depends on the led suit
     * and trump, which is trick resolution, not a property of the cards.
     */
    public boolean isStrongerThan(Card other) {
        if (other.suit != suit) {
            throw new IllegalArgumentException("Cannot compare strength of " + this + " and " + other);
        }
        return rank.compareTo(other.rank) > 0;
    }
}
