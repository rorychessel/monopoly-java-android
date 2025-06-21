package com.npv.monopoly;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import java.util.ArrayList;
import java.util.List;
public class StartGame extends Game {
    private StartGame game;
    public SpriteBatch batch;
    public BitmapFont font;
    private float elapsedTime;
    private static final float START_DURATION = 1f;
    public static List<GameSetupScreen.PlayerSetupInfo> configuredPlayersData;
    public static int configuredNumPlayersData;
    public static boolean playWithAIData; // true nếu người dùng chọn chơi với máy

    // Hằng số chứa các ký tự Tiếng Việt để nạp font
    public static final String VIETNAMESE_CHARACTERS = "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđĐÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ";

    @Override
    public void create() {
        batch = new SpriteBatch(); // Khởi tạo SpriteBatch

        BitmapFont tempFont = null;
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 24;
            parameter.characters = VIETNAMESE_CHARACTERS; // Nạp các ký tự Tiếng Việt
            parameter.color = Color.WHITE; // Đặt màu chữ mặc định
            tempFont = generator.generateFont(parameter);
            generator.dispose(); // Giải phóng generator sau khi tạo font
        } catch (Exception e) {
            Gdx.app.error("StartGame", "Không thể nạp font chữ! Sử dụng font mặc định. " + e.getMessage());
            tempFont = new BitmapFont(); // Sử dụng font mặc định của LibGDX nếu có lỗi
        }
        font = tempFont; // Gán font đã tạo

        configuredPlayersData = new ArrayList<>(); // Khởi tạo list rỗng
        configuredNumPlayersData = 0;              // Số người chơi ban đầu là 0
        playWithAIData = false;                    // Mặc định không chơi với AI

        this.setScreen(new SplashScreen(this));
    }

    // Phương thức render() của lớp Game sẽ tự động gọi render() của Screen hiện tại.
    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        // Giải phóng tất cả tài nguyên chung đã được nạp trong create()
        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }

        if (getScreen() != null) {
            getScreen().dispose();
        }
        Gdx.app.log("StartGame", "Đã giải phóng tài nguyên chung.");
    }
}

class SplashScreen implements Screen {
    private StartGame game;
    private SpriteBatch batch;
    private Texture logo;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private float elapsedTime;
    private static final float SPLASH_DURATION = 3f;
    public static ArrayList<GameSetupScreen.PlayerSetupInfo> configuredPlayersData;
    public static int configuredNumPlayersData;
    public static boolean playWithAIData;

    public SplashScreen(StartGame game) {
        this.game = game;
        try {
            batch = new SpriteBatch();
            logo = new Texture("hasbro.png");

            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 32;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "©áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđĐ";
            font = generator.generateFont(parameter);
            generator.dispose();

            glyphLayout = new GlyphLayout();
            elapsedTime = 0;
        } catch (Exception e) {
            Gdx.app.error("SplashScreen", "Error initializing: " + e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
        if (batch == null || logo == null || font == null) {
            Gdx.app.error("SplashScreen", "One or more resources are null");
            return;
        }

        elapsedTime += delta;

        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();

        try {
            float logoWidth = logo.getWidth();
            float logoHeight = logo.getHeight();
            float logoX = (Gdx.graphics.getWidth() - logoWidth) / 2;
            float logoY = (Gdx.graphics.getHeight() - logoHeight) / 2;
            Gdx.app.log("SplashScreen", "Logo position: x=" + logoX + ", y=" + logoY + ", width=" + logoWidth + ", height=" + logoHeight);
            batch.draw(logo, logoX, logoY, logoWidth, logoHeight);

            String copyrightText = "Bản quyền © 2025 Hasbro. Mọi quyền được bảo lưu.";
            font.setColor(1, 1, 1, 1);
            glyphLayout.setText(font, copyrightText);
            float textWidth = glyphLayout.width;
            float textX = (Gdx.graphics.getWidth() - textWidth) / 2;
            float textY = logoY - logoHeight / 2 - 20;
            Gdx.app.log("SplashScreen", "Text position: x=" + textX + ", y=" + textY + ", width=" + textWidth);
            font.draw(batch, copyrightText, textX, textY);

        } catch (Exception e) {
            Gdx.app.error("SplashScreen", "Error rendering: " + e.getMessage());
        }

        batch.end();

        // Chuyển sang IntermediateScreen sau 3 giây
        if (elapsedTime >= SPLASH_DURATION) {
            game.setScreen(new IntermediateScreen(game));
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (logo != null) logo.dispose();
        if (font != null) font.dispose();
        if (glyphLayout != null) glyphLayout = null;
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}

class IntermediateScreen implements Screen {
    private StartGame game;
    private SpriteBatch batch;
    private Texture logoBkhn;
    private Texture logoFami;
    private float elapsedTime;
    private static final float INTERMEDIATE_DURATION = 3f; // Tổng thời gian hiển thị 3 giây
    private static final float FADE_IN_DURATION = 1f; // Thời gian fade in 1 giây
    private static final float DISPLAY_DURATION = 1f; // Thời gian hiển thị 1 giây
    private static final float FADE_OUT_DURATION = 1f; // Thời gian fade out 1 giây
    private static final float LOGO_GAP = 20f; // Khoảng cách giữa hai logo

    public IntermediateScreen(StartGame game) {
        this.game = game;
        try {
            batch = new SpriteBatch();
            logoBkhn = new Texture("bkhn.png");
            logoFami = new Texture("fami.jpg");
            elapsedTime = 0;
        } catch (Exception e) {
            Gdx.app.error("IntermediateScreen", "Error initializing: " + e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
        if (batch == null || logoBkhn == null || logoFami == null) {
            Gdx.app.error("IntermediateScreen", "One or more resources are null");
            return;
        }

        elapsedTime += delta;

        // Xóa màn hình và đặt nền đen
        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();

        try {
            // Tính kích thước logo
            float bkhnWidth = logoBkhn.getWidth() * 0.3f;
            float bkhnHeight = logoBkhn.getHeight() * 0.3f;
            float famiWidth = logoFami.getWidth() * 0.175f;
            float famiHeight = logoFami.getHeight() * 0.175f;

            // Tính tổng chiều rộng của hai logo và khoảng cách
            float totalWidth = bkhnWidth + LOGO_GAP + famiWidth;

            // Tính tọa độ để căn giữa theo chiều ngang
            float startX = (Gdx.graphics.getWidth() - totalWidth) / 2;
            float centerY = (Gdx.graphics.getHeight() - Math.max(bkhnHeight, famiHeight)) / 2;

            // Tính toán alpha cho hiệu ứng fade
            float alpha = 0f;
            if (elapsedTime < FADE_IN_DURATION) {
                // Fade in: tăng từ 0 đến 1 trong 1 giây đầu
                alpha = elapsedTime / FADE_IN_DURATION;
            } else if (elapsedTime < FADE_IN_DURATION + DISPLAY_DURATION) {
                // Hiển thị: giữ alpha = 1 trong 1 giây
                alpha = 1f;
            } else {
                // Fade out: giảm từ 1 về 0 trong 1 giây cuối
                alpha = 1f - (elapsedTime - (FADE_IN_DURATION + DISPLAY_DURATION)) / FADE_OUT_DURATION;
                alpha = Math.max(0f, alpha);
            }

            // Đặt độ trong suốt cho batch
            batch.setColor(1, 1, 1, alpha);

            // Vẽ logo bkhn
            float bkhnX = startX;
            float bkhnY = centerY;
            Gdx.app.log("IntermediateScreen", "Bkhn logo position: x=" + bkhnX + ", y=" + bkhnY + ", width=" + bkhnWidth + ", height=" + bkhnHeight + ", alpha=" + alpha);
            batch.draw(logoBkhn, bkhnX, bkhnY, bkhnWidth, bkhnHeight);

            // Vẽ logo fami
            float famiX = startX + bkhnWidth + LOGO_GAP;
            float famiY = centerY;
            Gdx.app.log("IntermediateScreen", "Fami logo position: x=" + famiX + ", y=" + famiY + ", width=" + famiWidth + ", height=" + famiHeight + ", alpha=" + alpha);
            batch.draw(logoFami, famiX, famiY, famiWidth, famiHeight);

            // Reset color của batch
            batch.setColor(1, 1, 1, 1);

        } catch (Exception e) {
            Gdx.app.error("IntermediateScreen", "Error rendering: " + e.getMessage());
        }

        batch.end();

        if (elapsedTime >= INTERMEDIATE_DURATION) {
            game.setScreen(new FirstPageScreen(game));
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (logoBkhn != null) logoBkhn.dispose();
        if (logoFami != null) logoFami.dispose();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
