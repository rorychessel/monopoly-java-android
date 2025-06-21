package com.npv.monopoly;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;

public class ChanceSquare implements Square {
    private final Deck deck;
    private final String name;
    private final int posX;
    private final int posY;

    public ChanceSquare(String name, int posX, int posY, Deck deck) {
        if (deck == null) {
            Gdx.app.error("ChanceSquare", "Deck is null during initialization for square: " + name);
        }
        this.deck = deck;
        this.name = name;
        this.posX = posX;
        this.posY = posY;
    }

    public boolean isOwnable() {
        return false;
    }

    public int positionX() {
        return posX;
    }

    public int positionY() {
        return posY;
    }

    public String name() {
        return name;
    }

    public boolean isOwned() {
        return false;
    }

    public int cost() {
        return 0;
    }

    public void purchase(Player player) {
    }

    public int rent() {
        return 0;
    }

    public Player owner() {
        return null;
    }

    public String toString() {
        return name;
    }

    // Lấy danh sách thẻ
    public Iterable<Card> cards() {
        return deck != null ? deck.cards() : new ArrayList<>();
    }

    // Lật thẻ
    public Card drawCard() {
        if (this.deck == null) {
            Gdx.app.error("ChanceSquare", "Cannot draw card, deck is null at " + name());
            return null;
        }
        Card drawnCard = this.deck.drawCard();
        if (drawnCard == null) {
            Gdx.app.error("ChanceSquare", "Failed to draw card from deck at " + name());
        }
        return drawnCard;
    }
}
