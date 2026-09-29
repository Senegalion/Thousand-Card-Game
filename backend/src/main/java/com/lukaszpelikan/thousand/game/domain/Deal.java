package com.lukaszpelikan.thousand.game.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** The cards of one round as dealt: the dealer, every player's hand and the musik(s). */
public record Deal(Seat dealer, Map<Seat, List<Card>> hands, List<List<Card>> musiks) {

    public Deal {
        Objects.requireNonNull(dealer, "dealer");
        Map<Seat, List<Card>> handsCopy = new HashMap<>();
        hands.forEach((seat, hand) -> handsCopy.put(seat, List.copyOf(hand)));
        hands = Map.copyOf(handsCopy);
        musiks = musiks.stream().map(List::copyOf).toList();
    }

    /** Deals the first round of a match. The first dealer is random (R-010). */
    public static Deal first(VariantConfig config, Randomness randomness) {
        return deal(config, new Seat(randomness.nextInt(config.playerCount())), randomness);
    }

    /** Deals the next round. The dealer is the player to the left of this round's dealer (R-010). */
    public Deal next(VariantConfig config, Randomness randomness) {
        return deal(config, dealer.left(config.playerCount()), randomness);
    }

    private static Deal deal(VariantConfig config, Seat dealer, Randomness randomness) {
        DealLayout layout = config.dealLayout();
        List<Card> cards = shuffledDeck(randomness);
        int dealt = 0;
        Map<Seat, List<Card>> hands = new HashMap<>();
        for (int seat = 0; seat < config.playerCount(); seat++) {
            hands.put(new Seat(seat), cards.subList(dealt, dealt + layout.handSize()));
            dealt += layout.handSize();
        }
        List<List<Card>> musiks = new ArrayList<>();
        for (int size : layout.musikSizes()) {
            musiks.add(cards.subList(dealt, dealt + size));
            dealt += size;
        }
        if (dealt != cards.size()) {
            throw new IllegalStateException("Deal layout " + layout + " does not use all " + cards.size() + " cards");
        }
        return new Deal(dealer, hands, musiks);
    }

    private static List<Card> shuffledDeck(Randomness randomness) {
        List<Card> deck = Deck.standard();
        List<Card> shuffled = List.copyOf(randomness.shuffle(deck));
        if (shuffled.size() != deck.size() || !new HashSet<>(shuffled).containsAll(deck)) {
            throw new IllegalStateException("Shuffle is not a permutation of the deck: " + shuffled);
        }
        return shuffled;
    }
}
