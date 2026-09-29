package com.lukaszpelikan.thousand.game.domain;

import java.util.List;

/** The only source of chance in the game: shuffling and the first dealer (R-010). Tests pass a seeded one. */
public interface Randomness {

    /** Returns a new list holding a random permutation of {@code cards}. */
    List<Card> shuffle(List<Card> cards);

    /** Returns a random integer from 0 (inclusive) to {@code bound} (exclusive). */
    int nextInt(int bound);
}
