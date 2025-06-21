package com.npv.monopoly;

import java.util.Objects;

public class Property implements Square {
    private String name;
    private int posX;
    private int posY;
    private int cost;
    private int baseRent;
    private int rent1House;
    private int rent2Houses;
    private int rent3Houses;
    private int rent4Houses;
    private int rentHotel;
    private int houseCost;
    private Player owner;
    private int numHouses;
    private Property[] group;

    public Property(String name, int posX, int posY, int cost, int baseRent, int rent1House, int rent2Houses,
                    int rent3Houses, int rent4Houses, int rentHotel, int houseCost) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.cost = cost;
        this.baseRent = baseRent;
        this.rent1House = rent1House;
        this.rent2Houses = rent2Houses;
        this.rent3Houses = rent3Houses;
        this.rent4Houses = rent4Houses;
        this.rentHotel = rentHotel;
        this.houseCost = houseCost;
        this.owner = null;
        this.numHouses = 0;
        this.group = null;
    }

    public boolean isOwnable() {
        return true;
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
        return owner != null;
    }

    public int cost() {
        return cost;
    }

    public void purchase(Player player) {
        if (player != null && !isOwned()) {
            owner = player;
        }
    }

    public int rent() {
        switch (numHouses) {
            case 0:
                return hasMonopoly() ? baseRent * 2 : baseRent;
            case 1:
                return rent1House;
            case 2:
                return rent2Houses;
            case 3:
                return rent3Houses;
            case 4:
                return rent4Houses;
            case 5:
                return rentHotel;
            default:
                return 0;
        }
    }

    public Player owner() {
        return owner;
    }

    public int houseCost() {
        return houseCost;
    }

    public void build(int houses) {
        if (houses >= 0 && numHouses + houses <= 5) {
            numHouses += houses;
        }
    }

    public int numHouses() {
        return numHouses;
    }

    public void setGroup(Property propA, Property propB) {
        this.group = new Property[2];
        this.group[0] = propA;
        this.group[1] = propB;
    }

    public void setGroup(Property propA, Property propB, Property propC) {
        this.group = new Property[3];
        this.group[0] = propA;
        this.group[1] = propB;
        this.group[2] = propC;
    }

    public boolean groupBuild() {
        if (group == null) return true;
        int minHouses = numHouses;
        for (Property prop : group) {
            if (prop != null && prop != this && prop.owner() == owner) {
                minHouses = Math.min(minHouses, prop.numHouses());
            }
        }
        return numHouses <= minHouses + 1;
    }

    public boolean groupSell() {
        if (group == null) return true;
        int maxHouses = numHouses;
        for (Property prop : group) {
            if (prop != null && prop != this && prop.owner() == owner) {
                maxHouses = Math.max(maxHouses, prop.numHouses());
            }
        }
        return numHouses >= maxHouses - 1;
    }

    public boolean hasMonopoly() {
        if (group == null) return false;
        for (Property prop : group) {
            if (prop != null && (!prop.isOwned() || prop.owner() != owner)) {
                return false;
            }
        }
        return true;
    }

    public Property[] getGroup() {
        return group != null ? group.clone() : new Property[0];
    }
}
