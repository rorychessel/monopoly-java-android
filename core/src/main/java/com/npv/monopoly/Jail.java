package com.npv.monopoly;

public class Jail implements Square {

    private final int posX; // vị trí X
    private final int posY; // vị trí Y
    private final JailType type; // loại hình vào tù
    private final String name; // tên

    public Jail(String name, int posX, int posY, JailType type) {
        this.type = type;
        this.name = name;
        this.posX = posX;
        this.posY = posY;
    }

    public String name() {
        return name;
    }

    public boolean isOwned() {
        return false;
    }

    public int positionX() {
        return posX;
    }

    public int positionY() {
        return posY;
    }

    public boolean isOwnable() {
        return false;
    }

    public JailType getType() {
        return type;
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

    public enum JailType {
        VISITING, IN_JAIL, TO_JAIL
    }
}
