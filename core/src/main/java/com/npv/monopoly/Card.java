package com.npv.monopoly;

public class Card {
    private CardAction action;
    private int value;
    private int travel = Integer.MAX_VALUE;
    private int travelTo = Integer.MAX_VALUE;
    private boolean nearestRail;
    private int house;
    private int hotel;
    private int eachPlayer;
    private boolean increased;
    private boolean outJailFree;
    private String textA;
    private String textB;
    private String textC;

    public Card(int a) {
        chance(a);
    }

    private void chance(int a) {
        switch (a) {
            case 0:
                BXGiaLam();
                break;
            case 1:
                dividend();
                break;
            case 2:
                dongNgac();
                break;
            case 3:
                loan();
                break;
            case 4:
                jailFree();
                break;
            case 5:
                repairs();
                break;
            case 6:
                poor();
                break;
            case 7:
                hoaBinhPark();
                break;
            case 8:
                kienHung();
                break;
            case 9:
                collectFromPlayers();
                break;
            case 10:
                chairman();
                break;
            case 11:
                back();
                break;
            case 12:
                go();
                break;
            case 13:
                jail();
                break;
            case 14:
                railroad();
                break;
            case 15:
                income();
                break;
            case 16:
                opera();
                break;
            case 17:
                xmas();
                break;
            case 18:
                go();
                break;
            case 19:
                bank();
                break;
            case 20:
                jailFree();
                break;
            case 21:
                hospital();
                break;
            case 22:
                services();
                break;
            case 23:
                jail();
                break;
            case 24:
                school();
                break;
            case 25:
                doctor();
                break;
            case 26:
                stock();
                break;
            case 27:
                life();
                break;
            case 28:
                beauty();
                break;
            default:
                break;
        }
    }

    private void BXGiaLam() {
        action = CardAction.GO_TO_SQUARE;
        textA = "Take a ride on the Gia Lam Bus Station";
        textB = "If you pass go collect $200";
        travelTo = 5;
        increased = false;
    }

    private void dividend() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "Bank pays you dividend of $50";
        value = 50;
    }

    private void dongNgac() {
        action = CardAction.GO_TO_SQUARE;
        textA = "Advance to Dong Ngac, Bac Tu Liem";
        travelTo = 24;
        increased = false;
    }

    private void loan() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "Your building and loan matures";
        textB = "Collect $150";
        value = 150;
    }

    private void jailFree() {
        action = CardAction.GET_OUT_OF_JAIL_FREE;
        textA = "Get out of jail free card";
        outJailFree = true;
    }

    private void repairs() {
        action = CardAction.PAY_BANK;
        textA = "Make general repairs on all your property";
        textB = "Pay $25 per house, $100 per hotel";
        house = -25;
        hotel = -100;
    }

    private void poor() {
        action = CardAction.PAY_BANK;
        textA = "Pay poor tax of $15";
        value = -15;
    }

    private void hoaBinhPark() {
        action = CardAction.GO_TO_SQUARE;
        textA = "Take a walk on the Hoa Binh Park";
        travelTo = 20;
        increased = false;
    }

    private void kienHung() {
        action = CardAction.GO_TO_SQUARE;
        textA = "Advance to Kien Hung, Ha Dong";
        travelTo = 11;
        increased = false;
    }

    private void collectFromPlayers() {
        action = CardAction.COLLECT_FROM_EACH_PLAYER;
        textA = "Collect $50 from each player";
        eachPlayer = 50;
    }

    private void back() {
        action = CardAction.GO_TO_SQUARE;
        textA = "Go back 3 spaces";
        travel = -3;
        increased = false;
    }

    private void chairman() {
        action = CardAction.PAY_EACH_PLAYER;
        textA = "Elected chairman, pay each player $50";
        eachPlayer = -50;
    }



    private void railroad() {
        action = CardAction.GO_TO_SQUARE;
        textA = "Advance to nearest railroad";
        textB = "Pay owner twice the rental";
        nearestRail = true;
        increased = true;
    }

    private void beauty() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "Won second prize in a beauty contest";
        textB = "Collect $10";
        value = 10;
    }

    private void life() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "Life insurance matures";
        textB = "Collect $100";
        value = 100;
    }

    private void stock() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "From sale of stock";
        textB = "You get $45";
        value = 45;
    }

    private void doctor() {
        action = CardAction.PAY_BANK;
        textA = "Doctor's Fee";
        textB = "Pay $50";
        value = -50;
    }

    private void school() {
        action = CardAction.PAY_BANK;
        textA = "Pay School tax of $150";
        value = -150;
    }

    private void jail() {
        action = CardAction.GO_TO_JAIL;
        textA = "Go to Jail";
        travelTo = 8;
    }

    private void services() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "Receive for Services $25";
        value = 25;
    }

    private void hospital() {
        action = CardAction.PAY_BANK;
        textA = "Pay hospital $100";
        value = -100;
    }

    private void bank() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "Bank Error in your favor";
        textB = "Collect $200";
        value = 200;
    }

    private void go() {
        action = CardAction.ADVANCE_TO_GO;
        textA = "Advance to Go";
        textB = "Collect $200";
        travelTo = 0;
    }

    private void xmas() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "Xmas fund matures";
        textB = "Collect $100";
        value = 100;
    }

    private void opera() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "Grand Opera Opening";
        textB = "Collect $50 from bank";
        value = 50;
    }

    private void income() {
        action = CardAction.BANK_PAYS_YOU;
        textA = "Income Tax Refund";
        textB = "Collect $20";
        value = 20;
    }

    public int value() {
        return value;
    }

    public int travel() {
        return travel;
    }

    public int travelTo() {
        return travelTo;
    }

    public boolean travelRail() {
        return nearestRail;
    }

    public int house() {
        return house;
    }

    public int hotel() {
        return hotel;
    }

    public int eachPlayer() {
        return eachPlayer;
    }

    public boolean increased() {
        return increased;
    }

    public boolean outJailFree() {
        return outJailFree;
    }

    public String textA() {
        return textA;
    }

    public String textB() {
        return textB;
    }

    public String textC() {
        return textC;
    }

    public CardAction action() {
        return action;
    }

    // Các loại hành động của thẻ
    public enum CardAction {
        BANK_PAYS_YOU, PAY_BANK, ADVANCE_TO_GO, GO_TO_SQUARE, GO_TO_JAIL,
        PAY_EACH_PLAYER, COLLECT_FROM_EACH_PLAYER, GET_OUT_OF_JAIL_FREE
    }
}
