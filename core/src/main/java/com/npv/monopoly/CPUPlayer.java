package com.npv.monopoly;

import java.util.ArrayList;
import java.util.List;

public class CPUPlayer implements Player {
    private final String playerName;
    private int money;
    private int currentBoardIndex;
    private List<Square> properties;
    private boolean isInJailState;
    private int jailTurnsRemaining;
    private int numGetOutOfJailFreeCards;
    private int doubleRollsThisTurn;

    public CPUPlayer(String name, int initialMoney) {
        this.money = initialMoney;
        this.properties = new ArrayList<>();
        this.currentBoardIndex = 0;
        this.playerName = "AI";
        this.isInJailState = false;
        this.jailTurnsRemaining = 0;
        this.numGetOutOfJailFreeCards = 0;
        this.doubleRollsThisTurn = 0;
    }

    @Override
    public String name() {
        return playerName;
    }

    @Override
    public int getMoney() {
        return money;
    }

    @Override
    public void changeMoney(int amount) {
        this.money += amount;
    }

    @Override
    public int getCurrentBoardIndex() {
        return currentBoardIndex;
    }

    @Override
    public void moveBySteps(int numSpaces, int boardSize) {
        this.currentBoardIndex = (this.currentBoardIndex + numSpaces) % boardSize;
    }

    @Override
    public void moveToBoardIndex(int boardIndex) {
        this.currentBoardIndex = boardIndex;
        if (this.currentBoardIndex < 0 || this.currentBoardIndex >= 40) {
            System.err.println("Cảnh báo: Vị trí CPU không hợp lệ sau moveToBoardIndex: " + this.currentBoardIndex);
            this.currentBoardIndex = this.currentBoardIndex % 40;
            if (this.currentBoardIndex < 0) this.currentBoardIndex += 40;
        }
    }

    @Override
    public void addProperty(Square square) {
        if (!square.isOwnable()) {
            return;
        }
        if (!properties.contains(square)) {
            properties.add(square);
        }
    }

    @Override
    public void removeProperty(Square square) {
        properties.remove(square);
    }

    @Override
    public Iterable<Square> getProperties() {
        return new ArrayList<>(properties);
    }

    @Override
    public void goToJail() {
        this.isInJailState = true;
        this.jailTurnsRemaining = 3;
    }

    @Override
    public boolean isInJail() {
        return this.isInJailState;
    }

    @Override
    public boolean decrementJailTurnsAndCheckStay() {
        if (!this.isInJailState) {
            return false;
        }
        this.jailTurnsRemaining--;
        if (this.jailTurnsRemaining <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public void leaveJail() {
        this.isInJailState = false;
        this.jailTurnsRemaining = 0;
    }

    @Override
    public void addGetOutOfJailFreeCard() {
        this.numGetOutOfJailFreeCards++;
    }

    @Override
    public boolean useGetOutOfJailFreeCard() {
        if (this.numGetOutOfJailFreeCards > 0) {
            this.numGetOutOfJailFreeCards--;
            return true;
        }
        return false;
    }

    @Override
    public int getNumGetOutOfJailFreeCards() {
        return this.numGetOutOfJailFreeCards;
    }

    @Override
    public int getTotalAssetsValue() {
        int assets = this.money;
        for (Square sq : properties) {
            assets += sq.cost();
            if (sq instanceof Property) {
                Property prop = (Property) sq;
                if (prop.numHouses() > 0 && prop.numHouses() <= 4) {
                    assets += prop.numHouses() * prop.houseCost();
                } else if (prop.numHouses() == 5) {
                    assets += 5 * prop.houseCost();
                }
            }
        }
        return assets;
    }

    @Override
    public int getDoubleRollsThisTurn() {
        return this.doubleRollsThisTurn;
    }

    @Override
    public void incrementDoubleRollsThisTurn() {
        this.doubleRollsThisTurn++;
    }

    @Override
    public void resetDoubleRollsThisTurn() {
        this.doubleRollsThisTurn = 0;
    }

    public boolean decideToBuyProperty(Square propertyToBuy) {
        // Luôn mua nếu có đủ tiền
        if (propertyToBuy instanceof Property || propertyToBuy instanceof Railroad) { // Thêm Utility nếu có
            return this.money >= propertyToBuy.cost();
        }
        return false;
    }

    public boolean decideToPayToLeaveJail() {
        // Luôn trả tiền để ra tù nếu có đủ tiền và còn phải ở tù
        int bailAmount = 50;
        return this.isInJailState && this.money >= bailAmount;
    }

    public boolean decideToUseJailFreeCard() {
        // Luôn dùng thẻ nếu có
        return this.isInJailState && this.numGetOutOfJailFreeCards > 0;
    }

    @Override
    public int getJailTurnsRemaining() {
        return jailTurnsRemaining;
    }
}
