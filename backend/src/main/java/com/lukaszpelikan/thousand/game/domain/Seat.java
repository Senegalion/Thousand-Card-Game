package com.lukaszpelikan.thousand.game.domain;

/** A seat at the table, numbered from 0 in the order play goes to the left (R-011). */
public record Seat(int index) {

    public Seat {
        if (index < 0) {
            throw new IllegalArgumentException("Seat index must not be negative: " + index);
        }
    }

    /** The seat to the left of this one at a table of {@code playerCount}. */
    public Seat left(int playerCount) {
        if (index >= playerCount) {
            throw new IllegalArgumentException("Seat " + index + " is not at a table of " + playerCount);
        }
        return new Seat((index + 1) % playerCount);
    }
}
