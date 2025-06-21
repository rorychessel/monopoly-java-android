package com.npv.monopoly;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class GameSetupScreen implements Screen {
    private final StartGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont smallFont;
    private GlyphLayout glyphLayout;
    private Texture backgroundTexture, friendButtonTexture, aiButtonTexture, tokenTextures[];

    private enum SetupState {
        SELECT_MODE,
        SELECT_NUM_PLAYERS,
        PLAYER_INPUT
    }
    private SetupState currentState = SetupState.SELECT_MODE;

    private int selectedMode = -1; // 0: Bạn bè, 1: Máy
    private int numPlayersToSetup = 0;
    private int currentPlayerInputIndex = 0;

    public static class PlayerSetupInfo {
        String name = "";
        int selectedTokenIndex = -1;
        Texture tokenTexture;

        PlayerSetupInfo(String name, int tokenIndex, Texture tokenTexture) {
            this.name = name;
            this.selectedTokenIndex = tokenIndex;
            this.tokenTexture = tokenTexture;
        }
    }
    private List<PlayerSetupInfo> configuredPlayers;

    private String currentInputPlayerName = "";
    private int currentSelectedToken = -1;

    private boolean isInputBoxHovered, isFriendButtonHovered, isAIButtonHovered, isStartButtonHovered,
        isSaveAndNextButtonHovered;

    // KÍCH THƯỚC NÚT CHỌN CHẾ ĐỘ
    private static final float MODE_BUTTON_WIDTH = 320f;
    private static final float MODE_BUTTON_HEIGHT = 160f;

    private static final float TOKEN_SIZE = 100f;
    private static final float TOKEN_GAP = 15f;
    private static final float GAP = 30f;
    private static final float SHADOW_OFFSET = 5f;
    private static final float INPUT_BOX_WIDTH = 400f;
    private static final float INPUT_BOX_HEIGHT = 70f;

    private static final float ACTION_BUTTON_WIDTH = 320f;
    private static final float ACTION_BUTTON_HEIGHT = 100f;

    private static final float PLAYER_LIST_X_OFFSET = 50f;
    private static final float PLAYER_LIST_START_Y_OFFSET = 150f;
    private static final float PLAYER_LIST_ITEM_HEIGHT = 50f;
    private static final float PLAYER_LIST_TOKEN_SIZE = 40f;

    private static final float NUM_PLAYER_BUTTON_WIDTH = 100f;
    private static final float NUM_PLAYER_BUTTON_HEIGHT = 70f;
    private boolean[] isNumPlayerButtonHovered = new boolean[3];

    public GameSetupScreen(StartGame game) {
        this.game = game;
        configuredPlayers = new ArrayList<>();
        try {
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            glyphLayout = new GlyphLayout();
            friendButtonTexture = new Texture(Gdx.files.internal("vs.jpg"));
            aiButtonTexture = new Texture(Gdx.files.internal("ai.jpg"));
            tokenTextures = new Texture[]{
                new Texture(Gdx.files.internal("car.png")),
                new Texture(Gdx.files.internal("dog.png")),
                new Texture(Gdx.files.internal("iron.png")),
                new Texture(Gdx.files.internal("xe_cut_kit.png"))
            };
            backgroundTexture = new Texture(Gdx.files.internal("background.jpg"));

            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 32;
            parameter.shadowOffsetX = 2;
            parameter.shadowOffsetY = 2;
            parameter.shadowColor = new Color(0, 0, 0, 0.7f);
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "©áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđĐ";
            font = generator.generateFont(parameter);

            parameter.size = 26;
            smallFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("GameSetupScreen", "Error initializing: " + e.getMessage(), e);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
        if (batch == null || font == null || shapeRenderer == null) return;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        int mouseX = Gdx.input.getX();
        int mouseY = (int) (screenHeight - Gdx.input.getY());

        batch.begin();
        if (backgroundTexture != null)
            batch.draw(backgroundTexture, 0, 0, screenWidth, screenHeight);
        batch.end();

        switch (currentState) {
            case SELECT_MODE:
                renderSelectModeUI(mouseX, mouseY, screenWidth, screenHeight);
                break;
            case SELECT_NUM_PLAYERS:
                renderSelectNumPlayersUI(mouseX, mouseY, screenWidth, screenHeight);
                break;
            case PLAYER_INPUT:
                renderPlayerInputUI(mouseX, mouseY, screenWidth, screenHeight);
                break;
        }
        renderConfiguredPlayersList(screenWidth, screenHeight);

        if (Gdx.input.justTouched()) {
            handleTouchInput(mouseX, mouseY, screenWidth, screenHeight);
        }
    }

    private void renderSelectModeUI(int mouseX, int mouseY, float screenWidth, float screenHeight) {
        float modeButtonY = screenHeight * 0.6f - MODE_BUTTON_HEIGHT / 2;
        float friendButtonX = (screenWidth - (MODE_BUTTON_WIDTH * 2 + GAP)) / 2;
        float aiButtonX = friendButtonX + MODE_BUTTON_WIDTH + GAP;

        isFriendButtonHovered = isHovered(mouseX, mouseY, friendButtonX, modeButtonY, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT);
        isAIButtonHovered = isHovered(mouseX, mouseY, aiButtonX, modeButtonY, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT);

        batch.begin();
        batch.setColor(isFriendButtonHovered || selectedMode == 0 ? Color.WHITE : Color.GRAY);
        if (friendButtonTexture != null) batch.draw(friendButtonTexture, friendButtonX, modeButtonY, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT);
        // drawTextCentered("Chơi với bạn", friendButtonX + MODE_BUTTON_WIDTH / 2, modeButtonY + MODE_BUTTON_HEIGHT / 2, Color.WHITE, font);

        batch.setColor(isAIButtonHovered || selectedMode == 1 ? Color.WHITE : Color.GRAY);
        if (aiButtonTexture != null) batch.draw(aiButtonTexture, aiButtonX, modeButtonY, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT);
        // drawTextCentered("Chơi với Máy", aiButtonX + MODE_BUTTON_WIDTH / 2, modeButtonY + MODE_BUTTON_HEIGHT / 2, Color.WHITE, font);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void renderSelectNumPlayersUI(int mouseX, int mouseY, float screenWidth, float screenHeight) {
        float titleY = screenHeight * 0.8f;
        batch.begin();
        drawTextCentered("Chọn số người chơi:", screenWidth / 2, titleY, Color.WHITE, font);
        batch.end();

        float numButtonTotalWidth = NUM_PLAYER_BUTTON_WIDTH * 3 + GAP * 2;
        float numButtonStartX = (screenWidth - numButtonTotalWidth) / 2;
        float numButtonY = titleY - 100f - NUM_PLAYER_BUTTON_HEIGHT / 2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 3; i++) {
            float x = numButtonStartX + i * (NUM_PLAYER_BUTTON_WIDTH + GAP);
            isNumPlayerButtonHovered[i] = isHovered(mouseX, mouseY, x, numButtonY, NUM_PLAYER_BUTTON_WIDTH, NUM_PLAYER_BUTTON_HEIGHT);
            drawButtonShape(x, numButtonY, NUM_PLAYER_BUTTON_WIDTH, NUM_PLAYER_BUTTON_HEIGHT, isNumPlayerButtonHovered[i]);
        }
        shapeRenderer.end();

        batch.begin();
        for (int i = 0; i < 3; i++) {
            float x = numButtonStartX + i * (NUM_PLAYER_BUTTON_WIDTH + GAP);
            drawTextCentered(String.valueOf(i + 2), x + NUM_PLAYER_BUTTON_WIDTH / 2, numButtonY + NUM_PLAYER_BUTTON_HEIGHT / 2, Color.WHITE, font);
        }
        batch.end();
    }

    private void renderPlayerInputUI(int mouseX, int mouseY, float screenWidth, float screenHeight) {
        float currentY = screenHeight * 0.85f;

        batch.begin();
        drawTextCentered("Cài đặt cho Người chơi " + (currentPlayerInputIndex + 1) + "/" + numPlayersToSetup, screenWidth / 2, currentY, Color.WHITE, font);
        currentY -= 80f;
        batch.end();

        float inputX = (screenWidth - INPUT_BOX_WIDTH) / 2;
        isInputBoxHovered = isHovered(mouseX, mouseY, inputX, currentY - INPUT_BOX_HEIGHT, INPUT_BOX_WIDTH, INPUT_BOX_HEIGHT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawRoundedRect(inputX, currentY - INPUT_BOX_HEIGHT, INPUT_BOX_WIDTH, INPUT_BOX_HEIGHT, new Color(0.9f, 0.9f, 0.9f, 1f), isInputBoxHovered);
        shapeRenderer.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
        shapeRenderer.rect(inputX, currentY - INPUT_BOX_HEIGHT, INPUT_BOX_WIDTH, 2f);
        shapeRenderer.end();

        batch.begin();
        String nameToDisplay = currentInputPlayerName.isEmpty() ? "Nhập tên..." : currentInputPlayerName;
        drawTextCentered(nameToDisplay, inputX + INPUT_BOX_WIDTH / 2, currentY - INPUT_BOX_HEIGHT / 2, currentInputPlayerName.isEmpty() ? Color.GRAY : Color.BLACK, font);
        currentY -= (INPUT_BOX_HEIGHT + 50f);

        drawTextCentered("Chọn quân cờ:", screenWidth / 2, currentY, Color.WHITE, font);
        float tokenAreaY = currentY - (TOKEN_SIZE + 30f); // Y của hàng token

        float totalTokenWidth = tokenTextures.length * TOKEN_SIZE + (tokenTextures.length - 1) * TOKEN_GAP;
        float tokenStartX = (screenWidth - totalTokenWidth) / 2;

        for (int i = 0; i < tokenTextures.length; i++) {
            float tokenX = tokenStartX + i * (TOKEN_SIZE + TOKEN_GAP);
            boolean isTokenTakenByOthers = false;
            for (PlayerSetupInfo info : configuredPlayers) {
                if (info.selectedTokenIndex == i) {
                    isTokenTakenByOthers = true;
                    break;
                }
            }

            // Vẽ token
            batch.setColor(isTokenTakenByOthers ? 0.5f : 1f, isTokenTakenByOthers ? 0.5f : 1f, isTokenTakenByOthers ? 0.5f : 1f, 1f);
            if (tokenTextures[i] != null) batch.draw(tokenTextures[i], tokenX, tokenAreaY, TOKEN_SIZE, TOKEN_SIZE);

            if (i == currentSelectedToken && !isTokenTakenByOthers) {
                batch.setColor(1f, 1f, 0f, 0.4f);
                batch.draw(tokenTextures[i], tokenX - 2, tokenAreaY - 2, TOKEN_SIZE + 4, TOKEN_SIZE + 4);
            }
        }
        batch.setColor(Color.WHITE);
        currentY = tokenAreaY - (80f);

        String actionButtonText;
        boolean isCurrentActionButtonHovered;
        if (currentPlayerInputIndex < numPlayersToSetup - 1) {
            actionButtonText = "Lưu & Tiếp tục";
            isSaveAndNextButtonHovered = isHovered(mouseX, mouseY, (screenWidth - ACTION_BUTTON_WIDTH) / 2, currentY - ACTION_BUTTON_HEIGHT, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT);
            isCurrentActionButtonHovered = isSaveAndNextButtonHovered;
        } else {
            actionButtonText = "Lưu & Bắt đầu";
            isStartButtonHovered = isHovered(mouseX, mouseY, (screenWidth - ACTION_BUTTON_WIDTH) / 2, currentY - ACTION_BUTTON_HEIGHT, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT);
            isCurrentActionButtonHovered = isStartButtonHovered;
        }
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        boolean canProceed = !currentInputPlayerName.isEmpty() && currentSelectedToken != -1;
        if (currentPlayerInputIndex == numPlayersToSetup -1) {
            canProceed = canProceed && (configuredPlayers.size() == numPlayersToSetup -1) ;
        }
        drawButtonShape((screenWidth - ACTION_BUTTON_WIDTH) / 2, currentY - ACTION_BUTTON_HEIGHT, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT, isCurrentActionButtonHovered && canProceed);
        shapeRenderer.end();

        batch.begin();
        drawTextCentered(actionButtonText, screenWidth / 2, currentY - ACTION_BUTTON_HEIGHT / 2, Color.WHITE, font);
        batch.end();
    }

    private void renderConfiguredPlayersList(float screenWidth, float screenHeight) {
        batch.begin();
        smallFont.setColor(Color.LIGHT_GRAY);
        float listY = screenHeight - PLAYER_LIST_START_Y_OFFSET;
        glyphLayout.setText(font, "Người chơi đã đăng ký:");
        font.draw(batch, glyphLayout, PLAYER_LIST_X_OFFSET, listY + glyphLayout.height);
        listY -= (glyphLayout.height + GAP / 2);

        for (int i = 0; i < configuredPlayers.size(); i++) {
            PlayerSetupInfo info = configuredPlayers.get(i);
            String playerText = (i + 1) + ". " + info.name;
            glyphLayout.setText(smallFont, playerText);
            smallFont.draw(batch, glyphLayout, PLAYER_LIST_X_OFFSET, listY + glyphLayout.height);

            if (info.tokenTexture != null) {
                batch.draw(info.tokenTexture, PLAYER_LIST_X_OFFSET + glyphLayout.width + TOKEN_GAP, listY - PLAYER_LIST_TOKEN_SIZE / 2 + glyphLayout.height / 2, PLAYER_LIST_TOKEN_SIZE, PLAYER_LIST_TOKEN_SIZE);
            }
            listY -= PLAYER_LIST_ITEM_HEIGHT;
            if (listY < 50) break;
        }
        batch.end();
    }

    private void handleTouchInput(int mouseX, int mouseY, float screenWidth, float screenHeight) {
        if (currentState == SetupState.SELECT_MODE) {
            float modeButtonY = screenHeight * 0.6f - MODE_BUTTON_HEIGHT / 2;
            float friendButtonX = (screenWidth - (MODE_BUTTON_WIDTH * 2 + GAP)) / 2;
            float aiButtonX = friendButtonX + MODE_BUTTON_WIDTH + GAP;

            if (isHovered(mouseX, mouseY, friendButtonX, modeButtonY, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT)) {
                selectedMode = 0;
                currentState = SetupState.SELECT_NUM_PLAYERS;
                configuredPlayers.clear();
                currentPlayerInputIndex = 0;
            } else if (isHovered(mouseX, mouseY, aiButtonX, modeButtonY, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT)) {
                selectedMode = 1;
                Gdx.app.log("GameSetup", "Chơi với Máy. (Cần logic khởi tạo CPU & chuyển màn hình)");
            }
        } else if (currentState == SetupState.SELECT_NUM_PLAYERS) {
            float titleY = screenHeight * 0.8f;
            float numButtonTotalWidth = NUM_PLAYER_BUTTON_WIDTH * 3 + GAP * 2;
            float numButtonStartX = (screenWidth - numButtonTotalWidth) / 2;
            float numButtonY = titleY - 100f - NUM_PLAYER_BUTTON_HEIGHT / 2;
            for (int i = 0; i < 3; i++) {
                float x = numButtonStartX + i * (NUM_PLAYER_BUTTON_WIDTH + GAP);
                if (isHovered(mouseX, mouseY, x, numButtonY, NUM_PLAYER_BUTTON_WIDTH, NUM_PLAYER_BUTTON_HEIGHT)) {
                    numPlayersToSetup = i + 2;
                    currentState = SetupState.PLAYER_INPUT;
                    resetCurrentPlayerInput();
                    break;
                }
            }
        } else if (currentState == SetupState.PLAYER_INPUT) {
            float currentY_check = screenHeight * 0.85f; // Tính lại Y để kiểm tra click
            currentY_check -= 80f;
            float inputX = (screenWidth - INPUT_BOX_WIDTH) / 2;
            float inputBoxActualY = currentY_check - INPUT_BOX_HEIGHT;

            if (isHovered(mouseX, mouseY, inputX, inputBoxActualY, INPUT_BOX_WIDTH, INPUT_BOX_HEIGHT)) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override public void input(String text) { if (text.length() <= 20) currentInputPlayerName = text.trim(); }
                    @Override public void canceled() { }
                }, "Nhập tên người chơi " + (currentPlayerInputIndex + 1), currentInputPlayerName, "Tối đa 20 ký tự");
            }

            currentY_check -= (INPUT_BOX_HEIGHT + 50f);
            float tokenAreaClickY = currentY_check - (TOKEN_SIZE + 30f); // Y của hàng token để check click

            float totalTokenWidth = tokenTextures.length * TOKEN_SIZE + (tokenTextures.length - 1) * TOKEN_GAP;
            float tokenStartX = (screenWidth - totalTokenWidth) / 2;
            for (int i = 0; i < tokenTextures.length; i++) {
                float tokenX = tokenStartX + i * (TOKEN_SIZE + TOKEN_GAP);
                if (isHovered(mouseX, mouseY, tokenX, tokenAreaClickY, TOKEN_SIZE, TOKEN_SIZE)) {
                    boolean tokenTakenByOthers = false;
                    for (PlayerSetupInfo info : configuredPlayers) {
                        if (info.selectedTokenIndex == i) {
                            tokenTakenByOthers = true;
                            break;
                        }
                    }
                    if (!tokenTakenByOthers) {
                        currentSelectedToken = i;
                    }
                    break;
                }
            }

            float actionButtonAreaY = tokenAreaClickY - (80f);
            float actionButtonActualY = actionButtonAreaY - ACTION_BUTTON_HEIGHT;
            boolean canProceedCurrent = !currentInputPlayerName.isEmpty() && currentSelectedToken != -1;

            if (isHovered(mouseX, mouseY, (screenWidth - ACTION_BUTTON_WIDTH) / 2, actionButtonActualY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT) && canProceedCurrent) {
                if (currentPlayerInputIndex < numPlayersToSetup - 1) { // Lưu & Tiếp
                    saveCurrentPlayerInfo();
                    if (configuredPlayers.size() == currentPlayerInputIndex + 1) {
                        currentPlayerInputIndex++;
                        resetCurrentPlayerInput();
                    }
                } else { // Lưu & Bắt đầu (người chơi cuối cùng)
                    saveCurrentPlayerInfo();
                    if (configuredPlayers.size() == numPlayersToSetup) {
                        Gdx.app.log("GameSetup", "Bắt đầu game với " + numPlayersToSetup + " người chơi.");
                        if (game != null) {
                            // Truyền dữ liệu cho game chính
                            StartGame.configuredPlayersData = new ArrayList<>(configuredPlayers);
                            StartGame.configuredNumPlayersData = numPlayersToSetup;
                            StartGame.playWithAIData = (selectedMode == 1);
                            game.setScreen(new MainGameScreen(game));
                        }
                    } else {
                        Gdx.app.log("GameSetup", "Lưu người chơi cuối thất bại hoặc số lượng không khớp. Đã lưu: " + configuredPlayers.size() + ", Cần: " + numPlayersToSetup);
                    }
                }
            }
        }
    }

    private void saveCurrentPlayerInfo() {
        if (currentInputPlayerName.isEmpty() || currentSelectedToken == -1) {
            Gdx.app.log("GameSetup", "Tên hoặc token chưa được chọn.");
            return;
        }
        for (PlayerSetupInfo info : configuredPlayers) {
            if (info.selectedTokenIndex == currentSelectedToken) {
                Gdx.app.log("GameSetup", "Token " + currentSelectedToken + " đã bị người chơi '" + info.name + "' chọn. Vui lòng chọn token khác.");
                return;
            }
        }
        configuredPlayers.add(new PlayerSetupInfo(currentInputPlayerName, currentSelectedToken, tokenTextures[currentSelectedToken]));
        Gdx.app.log("GameSetup", "Đã lưu: " + currentInputPlayerName + ", Token: " + currentSelectedToken + ". Tổng số đã lưu: " + configuredPlayers.size());
    }

    private void resetCurrentPlayerInput() {
        currentInputPlayerName = "";
        currentSelectedToken = -1;
    }

    private boolean isHovered(int x, int y, float posX, float posY, float width, float height) {
        return x >= posX && x <= posX + width && y >= posY && y <= posY + height;
    }

    private void drawButtonShape(float x, float y, float width, float height, boolean isHovered) {
        shapeRenderer.setColor(new Color(0, 0, 0, 0.3f));
        shapeRenderer.rect(x + SHADOW_OFFSET, y - SHADOW_OFFSET, width, height);
        shapeRenderer.setColor(isHovered ? new Color(0.3f, 0.6f, 0.9f, 1f) : new Color(0.2f, 0.5f, 0.8f, 1f));
        shapeRenderer.rect(x, y, width, height);
    }

    private void drawRoundedRect(float x, float y, float width, float height, Color color, boolean isHovered) {
        shapeRenderer.setColor(isHovered ? Color.LIGHT_GRAY.cpy().lerp(color, 0.5f) : color);
        shapeRenderer.rect(x, y, width, height);
    }

    private void drawTextCentered(String text, float targetX, float targetY, Color color, BitmapFont fontToUse) {
        fontToUse.setColor(color);
        glyphLayout.setText(fontToUse, text);
        float textX = targetX - glyphLayout.width / 2;
        float textY = targetY + glyphLayout.height / 2;
        fontToUse.draw(batch, glyphLayout, textX, textY);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (friendButtonTexture != null) friendButtonTexture.dispose();
        if (aiButtonTexture != null) aiButtonTexture.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (tokenTextures != null) for (Texture t : tokenTextures) if (t != null) t.dispose();
    }

    @Override public void show() {
        currentState = SetupState.SELECT_MODE;
        selectedMode = -1;
        numPlayersToSetup = 0;
        configuredPlayers.clear();
        currentPlayerInputIndex = 0;
        resetCurrentPlayerInput();
        Gdx.input.setInputProcessor(Gdx.input.getInputProcessor());
    }
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
