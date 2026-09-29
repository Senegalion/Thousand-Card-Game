package com.lukaszpelikan.thousand.game.domain;

/** The four suits, each carrying its marriage value (R-004). */
public enum Suit {
    HEARTS(100),
    DIAMONDS(80),
    CLUBS(60),
    SPADES(40);

    private final int marriageValue;

    Suit(int marriageValue) {
        this.marriageValue = marriageValue;
    }

    /** Value of this suit's marriage (meldunek), a Q and K of the suit (R-004). */
    public int marriageValue() {
        return marriageValue;
    }
}
