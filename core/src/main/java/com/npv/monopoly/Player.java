package com.npv.monopoly;

public interface Player {

    // Trả về tên của người chơi
    String name();

    // Trả về số tiền hiện tại của người chơi
    int getMoney();

    // Thay đổi số tiền của người chơi
    void changeMoney(int amount);

    // Trả về chỉ số của ô hiện tại mà người chơi đang đứng trên bàn cờ (0-39)
    int getCurrentBoardIndex();

    // Di chuyển người chơi một số bước nhất định trên bàn cờ
    void moveBySteps(int numSpaces, int boardSize);

    // Di chuyển người chơi đến một ô cụ thể trên bàn cờ
    void moveToBoardIndex(int boardIndex);

    // Thêm một tài sản vào danh sách sở hữu của người chơi
    void addProperty(Square square);

    // Xóa một tài sản khỏi danh sách sở hữu của người chơi
    void removeProperty(Square square);

    // Trả về một Iterable chứa các tài sản mà người chơi sở hữu
    Iterable<Square> getProperties();

    // Đặt trạng thái người chơi vào tù
    void goToJail();

    // Kiểm tra xem người chơi có đang ở trong tù không
    boolean isInJail();

    // Giảm số lượt còn lại phải ở trong tù
    boolean decrementJailTurnsAndCheckStay();

    // Đặt trạng thái người chơi ra khỏi tù
    void leaveJail();

    // Thêm một thẻ ra tù cho người chơi
    void addGetOutOfJailFreeCard();

    // Người chơi sử dụng một thẻ ra tù
    boolean useGetOutOfJailFreeCard();

    // Trả về số lượng thẻ ra tù mà người chơi đang có
    int getNumGetOutOfJailFreeCards();

    // Trả về tổng giá trị tài sản của người chơi
    int getTotalAssetsValue();

    // Lấy số lần người chơi tung được đôi trong lượt hiện tại
    int getDoubleRollsThisTurn();

    // Tăng số lần người chơi tung được đôi trong lượt hiện tại
    void incrementDoubleRollsThisTurn();

    // Đặt lại số lần tung đôi về 0
    void resetDoubleRollsThisTurn();

    // Trả về số lượt người chơi còn phải ở trong tù
    int getJailTurnsRemaining();
}
