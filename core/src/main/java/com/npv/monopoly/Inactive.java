package com.npv.monopoly;

public class Inactive implements Square {
    private final int posX; // vị trí X
    private final int posY; // vị trí Y
    private final String name;

    public Inactive(String name, int posX, int posY) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
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

    public boolean isOwnable() {
        return false;
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
}
