package com.lukaszpelikan.thousand.game.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * What one seat may see of the game. It holds that seat's own hand and public information, and never
 * another hand or an unrevealed musik (hidden information).
 */
public record PlayerView(
        Seat mySeat, Seat dealer, List<Card> myHand, Map<Seat, Integer> cardCounts, List<Card> revealedMusik) {

    public PlayerView {
        Objects.requireNonNull(mySeat, "mySeat");
        Objects.requireNonNull(dealer, "dealer");
        myHand = List.copyOf(myHand);
        cardCounts = Map.copyOf(cardCounts);
        revealedMusik = List.copyOf(revealedMusik);
    }

    /** The view of a freshly dealt round for {@code seat}. No musik is revealed before the auction ends. */
    public static PlayerView project(Deal deal, Seat seat) {
        List<Card> hand = deal.hands().get(seat);
        if (hand == null) {
            throw new IllegalArgumentException("Seat " + seat.index() + " is not at this table");
        }
        Map<Seat, Integer> cardCounts = new HashMap<>();
        deal.hands().forEach((other, cards) -> cardCounts.put(other, cards.size()));
        return new PlayerView(seat, deal.dealer(), hand, cardCounts, List.of());
    }
}
