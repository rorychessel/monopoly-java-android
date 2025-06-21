package com.npv.monopoly;

import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class RandomDeck implements Deck {
    private final ArrayList<Card> deck;
    private int SIZE;
    private int current;
    private boolean outOfJailFree;

    public RandomDeck() {
        deck = new ArrayList<>();
        outOfJailFree = true;
    }

    public void initialize(Card[] cards) {
        if (!deck.isEmpty()) {
            return;
        }
        if (cards == null || cards.length == 0) {
            Gdx.app.error("RandomDeck", "Cannot initialize deck with null or empty card array");
            return;
        }
        SIZE = cards.length;
        deck.addAll(Arrays.asList(cards));
        for (Card card : deck) {
            if (card == null || card.action() == null) {
                Gdx.app.error("RandomDeck", "Found null card or card with null action during initialization");
            }
        }
        Collections.shuffle(deck);
        current = 0;
    }

    public Card drawCard() {
        if (deck.isEmpty()) {
            Gdx.app.error("RandomDeck", "Cannot draw card: Deck is empty");
            return null;
        }
        if (current >= SIZE) {
            Collections.shuffle(deck);
            current = 0;
        }
        Card card = deck.get(current);
        if (card == null || card.action() == null) {
            Gdx.app.error("RandomDeck", "Drawn card is null or has null action at index " + current);
            current++;
            return drawCard();
        }
        current++;
        if (card.outJailFree() && outOfJailFree) {
            outOfJailFree = false;
        } else if (card.outJailFree()) {
            return drawCard();
        }
        return card;
    }

    public void returnOutOfJail() {
        outOfJailFree = true;
    }

    public Iterable<Card> cards() {
        return new ArrayList<>(deck);
    }
}
