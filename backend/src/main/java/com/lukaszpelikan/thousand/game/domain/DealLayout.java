package com.lukaszpelikan.thousand.game.domain;

import java.util.List;

/** How the 24 cards are dealt: {@code handSize} cards to each player and the rest into musiks of the given sizes. */
public record DealLayout(int handSize, List<Integer> musikSizes) {

    public DealLayout {
        musikSizes = List.copyOf(musikSizes);
    }
}
