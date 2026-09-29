package com.lukaszpelikan.thousand.game.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Deterministic {@link Randomness} for tests: the same seed gives the same deals. */
final class SeededRandomness implements Randomness {

    private final Random random;

    SeededRandomness(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public List<Card> shuffle(List<Card> cards) {
        List<Card> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled, random);
        return shuffled;
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
