package com.npv.monopoly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class FirstPageScreen implements Screen {
    private final StartGame game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private final Texture backgroundTexture;
    private final Texture logoTexture;

    private float btnWidth, btnHeight, btnX, btnNewGameY, btnHelpY, btnExitY;
    private boolean isHoveredNewGame, isHoveredHelp, isHoveredExit;

    public FirstPageScreen(StartGame game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        backgroundTexture = new Texture(Gdx.files.internal("background.jpg"));
        logoTexture = new Texture(Gdx.files.internal("logo.png"));
        glyphLayout = new GlyphLayout();

        // Load font với try-catch
        BitmapFont loadedFont = null;
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 40;
            parameter.shadowOffsetX = 2;
            parameter.shadowOffsetY = 2;
            parameter.shadowColor = new Color(0, 0, 0, 0.7f);
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐĐ";
            loadedFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("FirstPageScreen", "Error loading font: " + e.getMessage());
        }
        font = loadedFont;
    }

    @Override
    public void render(float delta) {
        if (font == null) return;

        btnWidth = Gdx.graphics.getWidth() * 0.2f;
        btnHeight = Gdx.graphics.getHeight() * 0.1f;
        btnX = (Gdx.graphics.getWidth() - btnWidth) / 2;
        btnNewGameY = Gdx.graphics.getHeight() * 0.5f;
        btnHelpY = btnNewGameY - btnHeight - Gdx.graphics.getHeight() * 0.05f;
        btnExitY = btnHelpY - btnHeight - Gdx.graphics.getHeight() * 0.05f;

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        isHoveredNewGame = isHovered(mouseX, mouseY, btnX, btnNewGameY, btnWidth, btnHeight);
        isHoveredHelp = isHovered(mouseX, mouseY, btnX, btnHelpY, btnWidth, btnHeight);
        isHoveredExit = isHovered(mouseX, mouseY, btnX, btnExitY, btnWidth, btnHeight);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        float logoWidth = Gdx.graphics.getWidth() * 0.5f;
        float logoHeight = logoWidth * (logoTexture.getHeight() / (float)logoTexture.getWidth());
        float logoX = (Gdx.graphics.getWidth() - logoWidth) / 2;
        float logoY = Gdx.graphics.getHeight() - logoHeight - 50;
        batch.draw(logoTexture, logoX, logoY, logoWidth, logoHeight);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawButton(btnX, btnNewGameY, btnWidth, btnHeight, isHoveredNewGame);
        drawButton(btnX, btnHelpY, btnWidth, btnHeight, isHoveredHelp);
        drawButton(btnX, btnExitY, btnWidth, btnHeight, isHoveredExit);
        shapeRenderer.end();

        batch.begin();
        drawText("BẮT ĐẦU!", btnX, btnNewGameY + btnHeight / 2 + glyphLayout.height / 2, btnWidth, Color.WHITE);
        drawText("HƯỚNG DẪN", btnX, btnHelpY + btnHeight / 2 + glyphLayout.height / 2, btnWidth, Color.WHITE);
        drawText("THOÁT!", btnX, btnExitY + btnHeight / 2 + glyphLayout.height / 2, btnWidth, Color.WHITE);
        batch.end();

        if (Gdx.input.justTouched()) {
            if (isHoveredNewGame) game.setScreen(new GameSetupScreen(game));
            if (isHoveredHelp) game.setScreen(new HelpScreen(game));
            if (isHoveredExit) Gdx.app.exit();
        }
    }

    private void drawButton(float x, float y, float width, float height, boolean isHovered) {
        shapeRenderer.setColor(new Color(0, 0, 0, 0.3f)); // Đổ bóng
        shapeRenderer.rect(x + 5, y - 5, width, height);
        shapeRenderer.setColor(isHovered ? new Color(0.3f, 0.6f, 0.9f, 1f) : new Color(0.2f, 0.5f, 0.8f, 1f));
        shapeRenderer.rect(x, y, width, height);
    }

    private boolean isHovered(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void drawText(String text, float x, float y, float width, Color color) {
        font.setColor(color);
        glyphLayout.setText(font, text); // Cập nhật glyphLayout cho văn bản hiện tại
        float textX = x + (width - glyphLayout.width) / 2; // Căn giữa theo chiều ngang
        font.draw(batch, glyphLayout, textX, y); // Vẽ văn bản
    }

    @Override
    public void show() {}
    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (logoTexture != null) logoTexture.dispose();
        if (font != null) font.dispose();
    }
}
