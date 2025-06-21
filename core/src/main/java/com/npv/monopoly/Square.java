package com.npv.monopoly;
public interface Square {
    int positionX(); // vị trí X ô đất
    int positionY(); // vị trí Y ô đất
    String name(); // tên ô đất
    boolean isOwnable(); // có thể mua không
    boolean isOwned(); // có chủ chưa?
    int cost(); // giá mua
    void purchase(Player player); // giao dịch
    int rent(); // giá thuê

    Player owner(); // chủ sở hữu

    String toString();
}
