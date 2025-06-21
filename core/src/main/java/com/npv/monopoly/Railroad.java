package com.npv.monopoly;

public class Railroad implements Square {
    private final int COST = 200; // giá mua
    private final String name; // tên
    private final int posX; // vị trí X
    private final int posY; // vị trí Y
    private final Railroad[] others = new Railroad[3];
    private int numOwned;  // số lượng đã mua
    private Player owner; // chủ sở hữu
    private boolean owned;  // đã có chủ chưa?

    // constructor
    public Railroad(String name, int posX, int posY) {
        numOwned = 0;
        this.name = name;
        this.posX = posX;
        this.posY = posY;
    }

    public void createGroup(Railroad a, Railroad b, Railroad c){
        others[0] = a;
        others[1] = b;
        others[2] = c;
    }

    // cập nhật chủ sở hữu
    private void updateOwners() {
        if (owner == null) { // Nếu chưa có ai sở hữu railroad này
            numOwned = 0;
            return;
        }
        numOwned = 1; // Bắt đầu đếm từ railroad hiện tại
        for (Railroad r : others){
            if (r != null && r.isOwned() && r.owner() != null && r.owner().equals(owner))
                numOwned++;
        }
    }

    // trả về vị trí
    public int positionX() {
        return posX;
    }

    public int positionY() {
        return posY;
    }

    // trả về tên
    public String name() {
        return name;
    }

    // cập nhật trạng thái sau khi mua
    public void purchase(Player player) {
        owned = true;
        owner = player;

        updateOwners();
    }

    // kiểm tra xem có thể mua không?
    public boolean isOwnable() {
        return true;
    }

    // trả về số tiền thuê theo số lượng đã sở hữu
    public int rent() {
        updateOwners();

        switch (numOwned) {
            case 1:
                return 25;
            case 2:
                return 50;
            case 3:
                return 100;
            case 4:
                return 200;
            default:
                return 0;
        }
    }

    // trả về xem đã sở hữu chưa
    public boolean isOwned() {
        return owned;
    }

    // trả về chủ sở hữu
    public Player owner() {
        return owner;
    }

    // trả về giá mua
    public int cost() {
        return COST;
    }
}
