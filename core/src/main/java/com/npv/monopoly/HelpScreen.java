package com.npv.monopoly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class HelpScreen implements Screen {
    private final StartGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private int selectedTab = 0;
    private boolean isHoveredBack, isTab1Hovered, isTab2Hovered;
    private String errorMessage = null;
    private Texture contentBackground, backgroundTexture;
    private float scrollY = 0; // Vị trí cuộn
    private float maxScrollY; // Giới hạn cuộn tối đa

    private static final float TAB_WIDTH_RATIO = 0.35f;
    private static final float TAB_HEIGHT_RATIO = 0.08f;
    private static final float GAP_RATIO = 0.015f;
    private static final float CONTENT_WIDTH_RATIO = 0.9f;
    private static final float CONTENT_HEIGHT_RATIO = 0.7f;
    private static final float CLOSE_BUTTON_WIDTH_RATIO = 0.2f;
    private static final float CLOSE_BUTTON_HEIGHT_RATIO = 0.07f;
    private static final float VERTICAL_MARGIN_RATIO = 0.05f;
    private static final float SHADOW_OFFSET_RATIO = 0.005f;
    private static final float SCROLL_SPEED = 30f;

    private static final String[] OFFICIAL_RULES = {
        "LUẬT CHÍNH THỨC",
        "* Mục tiêu:",
        "- Trở thành người chơi giàu nhất bằng cách mua, cho thuê và giao dịch bất động sản, khiến đối thủ phá sản.",
        "* Nhân viên ngân hàng:",
        "- Quản lý tiền, Giấy chứng nhận quyền sở hữu và đấu giá.",
        "- Trả lương $200 khi vượt qua GO, thu thuế và tiền phạt.",
        "- Bán bất động sản, nhà, khách sạn, cho vay tiền thế chấp.",
        "* Cách chơi:",
        "- Tung xúc xắc để di chuyển quân cờ theo kim đồng hồ.",
        "- Dừng trên bất động sản chưa có chủ để mua hoặc đấu giá.",
        "- Trả tiền thuê nếu dừng trên bất động sản đã có chủ.",
        "- Rút thẻ Chance hoặc Community Chest và làm theo hướng dẫn.",
        "- Thanh toán thuế (Thuế thu nhập: $200 hoặc 10%, Thuế xa xỉ: $100).",
        "- Ô Free Parking không có tác dụng (theo quy tắc chính thức).",
        "- Vào tù khi dừng ở 'Đi vào tù', tung đôi 3 lần, hoặc rút thẻ; thoát bằng $50, thẻ 'Thoát khỏi tù', hoặc tung đôi trong 3 lượt.",
        "* Giao dịch:",
        "- Giao dịch bất động sản, tiền mặt, thẻ 'Thoát khỏi tù' khi tất cả đồng ý.",
        "- Cần độc quyền nhóm màu để xây nhà.",
        "- Nhà phải đều trên nhóm (không quá 1 nhà chênh lệch).",
        "- 4 nhà nâng cấp thành khách sạn (tối đa 1 khách sạn).",
        "- Bán lại nhà/khách sạn cho Ngân hàng với nửa giá.",
        "* Thế chấp:",
        "- Thế chấp bất động sản chưa cải tạo với giá trên Giấy chứng nhận.",
        "- Gỡ thế chấp với tiền vay + 10% lãi.",
        "* Phá sản:",
        "- Không trả nổi nợ với Ngân hàng hoặc người chơi.",
        "- Tài sản chuyển cho chủ nợ hoặc đấu giá nếu với Ngân hàng.",
        "* Chiến thắng:",
        "- Người chơi cuối cùng còn lại thắng."
    };

    private static final String[] HOUSE_RULES = {
        "LUẬT NHÀ & TÀI SẢN",
        "* Giao dịch:",
        "- Tự do giao dịch bất kỳ lúc nào, không cần đến lượt.",
        "- Cần đồng ý từ tất cả bên liên quan, không giới hạn thời gian.",
        "* Tù:",
        "- Phải tung đôi để thoát, không nộp $50.",
        "- Nhận $50 từ Ngân hàng khi vào tù (thưởng an ủi).",
        "* Bất động sản:",
        "- Bắt buộc đấu giá nếu không mua.",
        "- Tiền thuê gấp đôi nếu độc quyền nhóm màu (không cần nhà).",
        "- Xây hơn 4 nhà trước khi nâng cấp thành khách sạn.",
        "* Chiến thắng:",
        "- Kết thúc khi người đầu tiên phá sản, người giàu nhất thắng."
    };

    public HelpScreen(StartGame game) {
        this.game = game;
        try {
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            glyphLayout = new GlyphLayout();
            backgroundTexture = new Texture("background.jpg");

            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 40;
            parameter.shadowOffsetX = 2;
            parameter.shadowOffsetY = 2;
            parameter.shadowColor = new Color(0, 0, 0, 0.7f);
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđĐÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ";
            font = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            errorMessage = "Error initializing: " + e.getMessage();
            Gdx.app.error("HelpScreen", errorMessage, e);
        }
    }

    @Override
    public void show() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float contentWidth = screenWidth * CONTENT_WIDTH_RATIO;
        float contentHeight = screenHeight * CONTENT_HEIGHT_RATIO;

        Pixmap pixmap = new Pixmap((int) contentWidth, (int) contentHeight, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(1f, 1f, 1f, 0.9f));
        pixmap.fillRectangle(0, 0, (int) contentWidth, (int) contentHeight);
        contentBackground = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (batch == null || shapeRenderer == null || glyphLayout == null || font == null || contentBackground == null) {
            if (errorMessage != null && batch != null && font != null) {
                batch.begin();
                font.draw(batch, errorMessage, 20, Gdx.graphics.getHeight() - 20);
                batch.end();
            }
            return;
        }

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float tabWidth = screenWidth * TAB_WIDTH_RATIO;
        float tabHeight = screenHeight * TAB_HEIGHT_RATIO;
        float gap = screenWidth * GAP_RATIO;
        float contentWidth = screenWidth * CONTENT_WIDTH_RATIO;
        float contentHeight = screenHeight * CONTENT_HEIGHT_RATIO;
        float closeButtonWidth = screenWidth * CLOSE_BUTTON_WIDTH_RATIO;
        float closeButtonHeight = screenHeight * CLOSE_BUTTON_HEIGHT_RATIO;
        float shadowOffset = screenWidth * SHADOW_OFFSET_RATIO;
        float verticalMargin = screenHeight * VERTICAL_MARGIN_RATIO;

        float tabStartX = (screenWidth - (tabWidth * 2 + gap)) / 2;
        float tabStartY = screenHeight - tabHeight - verticalMargin;
        float contentX = (screenWidth - contentWidth) / 2;
        float contentY = (screenHeight - contentHeight) / 2;
        float closeButtonX = (screenWidth - closeButtonWidth) / 2;
        float closeButtonY = contentY - closeButtonHeight - verticalMargin;

        int screenX = Gdx.input.getX();
        int screenY = Gdx.graphics.getHeight() - Gdx.input.getY();
        isTab1Hovered = isHovered(screenX, screenY, tabStartX, tabStartY, tabWidth, tabHeight);
        isTab2Hovered = isHovered(screenX, screenY, tabStartX + tabWidth + gap, tabStartY, tabWidth, tabHeight);
        isHoveredBack = isHovered(screenX, screenY, closeButtonX, closeButtonY, closeButtonWidth, closeButtonHeight);

        if (isHovered(screenX, screenY, contentX, contentY, contentWidth, contentHeight)) {
            float scrollAmount = Gdx.input.getDeltaY() * SCROLL_SPEED;
            scrollY += scrollAmount;
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        }

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawButton(tabStartX, tabStartY, tabWidth, tabHeight, isTab1Hovered || selectedTab == 0);
        drawButton(tabStartX + tabWidth + gap, tabStartY, tabWidth, tabHeight, isTab2Hovered || selectedTab == 1);
        drawButton(closeButtonX, closeButtonY, closeButtonWidth, closeButtonHeight, isHoveredBack);
        shapeRenderer.end();

        batch.begin();
        batch.draw(contentBackground, contentX, contentY, contentWidth, contentHeight);

        glyphLayout.setText(font, "LUẬT CHÍNH THỨC");
        drawText("LUẬT CHÍNH THỨC", tabStartX, tabStartY + tabHeight / 2 + glyphLayout.height / 2, tabWidth, Color.WHITE);
        glyphLayout.setText(font, "LUẬT NHÀ & TÀI SẢN");
        drawText("LUẬT NHÀ & TÀI SẢN", tabStartX + tabWidth + gap, tabStartY + tabHeight / 2 + glyphLayout.height / 2, tabWidth, Color.WHITE);

        String[] currentRules = (selectedTab == 0) ? OFFICIAL_RULES : HOUSE_RULES;
        float contentTextY = contentY + contentHeight - glyphLayout.height - 30f + scrollY;
        float totalTextHeight = 0;

        for (String line : currentRules) {
            glyphLayout.setText(font, line);
            totalTextHeight += glyphLayout.height + 20f;
        }
        maxScrollY = Math.max(0, totalTextHeight - contentHeight + 60f);

        for (String line : currentRules) {
            glyphLayout.setText(font, line);
            if (contentTextY - glyphLayout.height >= contentY && contentTextY <= contentY + contentHeight) {
                drawTextLeft(line, contentX + 30f, contentTextY, contentWidth - 60f, Color.BLACK);
            }
            contentTextY -= glyphLayout.height + 20f;
        }

        glyphLayout.setText(font, "ĐÓNG");
        drawText("ĐÓNG", closeButtonX, closeButtonY + closeButtonHeight / 2 + glyphLayout.height / 2, closeButtonWidth, Color.WHITE);
        batch.end();

        if (Gdx.input.justTouched()) {
            if (isTab1Hovered) selectedTab = 0;
            if (isTab2Hovered) selectedTab = 1;
            if (isHoveredBack) game.setScreen(new FirstPageScreen(game));
        }
    }

    private boolean isHovered(int x, int y, float posX, float posY, float width, float height) {
        return x >= posX && x <= posX + width && y >= posY && y <= posY + height;
    }

    private void drawButton(float x, float y, float width, float height, boolean isHovered) {
        shapeRenderer.setColor(new Color(0, 0, 0, 0.3f));
        shapeRenderer.rect(x + 5, y - 5, width, height);
        shapeRenderer.setColor(isHovered ? new Color(0.3f, 0.6f, 0.9f, 1f) : new Color(0.2f, 0.5f, 0.8f, 1f));
        shapeRenderer.rect(x, y, width, height);
    }

    private void drawText(String text, float x, float y, float width, Color color) {
        font.setColor(color);
        glyphLayout.setText(font, text);
        float textX = x + (width - glyphLayout.width) / 2;
        font.draw(batch, glyphLayout, textX, y);
    }

    private void drawTextLeft(String text, float x, float y, float width, Color color) {
        font.setColor(color);
        glyphLayout.setText(font, text);
        font.draw(batch, glyphLayout, x, y);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (contentBackground != null) contentBackground.dispose();
    }

    @Override
    public void resize(int width, int height) {
        if (contentBackground != null) {
            contentBackground.dispose();
        }
        float contentWidth = width * CONTENT_WIDTH_RATIO;
        float contentHeight = height * CONTENT_HEIGHT_RATIO;
        Pixmap pixmap = new Pixmap((int) contentWidth, (int) contentHeight, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(1f, 1f, 1f, 0.9f));
        pixmap.fillRectangle(0, 0, (int) contentWidth, (int) contentHeight);
        contentBackground = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
