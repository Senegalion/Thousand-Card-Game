package com.lukaszpelikan.thousand.game.domain;

/** Card ranks 9 to A (R-001), declared from weakest to strongest (R-002): 9, J, Q, K, 10, A. */
public enum Rank {
    NINE(0),
    JACK(2),
    QUEEN(3),
    KING(4),
    TEN(10),
    ACE(11);

    private final int points;

    Rank(int points) {
        this.points = points;
    }

    /** Card point value (R-003). */
    public int points() {
        return points;
    }
}
