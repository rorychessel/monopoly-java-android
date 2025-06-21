package com.npv.monopoly;

public class Taxes implements Square {
    private final double fixTax;  // giá thuế
    private final double varTax;  // % thuế tùy theo thu nhập
    private final String name; // tên
    private final int posX; // vị trí X
    private final int posY; // vị trí Y

    // constructor
    public Taxes(int posX, int posY, boolean income) {
        if (income) {
            fixTax = 200;
            varTax = 10;
            this.name = "Thue BDS";
        } else {
            fixTax = 75;
            varTax = 0;
            this.name = "Thue Cho";
        }
        this.posX = posX;
        this.posY = posY;
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

    public String name() {
        return name;
    }

    public boolean isOwnable() {
        return false;
    }

    public double tax() {
        return fixTax;
    }

    // trả về tiền thuế
    public double tax(double value) {
        // nếu không có gì thay đổi thì trả về giá trị fixTax
        if (varTax == 0)
            return fixTax;
        double percent = varTax / 100.0;
        return (int) (value * percent);
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
}
