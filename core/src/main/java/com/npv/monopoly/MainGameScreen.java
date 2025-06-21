package com.npv.monopoly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGameScreen implements Screen {
    private final StartGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont smallFont;
    private GlyphLayout glyphLayout;
    private Texture boardTexture;
    private Viewport viewport;

    private Logic gameLogic;

    private Card currentCard;
    private static final float WORLD_WIDTH = 1120;
    private static final float WORLD_HEIGHT = 800;
    private static final float EXTRA_WIDTH = 250f;
    private static final float TOTAL_WIDTH = WORLD_WIDTH + EXTRA_WIDTH;

    private static final float PLAYER_BOX_WIDTH = 240f;
    private static final float PLAYER_BOX_HEIGHT = 100f;
    private static final float PLAYER_BOX_X = WORLD_WIDTH + 250f;
    private float playerListStartY;
    private static final float PLAYER_BOX_GAP = 10f;

    private static final float CONTROL_AREA_WIDTH = 200f;
    private static final float CONTROL_AREA_X = WORLD_WIDTH + 10f;
    private static final float DICE_DISPLAY_HEIGHT = 80f;
    private static final float DICE_DISPLAY_Y_TOP = WORLD_HEIGHT - 20f;

    private static final float BUTTON_WIDTH = 150f;
    private static final float BUTTON_HEIGHT = 50f;
    private static final float BUTTON_GAP = 10f;
    private static final float BUTTON_AREA_X = WORLD_WIDTH + 30f;
    private float buttonAreaStartY;

    private static final float REPLAY_BUTTON_Y = WORLD_HEIGHT / 2 - BUTTON_HEIGHT / 2;

    private List<Texture> playerTokenTextures;
    private static final float TOKEN_ON_BOARD_SIZE = 40f;
    private static final float TOKEN_ON_PROPERTY_SIZE = 30f;

    private Texture[] diceTextures;

    private static final Map<Integer, com.npv.monopoly.Board.Point> boardSquareCoordinates = new HashMap<>();
    static {
        if (Board.POSITIONS_XY.length != 40) {
            Gdx.app.error("MainGameScreen", "POSITIONS_XY length is " + Board.POSITIONS_XY.length + ", expected 40");
        }
        for (int i = 0; i < Math.min(Board.POSITIONS_XY.length, 40); i++) {
            boardSquareCoordinates.put(i, new com.npv.monopoly.Board.Point(Board.POSITIONS_XY[i].x, Board.POSITIONS_XY[i].y));
        }
    }

    private float messageTimer = 0f;
    private static final float MESSAGE_DURATION = 3f;

    public MainGameScreen(StartGame game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        glyphLayout = new GlyphLayout();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
        if (generator == null) {
            Gdx.app.error("MainGameScreen", "Font generator failed to initialize for Roboto-Regular.ttf");
        }
        FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        parameter.shadowColor = new Color(0, 0, 0, 0.7f);
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "©áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđĐ";
        font = generator.generateFont(parameter);
        if (font == null) {
            Gdx.app.error("MainGameScreen", "Failed to generate font with size 32");
        }

        parameter.size = 26;
        smallFont = generator.generateFont(parameter);
        if (smallFont == null) {
            Gdx.app.error("MainGameScreen", "Failed to generate small font with size 26");
        }
        generator.dispose();

        boardTexture = loadTexture("map40.png");
        if (boardTexture == null) {
            Gdx.app.error("MainGameScreen", "Failed to load board texture: map40.png");
        }

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(TOTAL_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);

        gameLogic = new Logic();

        if (StartGame.configuredPlayersData == null || StartGame.configuredPlayersData.isEmpty() || StartGame.configuredNumPlayersData < 2 || StartGame.configuredNumPlayersData > 4) {
            Gdx.app.error("MainGameScreen", "Invalid player data: configuredPlayersData is " + (StartGame.configuredPlayersData == null ? "null" : "size " + StartGame.configuredPlayersData.size()) + ", configuredNumPlayersData is " + StartGame.configuredNumPlayersData);
            throw new IllegalStateException("Cannot start game: No valid player data from GameSetupScreen.");
        }

        int humanPlayerCount = StartGame.configuredPlayersData.size();
        if (StartGame.playWithAIData) {
            if (humanPlayerCount < 1) {
                Gdx.app.error("MainGameScreen", "AI mode requires at least 1 human player.");
                throw new IllegalStateException("Cannot start game: No human player data for AI mode.");
            }
            if (humanPlayerCount > StartGame.configuredNumPlayersData) {
                Gdx.app.error("MainGameScreen", "Too many human players (" + humanPlayerCount + ") for configured total (" + StartGame.configuredNumPlayersData + ").");
                throw new IllegalStateException("Cannot start game: Invalid player count.");
            }
        } else {
            if (humanPlayerCount != StartGame.configuredNumPlayersData) {
                Gdx.app.error("MainGameScreen", "Player count mismatch: configuredPlayersData has " + humanPlayerCount + ", expected " + StartGame.configuredNumPlayersData);
                throw new IllegalStateException("Cannot start game: Player count mismatch.");
            }
        }

        gameLogic.initializeGame(
            StartGame.configuredPlayersData.toArray(new GameSetupScreen.PlayerSetupInfo[0]),
            StartGame.playWithAIData
        );

        playerTokenTextures = new ArrayList<>();
        String[] tokenPaths = {"car.png", "dog.png", "iron.png", "xe_cut_kit.png"};
        for (GameSetupScreen.PlayerSetupInfo setup : StartGame.configuredPlayersData) {
            if (setup.selectedTokenIndex >= 0 && setup.selectedTokenIndex < tokenPaths.length) {
                Texture tokenTexture = loadTexture(tokenPaths[setup.selectedTokenIndex]);
                if (tokenTexture == null) {
                    Gdx.app.error("MainGameScreen", "Error loading token texture: " + tokenPaths[setup.selectedTokenIndex]);
                }
                playerTokenTextures.add(tokenTexture);
            } else {
                Gdx.app.error("MainGameScreen", "Invalid token index: " + setup.selectedTokenIndex + " for player: " + setup.name);
                playerTokenTextures.add(null);
            }
        }

        if (StartGame.playWithAIData) {
            int aiCount = StartGame.configuredNumPlayersData - humanPlayerCount;
            for (int i = 0; i < aiCount; i++) {
                int aiTokenIndex = (i + humanPlayerCount) % tokenPaths.length;
                while (isTokenIndexUsed(aiTokenIndex, StartGame.configuredPlayersData)) {
                    aiTokenIndex = (aiTokenIndex + 1) % tokenPaths.length;
                }
                Texture tokenTexture = loadTexture(tokenPaths[aiTokenIndex]);
                if (tokenTexture == null) {
                    Gdx.app.error("MainGameScreen", "Error loading AI token texture: " + tokenPaths[aiTokenIndex]);
                }
                playerTokenTextures.add(tokenTexture);
            }
        }

        diceTextures = new Texture[6];
        try {
            for (int i = 0; i < 6; i++) {
                diceTextures[i] = loadTexture("dice_" + (i + 1) + ".jpg");
                if (diceTextures[i] == null) {
                    Gdx.app.error("MainGameScreen", "Error loading dice texture: dice_" + (i + 1) + ".jpg");
                }
            }
        } catch (Exception e) {
            Gdx.app.error("MainGameScreen", "Exception loading dice textures: " + e.getMessage(), e);
            for (int i = 0; i < 6; i++) {
                if (diceTextures[i] != null) diceTextures[i].dispose();
            }
            diceTextures = null;
        }

        setupUI();
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == com.badlogic.gdx.Input.Buttons.LEFT) {
                    Vector3 worldCoordinates = camera.unproject(new Vector3(screenX, screenY, 0));
                    handleInput(worldCoordinates.x, worldCoordinates.y);
                    return true;
                }
                return false;
            }
        });
    }

    private Texture loadTexture(String fileName) {
        try {
            Texture texture = new Texture(Gdx.files.internal(fileName));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return texture;
        } catch (Exception e) {
            Gdx.app.error("MainGameScreen", "Failed to load texture: " + fileName + ", Error: " + e.getMessage());
            return null;
        }
    }

    private void setupUI() {
        if (StartGame.configuredPlayersData != null && !StartGame.configuredPlayersData.isEmpty()) {
            float totalHeight = StartGame.configuredPlayersData.size() * (PLAYER_BOX_HEIGHT + PLAYER_BOX_GAP) - PLAYER_BOX_GAP;
            playerListStartY = (WORLD_HEIGHT - totalHeight) / 2 + 170f;
            if (playerListStartY < 0) {
                playerListStartY = 10f;
            }
        } else {
            playerListStartY = WORLD_HEIGHT + 10f;
            if (playerListStartY < 0) {
                playerListStartY = 10f;
            }
        }
        buttonAreaStartY = WORLD_HEIGHT - 250f;
    }

    private void handleInput(float x, float y) {
        if (gameLogic == null) return;

        Logic.GamePhase phase = gameLogic.getCurrentPhase();

        if (phase == Logic.GamePhase.HANDLING_CARD_EFFECT) {
            gameLogic.clearCurrentCard();
            gameLogic.applyCardEffect(currentCard);
            return;
        }

        if (phase == Logic.GamePhase.GAME_OVER) {
            float replayButtonX = WORLD_WIDTH / 2 - BUTTON_WIDTH / 2;
            if (x >= replayButtonX && x <= replayButtonX + BUTTON_WIDTH &&
                y >= REPLAY_BUTTON_Y && y <= REPLAY_BUTTON_Y + BUTTON_HEIGHT) {
                StartGame.configuredPlayersData = null;
                StartGame.configuredNumPlayersData = 0;
                StartGame.playWithAIData = false;
                game.setScreen(new GameSetupScreen(game));
                return;
            }
        }

        if (x >= BUTTON_AREA_X && x <= BUTTON_AREA_X + BUTTON_WIDTH) {
            for (int i = 0; i < 6; i++) {
                float buttonY = buttonAreaStartY - i * (BUTTON_HEIGHT + BUTTON_GAP);
                if (y >= buttonY && y <= buttonY + BUTTON_HEIGHT) {
                    switch (i) {
                        case 0: // Roll
                            if (phase == Logic.GamePhase.PLAYER_ROLLING &&
                                (gameLogic.getCurrentPlayer().isInJail() ||
                                    !gameLogic.getCurrentPlayer().isInJail())) {
                                gameLogic.playerRequestsRollDice();
                            }
                            break;
                        case 1: // Buy
                            if (phase == Logic.GamePhase.AWAITING_PURCHASE_DECISION) {
                                gameLogic.playerDecidesToBuyProperty();
                            }
                            break;
                        case 2: // End Turn
                            if (phase == Logic.GamePhase.MANAGING_PROPERTIES ||
                                phase == Logic.GamePhase.AWAITING_PLAYER_ACTION ||
                                phase == Logic.GamePhase.AWAITING_JAIL_DECISION) {
                                gameLogic.endCurrentPlayerTurn();
                            }
                            break;
                        case 3: // Pay Bail
                            if (phase == Logic.GamePhase.PLAYER_ROLLING &&
                                gameLogic.getCurrentPlayer().isInJail() &&
                                gameLogic.getCurrentPlayer().getMoney() >= Logic.BAIL_AMOUNT) {
                                gameLogic.playerDecidesToPayToLeaveJail();
                            }
                            break;
                        case 4: // Use Jail Card
                            if (phase == Logic.GamePhase.PLAYER_ROLLING &&
                                gameLogic.getCurrentPlayer().isInJail() &&
                                gameLogic.getCurrentPlayer().getNumGetOutOfJailFreeCards() > 0) {
                                gameLogic.playerDecidesToUseJailCard();
                            }
                            break;
                        case 5: // Reject
                            if (phase == Logic.GamePhase.AWAITING_PURCHASE_DECISION) {
                                gameLogic.playerDeclinesToBuyProperty();
                            }
                            break;
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.8f, 0.8f, 0.8f, 1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        messageTimer += delta;
        if (messageTimer > MESSAGE_DURATION) {
            messageTimer = 0f;
        }

        batch.begin();
        if (boardTexture != null) {
            batch.draw(boardTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        } else {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            shapeRenderer.end();
            batch.begin();
        }
        batch.end();

        if (gameLogic != null && gameLogic.getAllPlayers() != null && gameLogic.getBoard() != null) {
            currentCard = gameLogic.getCurrentCard();
            renderPlayerList();
            renderDice();
            renderPlayerTokens();
            renderGameMessage();
            renderCard();
            renderButtons();

            if (gameLogic.getCurrentPhase() == Logic.GamePhase.CPU_THINKING) {
                gameLogic.handleCPUDecisions();
            }
        } else {
            renderGameMessage();
        }
    }

    private void renderPlayerList() {
        if (gameLogic == null || gameLogic.getAllPlayers() == null) return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        List<Player> allPlayers = (List<Player>) gameLogic.getAllPlayers();
        float currentY = playerListStartY;

        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);
            if (player == null) continue;

            shapeRenderer.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));
            shapeRenderer.rect(PLAYER_BOX_X, currentY, PLAYER_BOX_WIDTH, PLAYER_BOX_HEIGHT);

            if (player == gameLogic.getCurrentPlayer()) {
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.rectLine(PLAYER_BOX_X, currentY, PLAYER_BOX_X + PLAYER_BOX_WIDTH, currentY, 3f);
                shapeRenderer.rectLine(PLAYER_BOX_X, currentY + PLAYER_BOX_HEIGHT, PLAYER_BOX_X + PLAYER_BOX_WIDTH, currentY + PLAYER_BOX_HEIGHT, 3f);
                shapeRenderer.rectLine(PLAYER_BOX_X, currentY, PLAYER_BOX_X, currentY + PLAYER_BOX_HEIGHT, 3f);
                shapeRenderer.rectLine(PLAYER_BOX_X + PLAYER_BOX_WIDTH, currentY, PLAYER_BOX_X + PLAYER_BOX_WIDTH, currentY + PLAYER_BOX_HEIGHT, 3f);
            }
            currentY -= PLAYER_BOX_HEIGHT + PLAYER_BOX_GAP;
        }
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.BLACK);
        currentY = playerListStartY;

        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);
            if (player == null) continue;

            String playerName = player.name() != null ? player.name() : "Player";
            glyphLayout.setText(font, playerName);
            float nameTextX = PLAYER_BOX_X + 10f;
            float nameTextY = currentY + (PLAYER_BOX_HEIGHT * 2 / 3) + glyphLayout.height / 2;
            font.draw(batch, glyphLayout, nameTextX, nameTextY);

            String moneyText = "$" + player.getMoney();
            glyphLayout.setText(font, moneyText);
            float moneyTextX = PLAYER_BOX_X + 10f;
            float moneyTextY = currentY + (PLAYER_BOX_HEIGHT / 3) + glyphLayout.height / 2;
            font.draw(batch, glyphLayout, moneyTextX, moneyTextY);

            if (i < playerTokenTextures.size() && playerTokenTextures.get(i) != null) {
                float tokenSize = PLAYER_BOX_HEIGHT * 0.6f;
                float tokenX = PLAYER_BOX_X + (PLAYER_BOX_WIDTH * 2 / 3);
                float tokenY = currentY + (PLAYER_BOX_HEIGHT - tokenSize) / 2;
                batch.draw(playerTokenTextures.get(i), tokenX, tokenY, tokenSize, tokenSize);
            }
            currentY -= PLAYER_BOX_HEIGHT + PLAYER_BOX_GAP;
        }
        batch.end();
    }

    private void renderDice() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));
        shapeRenderer.rect(CONTROL_AREA_X, DICE_DISPLAY_Y_TOP - DICE_DISPLAY_HEIGHT, CONTROL_AREA_WIDTH, DICE_DISPLAY_HEIGHT);
        shapeRenderer.end();

        batch.begin();
        int diceValue = gameLogic.getLastDiceRollValue();

        if (diceTextures != null && diceTextures.length == 6) {
            int dice1Result = (diceValue > 0) ? (diceValue + 1) / 2 : 1;
            int dice2Result = (diceValue > 0) ? diceValue - dice1Result : 1;

            dice1Result = Math.max(1, Math.min(6, dice1Result));
            dice2Result = Math.max(1, Math.min(6, dice2Result));

            float singleDiceSize = DICE_DISPLAY_HEIGHT * 1.2f;
            float diceGap = 10f;
            float totalDiceWidth = singleDiceSize * 2 + diceGap;
            float dice1X = CONTROL_AREA_X + (CONTROL_AREA_WIDTH - totalDiceWidth) / 2f;
            float dice1Y = DICE_DISPLAY_Y_TOP - DICE_DISPLAY_HEIGHT / 2f - singleDiceSize / 2f;
            float dice2X = dice1X + singleDiceSize + diceGap;
            float dice2Y = dice1Y;

            if (dice1Result >= 1 && dice1Result <= 6 && diceTextures[dice1Result - 1] != null) {
                batch.draw(diceTextures[dice1Result - 1], dice1X, dice1Y, singleDiceSize, singleDiceSize);
            }
            if (dice2Result >= 1 && dice2Result <= 6 && diceTextures[dice2Result - 1] != null) {
                batch.draw(diceTextures[dice2Result - 1], dice2X, dice2Y, singleDiceSize, singleDiceSize);
            }
        } else {
            font.setColor(Color.WHITE);
            String diceText = diceValue > 0 ? "Dice: " + diceValue + (gameLogic.wasLastRollDouble() ? " (Double!)" : "") : "Not Rolled";
            glyphLayout.setText(font, diceText);
            float textX = CONTROL_AREA_X + CONTROL_AREA_WIDTH / 2 - glyphLayout.width / 2;
            float textY = DICE_DISPLAY_Y_TOP - DICE_DISPLAY_HEIGHT / 2 + glyphLayout.height / 2;
            font.draw(batch, glyphLayout, textX, textY);
        }
        batch.end();
    }

    private void renderPlayerTokens() {
        if (gameLogic == null || gameLogic.getAllPlayers() == null || gameLogic.getBoard() == null) return;

        batch.begin();
        List<Player> allPlayers = (List<Player>) gameLogic.getAllPlayers();
        Board board = gameLogic.getBoard();

        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);
            if (player == null) continue;

            Texture tokenTexture = (i < playerTokenTextures.size()) ? playerTokenTextures.get(i) : null;

            if (tokenTexture != null) {
                int currentSquareIndex = player.getCurrentBoardIndex();
                com.npv.monopoly.Board.Point squarePoint = boardSquareCoordinates.get(currentSquareIndex);

                if (squarePoint != null) {
                    float squareX = squarePoint.x - 10f;
                    float squareY = squarePoint.y - 10f;

                    float drawX = squareX - 10f;
                    float drawY = squareY - 10f;

                    int playerCountOnSquare = 0;
                    for (Player p : allPlayers) {
                        if (p.getCurrentBoardIndex() == currentSquareIndex && p != player) playerCountOnSquare++;
                    }
                    float offsetX = (playerCountOnSquare % 2) * (TOKEN_ON_BOARD_SIZE * 0.2f);
                    float offsetY = (playerCountOnSquare / 2) * (TOKEN_ON_BOARD_SIZE * 0.2f);

                    drawX += offsetX;
                    drawY += offsetY;

                    batch.draw(tokenTexture, drawX, drawY, TOKEN_ON_BOARD_SIZE, TOKEN_ON_BOARD_SIZE);
                } else {
                    Gdx.app.error("MainGameScreen", "Missing coordinates for board index: " + currentSquareIndex + " (Player: " + player.name() + ")");
                }
            }
        }

        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);
            if (player == null) continue;

            Texture tokenTexture = (i < playerTokenTextures.size()) ? playerTokenTextures.get(i) : null;
            if (tokenTexture == null) continue;

            List<Square> ownedProperties = (List<Square>) player.getProperties();
            if (ownedProperties == null) continue;

            for (Square square : ownedProperties) {
                if (!(square instanceof Property) && !(square instanceof Railroad)) continue;

                int propertyIndex = board.getIndexForSquare(square);
                com.npv.monopoly.Board.Point squarePoint = boardSquareCoordinates.get(propertyIndex);
                if (squarePoint == null) continue;

                float squareX = squarePoint.x - 10f;
                float squareY = squarePoint.y - 10f;

                float propertyY = squareY + 80f - TOKEN_ON_PROPERTY_SIZE;
                float propertyX = squareX + (80f - TOKEN_ON_PROPERTY_SIZE) / 2;

                if (propertyIndex >= 0 && propertyIndex <= 8) {
                    propertyY = squareY;
                    propertyX = squareX + 75f;
                } else if (propertyIndex >= 9 && propertyIndex <= 19) {
                    propertyX = squareX;
                    propertyY = squareY - 85f;
                } else if (propertyIndex >= 20 && propertyIndex <= 28) {
                    propertyY = squareY;
                    propertyX = squareX - 85f;
                } else if (propertyIndex >= 29 && propertyIndex <= 39) {
                    propertyX = squareX;
                    propertyY = squareY + 75f;
                }

                batch.draw(tokenTexture, propertyX, propertyY, TOKEN_ON_PROPERTY_SIZE, TOKEN_ON_PROPERTY_SIZE);
            }
        }

        batch.end();
    }

    private void renderCard() {
        if (currentCard != null && gameLogic.getCurrentPhase() == Logic.GamePhase.HANDLING_CARD_EFFECT) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(new Color(0, 0, 0, 0.6f));
            float cardWidth = WORLD_WIDTH * 0.4f, cardHeight = WORLD_HEIGHT * 0.3f;
            float cardX = WORLD_WIDTH / 2 - cardWidth / 2, cardY = WORLD_HEIGHT / 2 - cardHeight / 2;
            shapeRenderer.rect(cardX, cardY, cardWidth, cardHeight);
            shapeRenderer.end();
            batch.begin();
            String text = currentCard.textA() + (currentCard.textB() != null ? "\n" + currentCard.textB() : "") + (currentCard.textC() != null ? "\n" + currentCard.textC() : "");
            glyphLayout.setText(font, text, Color.WHITE, cardWidth - 20, com.badlogic.gdx.utils.Align.center, true);
            font.draw(batch, glyphLayout, cardX + 10, cardY + cardHeight - 10);
            batch.end();
        }
    }

    private void renderGameMessage() {
        String message = gameLogic != null ? gameLogic.getGameMessage() : "Error initializing game logic.";
        if (message != null && !message.isEmpty() && messageTimer <= MESSAGE_DURATION) {
            float messageAreaX = WORLD_WIDTH * 0.25f;
            float messageAreaWidth = WORLD_WIDTH * 0.5f;
            float messageAreaHeight = 100f;
            float messageAreaY = WORLD_HEIGHT / 2 - messageAreaHeight - 50f;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(new Color(0, 0, 0, 0.6f));
            shapeRenderer.rect(messageAreaX, messageAreaY, messageAreaWidth, messageAreaHeight);
            shapeRenderer.end();

            batch.begin();
            if (font != null) {
                glyphLayout.setText(font, message, Color.WHITE, messageAreaWidth - 20, com.badlogic.gdx.utils.Align.center, true);
                float textX = messageAreaX + (messageAreaWidth - glyphLayout.width) / 2f;
                float textY = messageAreaY + (messageAreaHeight + glyphLayout.height) / 2f;
                font.draw(batch, glyphLayout, textX, textY);
            } else {
                Gdx.app.error("MainGameScreen", "Font is null, cannot render message text.");
            }
            batch.end();
        }
    }

    private void renderButtons() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        String[] buttonLabels = {"Roll", "Buy", "End Turn", "Pay Bail", "Use Jail Card", "Reject"}; // Added "Reject"
        boolean isInJail = gameLogic.getCurrentPlayer() != null && gameLogic.getCurrentPlayer().isInJail();
        Logic.GamePhase phase = gameLogic.getCurrentPhase();

        // Render các nút thông thường
        for (int i = 0; i < buttonLabels.length; i++) {
            float buttonY = buttonAreaStartY - i * (BUTTON_HEIGHT + BUTTON_GAP);
            boolean isButtonActive = false;

            switch (i) {
                case 0: // Roll
                    isButtonActive = phase == Logic.GamePhase.PLAYER_ROLLING &&
                        (isInJail || !isInJail);
                    break;
                case 1: // Buy
                    isButtonActive = phase == Logic.GamePhase.AWAITING_PURCHASE_DECISION;
                    break;
                case 2: // End Turn
                    isButtonActive = phase == Logic.GamePhase.MANAGING_PROPERTIES ||
                        phase == Logic.GamePhase.AWAITING_PLAYER_ACTION ||
                        phase == Logic.GamePhase.AWAITING_JAIL_DECISION;
                    break;
                case 3: // Pay Bail
                    isButtonActive = phase == Logic.GamePhase.PLAYER_ROLLING &&
                        isInJail &&
                        gameLogic.getCurrentPlayer().getMoney() >= Logic.BAIL_AMOUNT;
                    break;
                case 4: // Use Jail Card
                    isButtonActive = phase == Logic.GamePhase.PLAYER_ROLLING &&
                        isInJail &&
                        gameLogic.getCurrentPlayer().getNumGetOutOfJailFreeCards() > 0;
                    break;
                case 5: // Reject
                    isButtonActive = phase == Logic.GamePhase.AWAITING_PURCHASE_DECISION;
                    break;
            }

            shapeRenderer.setColor(isButtonActive ? new Color(0.2f, 0.5f, 0.8f, 1f) : new Color(0.5f, 0.5f, 0.5f, 1f));
            shapeRenderer.rect(BUTTON_AREA_X, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        }

        // Render nút Replay khi trò chơi kết thúc
        if (phase == Logic.GamePhase.GAME_OVER) {
            float replayButtonX = WORLD_WIDTH / 2 - BUTTON_WIDTH / 2;
            shapeRenderer.setColor(new Color(0.2f, 0.5f, 0.8f, 1f));
            shapeRenderer.rect(replayButtonX, REPLAY_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        }

        shapeRenderer.end();

        batch.begin();
        // Render nhãn cho các nút thông thường
        for (int i = 0; i < buttonLabels.length; i++) {
            float buttonY = buttonAreaStartY - i * (BUTTON_HEIGHT + BUTTON_GAP);
            glyphLayout.setText(font, buttonLabels[i]);
            float textX = BUTTON_AREA_X + (BUTTON_WIDTH - glyphLayout.width) / 2;
            float textY = buttonY + (BUTTON_HEIGHT + glyphLayout.height) / 2;
            font.setColor(Color.WHITE);
            font.draw(batch, glyphLayout, textX, textY);
        }

        // Render nhãn cho nút Replay
        if (phase == Logic.GamePhase.GAME_OVER) {
            float replayButtonX = WORLD_WIDTH / 2 - BUTTON_WIDTH / 2;
            glyphLayout.setText(font, "Replay");
            float textX = replayButtonX + (BUTTON_WIDTH - glyphLayout.width) / 2;
            float textY = REPLAY_BUTTON_Y + (BUTTON_HEIGHT + glyphLayout.height) / 2;
            font.setColor(Color.WHITE);
            font.draw(batch, glyphLayout, textX, textY);
        }

        batch.end();
    }

    private boolean isTokenIndexUsed(int tokenIndex, List<GameSetupScreen.PlayerSetupInfo> players) {
        for (GameSetupScreen.PlayerSetupInfo setup : players) {
            if (setup.selectedTokenIndex == tokenIndex) {
                return true;
            }
        }
        return false;
    }

    @Override public void show() { Gdx.app.log("MainGameScreen", "show()"); }
    @Override public void resize(int width, int height) { viewport.update(width, height, true); Gdx.app.log("MainGameScreen", "resize()"); }
    @Override public void pause() { Gdx.app.log("MainGameScreen", "pause()"); }
    @Override public void resume() { Gdx.app.log("MainGameScreen", "resume()"); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); Gdx.app.log("MainGameScreen", "hide()"); }

    @Override
    public void dispose() {
        Gdx.app.log("MainGameScreen", "dispose()");
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (boardTexture != null) boardTexture.dispose();
        if (playerTokenTextures != null) {
            for (Texture t : playerTokenTextures) {
                if (t != null) t.dispose();
            }
            playerTokenTextures.clear();
        }
        if (diceTextures != null) {
            for (Texture t : diceTextures) {
                if (t != null) t.dispose();
            }
            diceTextures = null;
        }
    }
}
