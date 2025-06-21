package com.npv.monopoly;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Board {
    private final int N; // Số ô trên bàn cờ
    private final Square[] squaresByIndex; // Lưu các ô theo chỉ số 0-(N-1)
    private final Map<Point, Square> squaresByCoords; // Lưu các ô theo tọa độ (x, y)
    private final Deck chance;
    // private final Deck community;
    private int jailVisitingSquareX; // Tọa độ X của ô "Thăm Tù"
    private int jailVisitingSquareY; // Tọa độ Y của ô "Thăm Tù"
    private int jailSquareIndex = 8;

    // Tọa độ của ô đất
    public static class Point {
        final int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    // Mảng tọa độ cho 40 ô
    public static final Point[] POSITIONS_XY = new Point[40];
    static {
        // cột trái
        POSITIONS_XY[0] = new Point(60, 60); // Go
        POSITIONS_XY[1] = new Point(60, 160);  // Phù Đổng
        POSITIONS_XY[2] = new Point(60, 240);  // Cơ Hội
        POSITIONS_XY[3] = new Point(60, 320);  // Bát Tràng
        POSITIONS_XY[4] = new Point(60, 400);  // Long Biên
        POSITIONS_XY[5] = new Point(60, 480);  // BX Gia Lâm
        POSITIONS_XY[6] = new Point(60, 560);  // Bồ Đề
        POSITIONS_XY[7] = new Point(60, 640);  // Yên Sở
        POSITIONS_XY[8] = new Point(60, 740);  // Vào tù

        // hàng trên
        POSITIONS_XY[9] = new Point(160, 740);  // Hoàng Liệt
        POSITIONS_XY[10] = new Point(240, 740); // Thuế Chợ
        POSITIONS_XY[11] = new Point(320, 740);  // Kiến Hưng
        POSITIONS_XY[12] = new Point(400, 740);  // Hà Đông
        POSITIONS_XY[13] = new Point(480, 740);  // Cơ Hội
        POSITIONS_XY[14] = new Point(560, 740);  // Phương Liệt
        POSITIONS_XY[15] = new Point(640, 740);  // Khương Đình
        POSITIONS_XY[16] = new Point(720, 740);  // Yên Hòa
        POSITIONS_XY[17] = new Point(800, 740);  // Ga Cầu Giấy
        POSITIONS_XY[18] = new Point(880, 740);  // Cầu Giấy
        POSITIONS_XY[19] = new Point(960, 740);  // Đại Mỗ

        // cột phải
        POSITIONS_XY[20] = new Point(1060, 740);  // CV Hòa Bình
        POSITIONS_XY[21] = new Point(1060, 640);  // Từ Liêm
        POSITIONS_XY[22] = new Point(1060, 560);  // Cơ Hội
        POSITIONS_XY[23] = new Point(1060, 480);  // Phú Diễn
        POSITIONS_XY[24] = new Point(1060, 400);  // Đông Ngạc
        POSITIONS_XY[25] = new Point(1060, 320);  // BX Nam Thăng Long
        POSITIONS_XY[26] = new Point(1060, 240);  // Tây Hồ
        POSITIONS_XY[27] = new Point(1060, 160);  // Phú Thượng
        POSITIONS_XY[28] = new Point(1060, 60);  // Thăm Tù

        // hàng dưới
        POSITIONS_XY[29] = new Point(960, 60);  // Văn Miếu
        POSITIONS_XY[30] = new Point(880, 60); // Kim Liên
        POSITIONS_XY[31] = new Point(800, 60); // Ngọc Hà
        POSITIONS_XY[32] = new Point(720, 60); // Ga Hà Nội
        POSITIONS_XY[33] = new Point(640, 60); // Ba Đình
        POSITIONS_XY[34] = new Point(560, 60); // Hồng Hà
        POSITIONS_XY[35] = new Point(480, 60); // Cơ Hội
        POSITIONS_XY[36] = new Point(400, 60); // Bạch Mai
        POSITIONS_XY[37] = new Point(320, 60); // Cửa Nam
        POSITIONS_XY[38] = new Point(240, 60); // Thuế BĐS
        POSITIONS_XY[39] = new Point(160, 60); // Hoàn Kiếm
    }

    public Board(Deck chance, Deck community) {
        this.N = 40;
        this.squaresByIndex = new Square[N];
        this.squaresByCoords = new HashMap<>();
        this.chance = chance;
        // this.community = community;

        for (int i = 0; i < N; i++) {
            Point currentPoint = POSITIONS_XY[i];
            Square square = makeSquare(i, currentPoint.x, currentPoint.y);
            this.squaresByIndex[i] = square;
            this.squaresByCoords.put(currentPoint, square);

            if (i == 28) { // Ô "Just Visiting/In Jail"
                this.jailVisitingSquareX = currentPoint.x;
                this.jailVisitingSquareY = currentPoint.y;
            }
        }

        makeGroups();
        makeRail();
        // makeUtil(deterministic);
    }

    public int size() {
        return N;
    }

    public Property property(String name) {
        for (Square sq : squaresByIndex) {
            if (sq instanceof Property && sq.name().equals(name)) {
                return (Property) sq;
            }
        }
        return null;
    }

    public Square square(int posX, int posY) {
        return squaresByCoords.get(new Point(posX, posY));
    }

    public Square getSquareByIndex(int index) {
        if (index >= 0 && index < N) {
            return squaresByIndex[index];
        }
        return null;
    }

    public int getIndexForSquare(Square square) {
        if (square == null) return -1;
        for (int i = 0; i < N; i++) {
            if (squaresByIndex[i] == square) {
                return i;
            }
        }
        return -1;
    }

    public Square[] getBoardSquares() {
        return squaresByIndex;
    }

    public Square getSquare(int index) {
        if (index >= 0 && index < N) {
            return squaresByIndex[index];
        }
        return null;
    }

    public Deck getChanceDeck() {
        return chance;
    }

    private Square makeSquare(int traditionalIndex, int x, int y) {
        switch (traditionalIndex) {
            case 0: return go(x, y);
            case 1: return phuDong(x, y);
            case 2: return chance(x, y);
            case 3: return batTrang(x, y);
            case 4: return longBien(x, y);
            case 5: return BXGiaLam(x, y);
            case 6: return boDe(x, y);
            case 7: return yenSo(x, y);
            case 8: return vaoTu(x, y);
            case 9: return hoangLiet(x, y);
            case 10: return thueCho(x, y);
            case 11: return kienHung(x, y);
            case 12: return haDong(x, y);
            case 13: return chance(x, y);
            case 14: return phuongLiet(x, y);
            case 15: return khuongDinh(x, y);
            case 16: return yenHoa(x, y);
            case 17: return gaCauGiay(x, y);
            case 18: return cauGiay(x, y);
            case 19: return daiMo(x, y);
            case 20: return CVHoaBinh(x, y);
            case 21: return tuLiem(x, y);
            case 22: return chance(x, y);
            case 23: return phuDien(x, y);
            case 24: return dongNgac(x, y);
            case 25: return BXNamThangLong(x, y);
            case 26: return tayHo(x, y);
            case 27: return phuThuong(x, y);
            case 28: return thamTu(x, y);
            case 29: return vanMieu(x, y);
            case 30: return kimLien(x, y);
            case 31: return ngocHa(x, y);
            case 32: return gaHaNoi(x, y);
            case 33: return baDinh(x, y);
            case 34: return hongHa(x, y);
            case 35: return chance(x, y);
            case 36: return bachMai(x, y);
            case 37: return cuaNam(x, y);
            case 38: return thueBDS(x, y);
            case 39: return hoanKiem(x, y);
            default:
                System.err.println("ERROR: Index in Square: " + traditionalIndex);
                return null;
        }
    }

    private void makeGroups() {
        makeGroup("Phù Đổng", "Bát Tràng");
        makeGroup("Long Biên", "Bồ Đề", "Yên Sở");
        makeGroup("Hoàng Liệt", "Kiến Hưng", "Hà Đông");
        makeGroup("Phương Liệt", "Khương Đình", "Yên Hòa");
        makeGroup("Cầu Giấy", "Đại Mỗ", "Từ Liêm");
        makeGroup("Phú Diễn", "Đông Ngạc", "Tây Hồ");
        makeGroup("Phú Thượng", "Văn Miếu", "Kim Liên");
        makeGroup("Ngọc Hà", "Ba Đình", "Hồng Hà");
        makeGroup("Bạch Mai", "Cửa Nam", "Hoàn Kiếm");
    }

    private void makeRail() {
        Railroad a = (Railroad) getSquareByIndex(5); // BX Gia Lâm
        Railroad b = (Railroad) getSquareByIndex(17); // Ga Cầu Giấy
        Railroad c = (Railroad) getSquareByIndex(25); // BX Nam Thăng Long
        Railroad d = (Railroad) getSquareByIndex(32); // Ga Hà Nội

        if (a != null && b != null && c != null && d != null) {
            a.createGroup(b, c, d);
            b.createGroup(a, c, d);
            c.createGroup(a, b, d);
            d.createGroup(a, b, c);
        } else {
            System.err.println("ERROR: Cannot makeGroup for railroads!");
        }
    }

    private void makeGroup(String nameA, String nameB) {
        makeGroup(nameA, nameB, null);
    }

    private void makeGroup(String nameA, String nameB, String nameC) {
        Property propA = property(nameA);
        Property propB = property(nameB);
        Property propC = null;
        if (nameC != null) {
            propC = property(nameC);
        }

        if (propA == null || propB == null || (nameC != null && propC == null)) {
            System.err.println("ERROR: Properties are not able to makeGroup: "
                + nameA + ", " + nameB + (nameC != null ? ", " + nameC : ""));
            return;
        }

        propA.setGroup(propB, propC);
        propB.setGroup(propA, propC);
        if (propC != null) {
            propC.setGroup(propA, propB);
        }
    }

    private Square go(int x, int y) {
        return new Inactive("Go", x, y);
    }

    private Square phuDong(int x, int y) {
        return new Property("Phù Đổng", x, y, 20, 10, 30, 90, 160, 250, 60, 50);
    }

    private Square chance(int x, int y) {
        return new ChanceSquare("Chance", x, y, this.chance);
    }

    private Square batTrang(int x, int y) {
        return new Property("Bát Tràng", x, y, 40, 20, 60, 180, 320, 450, 60, 50);
    }

    private Square longBien(int x, int y) {
        return new Property("Long Biên", x, y, 60, 30, 90, 270, 400, 550, 100, 50);
    }

    private Square BXGiaLam(int x, int y) {
        return new Railroad("BX Gia Lâm", x, y);
    }

    private Square boDe(int x, int y) {
        return new Property("Bồ Đề", x, y, 60, 30, 90, 270, 400, 550, 100, 50);
    }

    private Square yenSo(int x, int y) {
        return new Property("Yên Sở", x, y, 80, 40, 100, 300, 450, 600, 120, 50);
    }

    private Square vaoTu(int x, int y) {
        return new Jail("Vào Tù", x, y, Jail.JailType.TO_JAIL);
    }

    private Square thamTu(int x, int y) {
        return new Jail("Thăm Tù", x, y, Jail.JailType.VISITING);
    }

    private Square hoangLiet(int x, int y) {
        return new Property("Hoàng Liệt", x, y, 100, 50, 150, 450, 625, 750, 140, 100);
    }

    private Square thueCho(int x, int y) {
        return new Taxes(x, y, false);
    }

    private Square kienHung(int x, int y) {
        return new Property("Kiến Hưng", x, y, 100, 50, 150, 450, 625, 750, 140, 100);
    }

    private Square haDong(int x, int y) {
        return new Property("Hà Đông", x, y, 100, 50, 150, 450, 625, 750, 140, 100);
    }

    private Square phuongLiet(int x, int y) {
        return new Property("Phương Liệt", x, y, 120, 60, 180, 500, 700, 900, 160, 100);
    }

    private Square khuongDinh(int x, int y) {
        return new Property("Khương Đình", x, y, 120, 60, 180, 500, 700, 900, 160, 100);
    }

    private Square yenHoa(int x, int y) {
        return new Property("Yên Hòa", x, y, 120, 60, 180, 500, 700, 900, 160, 100);
    }

    private Square gaCauGiay(int x, int y) {
        return new Railroad("Ga Cầu Giấy", x, y);
    }

    private Square cauGiay(int x, int y) {
        return new Property("Cầu Giấy", x, y, 140, 70, 200, 550, 750, 950, 180, 100);
    }

    private Square daiMo(int x, int y) {
        return new Property("Đại Mỗ", x, y, 140, 70, 200, 550, 750, 950, 180, 100);
    }

    private Square CVHoaBinh(int x, int y) {
        return new Inactive("Công viên Hòa Bình", x, y);
    }

    private Square tuLiem(int x, int y) {
        return new Property("Từ Liêm", x, y, 160, 80, 220, 600, 800, 1000, 200, 100);
    }

    private Square phuDien(int x, int y) {
        return new Property("Phú Diễn", x, y, 160, 80, 220, 600, 800, 1000, 200, 100);
    }

    private Square dongNgac(int x, int y) {
        return new Property("Đông Ngạc", x, y, 180, 90, 250, 700, 875, 1050, 220, 150);
    }

    private Square tayHo(int x, int y) {
        return new Property("Tây Hồ", x, y, 180, 90, 250, 700, 875, 1050, 220, 150);
    }

    private Square BXNamThangLong(int x, int y) {
        return new Railroad("BX Nam Thăng Long", x, y);
    }

    private Square phuThuong(int x, int y) {
        return new Property("Phú Thượng", x, y, 200, 100, 300, 750, 925, 1100, 240, 150);
    }

    private Square vanMieu(int x, int y) {
        return new Property("Văn Miếu", x, y, 220, 110, 330, 800, 975, 1150, 260, 150);
    }

    private Square kimLien(int x, int y) {
        return new Property("Kim Liên", x, y, 220, 110, 330, 800, 975, 1150, 260, 150);
    }

    private Square ngocHa(int x, int y) {
        return new Property("Ngọc Hà", x, y, 240, 120, 360, 850, 1025, 1200, 280, 150);
    }

    private Square gaHaNoi(int x, int y) {
        return new Railroad("Ga Hà Nội", x, y);
    }

    private Square baDinh(int x, int y) {
        return new Property("Ba Đình", x, y, 260, 130, 390, 900, 1100, 1275, 300, 200);
    }

    private Square hongHa(int x, int y) {
        return new Property("Hồng Hà", x, y, 260, 130, 390, 900, 1100, 1275, 300, 200);
    }

    private Square bachMai(int x, int y) {
        return new Property("Bạch Mai", x, y, 280, 150, 450, 1000, 1200, 1400, 320, 200);
    }

    private Square cuaNam(int x, int y) {
        return new Property("Cửa Nam", x, y, 280, 175, 500, 1100, 1300, 1500, 350, 200);
    }

    private Square thueBDS(int x, int y) {
        return new Taxes(x, y, true);
    }

    private Square hoanKiem(int x, int y) {
        return new Property("Hoàn Kiếm", x, y, 350, 200, 600, 1400, 1700, 2000, 400, 200);
    }

    public int getJailVisitingSquareX() {
        return jailVisitingSquareX;
    }

    public int getJailVisitingSquareY() {
        return jailVisitingSquareY;
    }

    public int getJailSquareIndex() {
        return jailSquareIndex;
    }
}
