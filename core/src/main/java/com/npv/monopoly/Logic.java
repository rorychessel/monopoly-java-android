package com.npv.monopoly;

import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Logic {
    private static final int INITIAL_MONEY = 1000;
    private static final int PASS_GO_MONEY = 200;
    public static final int BAIL_AMOUNT = 50;
    private static final int BOARD_SIZE = 40;
    private static final int MAX_HOUSES = 32;
    private static final int MAX_HOTELS = 12;
    private static final int VISITING_JAIL_FEE = 50; // Phí thăm tù

    private Card currentCard;
    private List<Player> players;
    private int currentPlayerIndex;
    private Board board;
    private ProbDice dice;
    private RandomDeck chanceDeck;
    private int lastDiceRollValue;
    private int lastDice1;
    private int lastDice2;
    private GamePhase currentPhase;
    private String gameMessage;
    private int availableHouses;
    private int availableHotels;
    private boolean lastRollWasDouble;

    public enum GamePhase {
        PLAYER_ROLLING, PLAYER_MOVING, AWAITING_PLAYER_ACTION, AWAITING_PURCHASE_DECISION,
        MANAGING_PROPERTIES, CPU_THINKING, HANDLING_CARD_EFFECT, AWAITING_JAIL_DECISION, LANDED_ON_SQUARE, GAME_OVER
    }

    public Logic() {
        players = new ArrayList<>();
        currentPlayerIndex = 0;
        board = null;
        dice = new ProbDice();
        chanceDeck = new RandomDeck();
        lastDiceRollValue = 0;
        lastDice1 = 0;
        lastDice2 = 0;
        currentPhase = GamePhase.PLAYER_ROLLING;
        gameMessage = "";
        availableHouses = MAX_HOUSES;
        availableHotels = MAX_HOTELS;
        lastRollWasDouble = false;
    }

    public void initializeGame(GameSetupScreen.PlayerSetupInfo[] playerSetups, boolean vsAI) {
        if (playerSetups == null || playerSetups.length < 2 || playerSetups.length > 8) {
            currentPhase = GamePhase.GAME_OVER;
            gameMessage = "Invalid number of players (must be 2-8)";
            return;
        }

        players.clear();
        for (GameSetupScreen.PlayerSetupInfo setup : playerSetups) {
            if (setup == null || setup.name == null || setup.name.trim().isEmpty()) {
                currentPhase = GamePhase.GAME_OVER;
                gameMessage = "Invalid player setup";
                return;
            }
            players.add(new HumanPlayer(setup.name, INITIAL_MONEY));
        }

        if (vsAI) {
            players.add(new CPUPlayer("AI", INITIAL_MONEY));
        }

        Card[] chanceCards = new Card[30];
        for (int i = 0; i < 30; i++) {
            chanceCards[i] = new Card(i);
        }
        chanceDeck.initialize(chanceCards);

        board = new Board(chanceDeck, null);
        currentPlayerIndex = 0;
        currentPhase = GamePhase.PLAYER_ROLLING;
        gameMessage = getCurrentPlayer().name() + "'s turn: Roll the dice";
    }

    public void playerRequestsRollDice() {
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer.isInJail()) {
            handleJailRollAttempt();
        } else {
            performRollAndMove();
        }
    }

    private void performRollAndMove() {
        Dice.Roll roll = dice.roll();
        lastDiceRollValue = roll.val;
        lastDice1 = roll.dice1;
        lastDice2 = roll.dice2;
        Player currentPlayer = getCurrentPlayer();
        lastRollWasDouble = roll.is_double;

        Gdx.app.log("Logic", "Roll: dice1=" + lastDice1 + ", dice2=" + lastDice2 + ", isDouble=" + lastRollWasDouble);

        // Kiểm tra xúc đôi 3 lần liên tiếp
        if (lastRollWasDouble) {
            currentPlayer.incrementDoubleRollsThisTurn();
            if (currentPlayer.getDoubleRollsThisTurn() >= 3) {
                sendToJail();
                return;
            }
        } else {
            currentPlayer.resetDoubleRollsThisTurn();
        }

        // Loại bỏ người chơi
        int oldPosition = currentPlayer.getCurrentBoardIndex();
        currentPlayer.moveBySteps(lastDiceRollValue, BOARD_SIZE);
        int newPosition = currentPlayer.getCurrentBoardIndex();

        // Kiểm tra xem có đi qua ô GO không
        if (newPosition < oldPosition && !currentPlayer.isInJail()) {
            currentPlayer.changeMoney(PASS_GO_MONEY);
            gameMessage = currentPlayer.name() + " passed GO, collected $" + PASS_GO_MONEY;
        }

        currentPhase = GamePhase.PLAYER_MOVING;
        handleLandedSquare();
    }

    private void handleJailRollAttempt() {
        Dice.Roll roll = dice.roll();
        lastDiceRollValue = roll.val;
        lastDice1 = roll.dice1;
        lastDice2 = roll.dice2;
        lastRollWasDouble = roll.is_double;

        Gdx.app.log("Logic", "Jail Roll: dice1=" + lastDice1 + ", dice2=" + lastDice2 + ", isDouble=" + lastRollWasDouble);

        Player currentPlayer = getCurrentPlayer();

        if (lastRollWasDouble) {
            currentPlayer.leaveJail();
            currentPlayer.resetDoubleRollsThisTurn();
            gameMessage = currentPlayer.name() + " rolled doubles, left jail";
            currentPhase = GamePhase.PLAYER_MOVING;

            // Di chuyển tiếp sau khi xúc đôi
            int oldPosition = currentPlayer.getCurrentBoardIndex();
            currentPlayer.moveBySteps(lastDiceRollValue, BOARD_SIZE);
            int newPosition = currentPlayer.getCurrentBoardIndex();
            if (newPosition < oldPosition) {
                currentPlayer.changeMoney(PASS_GO_MONEY);
                gameMessage += " and passed GO, collected $" + PASS_GO_MONEY;
            }
            handleLandedSquare();
        } else {
            boolean stayInJail = currentPlayer.decrementJailTurnsAndCheckStay();
            if (!stayInJail) {
                if (currentPlayer.getNumGetOutOfJailFreeCards() > 0 && currentPlayer.useGetOutOfJailFreeCard()) {
                    chanceDeck.returnOutOfJail();
                    currentPlayer.leaveJail();
                    gameMessage = currentPlayer.name() + " used Get Out of Jail Free card";
                    currentPhase = GamePhase.PLAYER_ROLLING;
                } else if (currentPlayer.getMoney() >= BAIL_AMOUNT) {
                    currentPlayer.changeMoney(-BAIL_AMOUNT);
                    currentPlayer.leaveJail();
                    gameMessage = currentPlayer.name() + " paid $" + BAIL_AMOUNT + " to leave jail";
                    currentPhase = GamePhase.PLAYER_ROLLING;
                } else {
                    handleBankruptcy(null);
                    return;
                }
            } else {
                gameMessage = currentPlayer.name() + " failed to roll doubles, " + currentPlayer.getJailTurnsRemaining() + " turns left in jail";
                currentPhase = GamePhase.AWAITING_JAIL_DECISION;
                endCurrentPlayerTurn();
            }
        }
    }

    private void handleLandedSquare() {
        Player currentPlayer = getCurrentPlayer();
        Square currentSquare = board.getSquareByIndex(currentPlayer.getCurrentBoardIndex());

        if (currentSquare == null) {
            currentPhase = GamePhase.GAME_OVER;
            gameMessage = "Error: Invalid square at index " + currentPlayer.getCurrentBoardIndex();
            return;
        }

        // Lưu trạng thái tung đôi trước khi xử lý ô
        boolean wasDouble = lastRollWasDouble;
        int doubleRolls = currentPlayer.getDoubleRollsThisTurn();

        Gdx.app.log("Logic", "Handling square: " + currentSquare.name() + ", wasDouble=" + wasDouble + ", doubleRolls=" + doubleRolls);

        // Xử lý ô "Thăm Tù" (ô số 28)
        if (board.getIndexForSquare(currentSquare) == 28 && currentSquare.name().equals("Thăm Tù")) {
            // Kiểm tra xem ô tù (ô số 8) có người hay không
            boolean jailOccupied = false;
            for (Player p : players) {
                if (p.getCurrentBoardIndex() == 8 && p.isInJail() && p != currentPlayer) {
                    jailOccupied = true;
                    break;
                }
            }

            if (!jailOccupied) {
                // Ô tù trống, người chơi vào tù
                sendToJail();
                gameMessage = currentPlayer.name() + " landed on Thăm Tù and was sent to jail (jail was empty)";
                return;
            } else {
                // Ô tù có người, trả phí thăm tù
                handlePayment(currentPlayer, null, VISITING_JAIL_FEE);
                gameMessage = currentPlayer.name() + " landed on Thăm Tù and paid $" + VISITING_JAIL_FEE + " visiting fee (jail occupied)";
            }
        } else if (currentSquare instanceof Property || currentSquare instanceof Railroad) {
            if (!currentSquare.isOwned() && currentSquare.isOwnable()) {
                currentPhase = GamePhase.AWAITING_PURCHASE_DECISION;
                gameMessage = currentPlayer.name() + ", buy " + currentSquare.name() + " for $" + currentSquare.cost() + "?";
                return; // Chờ quyết định mua
            } else if (currentSquare.isOwned() && currentSquare.owner() != currentPlayer) {
                int rent = currentSquare.rent();
                if (rent > 0) {
                    handlePayment(currentPlayer, currentSquare.owner(), rent);
                    gameMessage = currentPlayer.name() + " paid $" + rent + " rent to " + currentSquare.owner().name();
                }
            }
        } else if (currentSquare instanceof ChanceSquare) {
            Card card = ((ChanceSquare) currentSquare).drawCard();
            if (card == null) {
                Gdx.app.error("Logic", "Failed to draw card from Chance square at " + currentSquare.name());
                gameMessage = "Error: No card available at " + currentSquare.name();
            } else {
                currentPhase = GamePhase.HANDLING_CARD_EFFECT;
                currentCard = card;
                gameMessage = card.textA() + (card.textB() != null ? " " + card.textB() : "") + (card.textC() != null ? " " + card.textC() : "");
                return; // Chờ xử lý thẻ
            }
        } else if (currentSquare instanceof Taxes) {
            double tax = ((Taxes) currentSquare).tax(currentPlayer.getTotalAssetsValue());
            handlePayment(currentPlayer, null, (int) tax);
            gameMessage = currentPlayer.name() + " paid $" + (int) tax + " in taxes";
        } else if (currentSquare instanceof Jail) {
            Jail jail = (Jail) currentSquare;
            if (jail.getType() == Jail.JailType.TO_JAIL) {
                sendToJail();
                return; // Không kiểm tra tung đôi vì vào tù
            }
        }

        // Kiểm tra tung đôi sau khi xử lý ô (trừ trường hợp chờ mua, xử lý thẻ, hoặc vào tù)
        if (wasDouble && doubleRolls < 3 && !currentPlayer.isInJail()) {
            currentPhase = GamePhase.PLAYER_ROLLING;
            gameMessage = currentPlayer.name() + " rolled doubles, roll again!";
        } else {
            currentPhase = GamePhase.MANAGING_PROPERTIES;
            gameMessage = currentPlayer.name() + ": Manage properties or end turn.";
        }
    }

    public void applyCardEffect(Card card) {
        if (card == null) {
            Gdx.app.error("Logic", "Attempted to apply null card effect");
            gameMessage = "Error: Invalid card effect";
            currentCard = null;
            transitionToNextPhase();
            return;
        }

        Card.CardAction action = card.action();
        if (action == null) {
            Gdx.app.error("Logic", "Card action is null for card: " + card.textA());
            gameMessage = "Error: Invalid card action";
            currentCard = null;
            transitionToNextPhase();
            return;
        }

        Player currentPlayer = getCurrentPlayer();
        this.currentCard = card;
        StringBuilder message = new StringBuilder(card.textA());
        if (card.textB() != null && !card.textB().isEmpty()) {
            message.append(" ").append(card.textB());
        }
        if (card.textC() != null && !card.textC().isEmpty()) {
            message.append(" ").append(card.textC());
        }
        gameMessage = message.toString();

        boolean wasDouble = lastRollWasDouble;
        int doubleRolls = currentPlayer.getDoubleRollsThisTurn();

        switch (action) {
            case BANK_PAYS_YOU:
                currentPlayer.changeMoney(card.value());
                break;
            case PAY_BANK:
                handlePayment(currentPlayer, null, -card.value());
                if (isPlayerBankrupt(currentPlayer)) return;
                break;
            case ADVANCE_TO_GO:
                movePlayerToSquare(0, true);
                return;
            case GO_TO_SQUARE:
                if (card.travel() != Integer.MAX_VALUE) {
                    int steps = card.travel();
                    int oldPos = currentPlayer.getCurrentBoardIndex();
                    currentPlayer.moveBySteps(steps, BOARD_SIZE);
                    int newPosition = (oldPos + steps + BOARD_SIZE) % BOARD_SIZE;
                    currentPlayer.moveToBoardIndex(newPosition);
                    if (steps > 0 && newPosition < oldPos) {
                        currentPlayer.changeMoney(PASS_GO_MONEY);
                    }
                    handleLandedSquare();
                    return;
                } else if (card.travelTo() != Integer.MAX_VALUE) {
                    int target = card.travelTo();
                    movePlayerToSquare(target, card.increased());
                    return;
                } else if (card.travelRail()) {
                    moveToNearestRailroad();
                    return;
                } else {
                    Gdx.app.error("Logic", "Invalid GO_TO_SQUARE card configuration: " + card.textA());
                    gameMessage = "Error: Invalid card movement";
                }
                break;
            case GO_TO_JAIL:
                sendToJail();
                return;
            case GET_OUT_OF_JAIL_FREE:
                currentPlayer.addGetOutOfJailFreeCard();
                break;
            case PAY_EACH_PLAYER:
                for (Player other : players) {
                    if (other != currentPlayer && !isPlayerBankrupt(other)) {
                        handlePayment(currentPlayer, other, -card.eachPlayer());
                    }
                }
                if (isPlayerBankrupt(currentPlayer)) return;
                break;
            case COLLECT_FROM_EACH_PLAYER:
                for (Player other : players) {
                    if (other != currentPlayer && !isPlayerBankrupt(other)) {
                        handlePayment(other, currentPlayer, -card.eachPlayer());
                    }
                }
                if (isPlayerBankrupt(currentPlayer)) return;
                break;
            default:
                Gdx.app.error("Logic", "Unhandled card action: " + action);
                gameMessage = "Error: Unhandled card effect";
                break;
        }

        if (isPlayerBankrupt(currentPlayer)) return;
        if (wasDouble && doubleRolls < 3 && !currentPlayer.isInJail()) {
            currentPhase = GamePhase.PLAYER_ROLLING;
            gameMessage = currentPlayer.name() + " rolled doubles, roll again!";
        } else {
            currentPhase = GamePhase.MANAGING_PROPERTIES;
            gameMessage = currentPlayer.name() + ": Manage properties or end turn.";
        }
    }

    private void movePlayerToSquare(int targetIndex, boolean passGo) {
        Player currentPlayer = getCurrentPlayer();
        int oldPos = currentPlayer.getCurrentBoardIndex();
        currentPlayer.moveToBoardIndex(targetIndex);
        boolean passedGo = passGo && targetIndex < oldPos && targetIndex != board.getJailSquareIndex();
        if (passedGo) {
            currentPlayer.changeMoney(PASS_GO_MONEY);
            gameMessage = currentPlayer.name() + " passed GO, collected $" + PASS_GO_MONEY;
        } else {
            gameMessage = "";
        }
        Square targetSquare = board.getSquareByIndex(targetIndex);
        if (targetSquare == null) {
            Gdx.app.error("Logic", "Invalid square index: " + targetIndex);
            gameMessage = "Error: Invalid board position";
            currentPhase = GamePhase.GAME_OVER;
            return;
        }
        boolean wasDouble = lastRollWasDouble;
        int doubleRolls = currentPlayer.getDoubleRollsThisTurn();
        if (targetSquare instanceof ChanceSquare && currentPhase == GamePhase.HANDLING_CARD_EFFECT) {
            gameMessage = passedGo ? gameMessage + "; Landed on another Chance square, skipping card draw" : "Landed on another Chance square, skipping card draw";
            if (wasDouble && doubleRolls < 3 && !currentPlayer.isInJail()) {
                currentPhase = GamePhase.PLAYER_ROLLING;
                gameMessage = currentPlayer.name() + " rolled doubles, roll again!";
            } else {
                currentPhase = GamePhase.MANAGING_PROPERTIES;
                gameMessage = currentPlayer.name() + ": Manage properties or end turn.";
            }
        } else {
            handleLandedSquare();
            // Đặt lại trạng thái xúc đôi
            if (!currentPlayer.isInJail() && wasDouble && doubleRolls < 3 && currentPhase != GamePhase.AWAITING_PURCHASE_DECISION && currentPhase != GamePhase.AWAITING_JAIL_DECISION) {
                currentPhase = GamePhase.PLAYER_ROLLING;
                gameMessage = currentPlayer.name() + " rolled doubles, roll again!";
            }
        }
    }

    private void moveToNearestRailroad() {
        Player currentPlayer = getCurrentPlayer();
        int currentIndex = currentPlayer.getCurrentBoardIndex();
        int[] railroadIndices = {5, 17, 25, 32};
        int minDistance = BOARD_SIZE;
        int targetIndex = railroadIndices[0];

        for (int railIndex : railroadIndices) {
            int distance = (railIndex - currentIndex + BOARD_SIZE) % BOARD_SIZE;
            if (distance < minDistance) {
                minDistance = distance;
                targetIndex = railIndex;
            }
        }

        movePlayerToSquare(targetIndex, true);
        Square rail = board.getSquareByIndex(targetIndex);
        if (rail.isOwned() && rail.owner() != currentPlayer) {
            int rent = rail.rent() * 2;
            handlePayment(currentPlayer, rail.owner(), rent);
            gameMessage += "; Paid $" + rent + " rent to " + rail.owner().name();
        }
    }

    private void sendToJail() {
        Player currentPlayer = getCurrentPlayer();
        currentPlayer.moveToBoardIndex(board.getJailSquareIndex());
        currentPlayer.goToJail();
        currentPlayer.resetDoubleRollsThisTurn();
        lastRollWasDouble = false;
        gameMessage = currentPlayer.name() + " was sent to jail";
        currentPhase = GamePhase.AWAITING_JAIL_DECISION;
        // Tự động kết thúc lượt sau khi vào tù
        endCurrentPlayerTurn();
    }

    public void playerDecidesPurchase(boolean wantsToBuy) {
        if (currentPhase != GamePhase.AWAITING_PURCHASE_DECISION) {
            Gdx.app.log("Logic", "Invalid phase for purchase decision: " + currentPhase);
            return;
        }

        Player currentPlayer = getCurrentPlayer();
        Square currentSquare = board.getSquareByIndex(currentPlayer.getCurrentBoardIndex());

        if (wantsToBuy && currentSquare.isOwnable() && !currentSquare.isOwned()) {
            int cost = currentSquare.cost();
            if (currentPlayer.getMoney() >= cost) {
                currentPlayer.changeMoney(-cost);
                currentSquare.purchase(currentPlayer);
                currentPlayer.addProperty(currentSquare);
                gameMessage = currentPlayer.name() + " bought " + currentSquare.name() + " for $" + cost;
                // Kiểm tra điều kiện thắng sau khi mua
                if (checkWinCondition(currentPlayer)) {
                    return;
                }
            } else {
                gameMessage = currentPlayer.name() + " cannot afford " + currentSquare.name();
            }
        } else {
            gameMessage = currentPlayer.name() + " declined to buy " + currentSquare.name();
        }

        // Kiểm tra tung đôi sau khi quyết định mua
        if (lastRollWasDouble && currentPlayer.getDoubleRollsThisTurn() < 3 && !currentPlayer.isInJail()) {
            currentPhase = GamePhase.PLAYER_ROLLING;
            gameMessage = currentPlayer.name() + " rolled doubles, roll again!";
        } else {
            // Nếu không xúc đôi, kết thúc lượt ngay lập tức và chuyển lượt cho người chơi tiếp theo
            endCurrentPlayerTurn();
        }
    }

    public void playerRequestsBuildHouse(String propertyName, int numHouses) {
        if (currentPhase != GamePhase.MANAGING_PROPERTIES) {
            return;
        }

        Player currentPlayer = getCurrentPlayer();
        Property property = board.property(propertyName);
        if (property == null) {
            gameMessage = "Property " + propertyName + " does not exist";
            return;
        }
        if (property.owner() != currentPlayer) {
            gameMessage = currentPlayer.name() + " does not own " + propertyName;
            return;
        }
        if (!property.hasMonopoly()) {
            gameMessage = "Cannot build on " + propertyName + ": No monopoly";
            return;
        }
        if (property.numHouses() + numHouses > 5) {
            gameMessage = "Cannot build on " + propertyName + ": Maximum houses reached";
            return;
        }

        int housesToBuild = Math.min(numHouses, 5 - property.numHouses());
        int totalCost = 0;
        for (int i = 0; i < housesToBuild; i++) {
            if (property.groupBuild() && availableHouses > 0) {
                totalCost += property.houseCost();
                if (property.numHouses() == 4 && availableHotels > 0) {
                    availableHouses += 4;
                    availableHotels--;
                } else {
                    availableHouses--;
                }
                property.build(1);
            } else {
                gameMessage = "Cannot build on " + propertyName + ": Uneven build or no houses available";
                return;
            }
        }

        if (currentPlayer.getMoney() >= totalCost) {
            currentPlayer.changeMoney(-totalCost);
            gameMessage = currentPlayer.name() + " built " + housesToBuild + " house(s) on " + propertyName + " for $" + totalCost;
        } else {
            gameMessage = currentPlayer.name() + " cannot afford to build on " + propertyName;
            property.build(-housesToBuild);
            availableHouses += housesToBuild;
        }
    }

    public void playerRequestsSellHouse(String propertyName, int numHouses) {
        if (currentPhase != GamePhase.MANAGING_PROPERTIES) {
            return;
        }

        Player currentPlayer = getCurrentPlayer();
        Property property = board.property(propertyName);
        if (property == null || property.owner() != currentPlayer) {
            gameMessage = "Cannot sell houses on " + propertyName;
            return;
        }

        int housesToSell = Math.min(numHouses, property.numHouses());
        int totalRefund = 0;
        for (int i = 0; i < housesToSell; i++) {
            if (property.groupSell()) {
                totalRefund += property.houseCost() / 2;
                if (property.numHouses() == 5) {
                    availableHotels++;
                    availableHouses -= 4;
                }
                property.build(-1);
                availableHouses++;
            } else {
                gameMessage = "Cannot sell houses on " + propertyName + ": uneven";
                return;
            }
        }

        currentPlayer.changeMoney(totalRefund);
        gameMessage = currentPlayer.name() + " sold " + housesToSell + " house(s) on " + propertyName + " for $" + totalRefund;
    }

    private void handlePayment(Player payer, Player receiver, int amount) {
        if (amount <= 0 || isPlayerBankrupt(payer)) {
            return;
        }

        if (payer.getMoney() >= amount) {
            payer.changeMoney(-amount);
            if (receiver != null) {
                receiver.changeMoney(amount);
            }
        } else {
            handleBankruptcy(receiver);
        }
    }

    private void handleBankruptcy(Player creditor) {
        Player debtor = getCurrentPlayer();
        if (creditor != null && debtor.getMoney() < 0) {
            for (Square property : debtor.getProperties()) {
                property.purchase(creditor);
                creditor.addProperty(property);
            }
            debtor.getProperties().forEach(debtor::removeProperty);
            debtor.changeMoney(-debtor.getMoney());
        }

        players.remove(debtor);
        if (players.size() <= 1) {
            currentPhase = GamePhase.GAME_OVER;
            gameMessage = players.isEmpty() ? "No players left!" : players.get(0).name() + " wins!";
            return;
        }

        if (creditor != null && creditor.getMoney() < 0) {
            handleBankruptcy(null);
            return;
        }

        currentPlayerIndex = currentPlayerIndex % players.size();
        currentPhase = GamePhase.PLAYER_ROLLING;
        gameMessage = getCurrentPlayer().name() + "'s turn: Roll the dice";
    }

    private boolean hasSellableProperty(Player player) {
        if (player == null || player.getProperties() == null) return false;
        for (Square square : player.getProperties()) {
            if (square instanceof Property) {
                Property prop = (Property) square;
                if (prop.isOwned() && prop.owner() == player && prop.numHouses() >= 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkWinCondition(Player player) {
        // Kiểm tra bộ màu
        List<Square> ownedProperties = new ArrayList<>();
        for (Square square : player.getProperties()) {
            if (square instanceof Property) {
                ownedProperties.add(square);
            }
        }

        // Kiểm tra những bộ màu đã được hoàn thành
        Set<String> completedColorGroups = new HashSet<>();
        for (Square square : ownedProperties) {
            Property prop = (Property) square;
            Property[] group = prop.getGroup();
            if (group == null || group.length == 0) {
                Gdx.app.log("Logic", "Skipping property " + prop.name() + ": No group defined");
                continue;
            }

            StringBuilder groupProperties = new StringBuilder("[");
            for (int i = 0; i < group.length; i++) {
                if (group[i] != null) {
                    groupProperties.append(group[i].name()).append(" (owned by ").append(group[i].isOwned() ? group[i].owner().name() : "none").append(")");
                    if (i < group.length - 1) groupProperties.append(", ");
                }
            }
            groupProperties.append("]");
            Gdx.app.log("Logic", "Checking group for property " + prop.name() + ": " + groupProperties.toString());

            // Kiểm tra xem người chơi đã mua được tất cả ô đất thuộc cùng 1 bộ màu chưa
            boolean ownsAll = true;
            String groupIdentifier = null;
            for (Property groupProp : group) {
                if (groupProp == null) continue;
                if (groupIdentifier == null) {
                    List<String> propertyNames = new ArrayList<>();
                    for (Property p : group) {
                        if (p != null) propertyNames.add(p.name());
                    }
                    propertyNames.sort(String::compareTo);
                    groupIdentifier = String.join("_", propertyNames) + "_group";
                }
                if (!groupProp.isOwned() || groupProp.owner() != player) {
                    ownsAll = false;
                    break;
                }
            }

            if (ownsAll && groupIdentifier != null) {
                Gdx.app.log("Logic", "Player " + player.name() + " fully owns group: " + groupIdentifier + " with properties " + groupProperties.toString());
                completedColorGroups.add(groupIdentifier);
            }
        }

        Gdx.app.log("Logic", "Player " + player.name() + " has completed the following color groups: " + completedColorGroups.toString());
        Gdx.app.log("Logic", "Player " + player.name() + " has completed " + completedColorGroups.size() + " color groups");

        // Kiểm tra số bộ màu khác nhau của người chơi
        if (completedColorGroups.size() >= 3) {
            currentPhase = GamePhase.GAME_OVER;
            gameMessage = player.name() + " wins by owning " + completedColorGroups.size() + " distinct color groups!";
            Gdx.app.log("Logic", "Win condition met: " + player.name() + " owns " + completedColorGroups.size() + " distinct color groups");
            return true;
        }

        // Kiểm tra nhà ga
        int railroadCount = 0;
        for (Square square : player.getProperties()) {
            if (square instanceof Railroad) {
                railroadCount++;
            }
        }

        if (railroadCount == 4) {
            currentPhase = GamePhase.GAME_OVER;
            gameMessage = player.name() + " wins by owning all 4 railroads!";
            Gdx.app.log("Logic", "Win condition met: " + player.name() + " owns all 4 railroads");
            return true;
        }

        return false;
    }

    public boolean wasLastRollDouble() {
        return lastRollWasDouble;
    }

    public void transitionToNextPhase() {
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer == null) {
            currentPhase = GamePhase.GAME_OVER;
            gameMessage = "No player available. Game over.";
            return;
        }

        boolean wasDouble = lastRollWasDouble;
        int doubleRolls = currentPlayer.getDoubleRollsThisTurn();

        switch (currentPhase) {
            case AWAITING_PLAYER_ACTION:
                if (currentPlayer.isInJail()) {
                    currentPhase = GamePhase.AWAITING_JAIL_DECISION;
                    gameMessage = currentPlayer.name() + " is in jail. Roll doubles, pay, or use card.";
                } else if (currentPlayer instanceof CPUPlayer) {
                    currentPhase = GamePhase.CPU_THINKING;
                    gameMessage = currentPlayer.name() + " (CPU) is thinking...";
                } else {
                    currentPhase = GamePhase.PLAYER_ROLLING;
                    gameMessage = currentPlayer.name() + ": Press 'Roll' to roll the dice.";
                }
                break;

            case PLAYER_ROLLING:
                currentPhase = GamePhase.PLAYER_MOVING;
                gameMessage = currentPlayer.name() + " is moving...";
                break;

            case PLAYER_MOVING:
                currentPhase = GamePhase.LANDED_ON_SQUARE;
                gameMessage = currentPlayer.name() + " is moving...";
                break;

            case LANDED_ON_SQUARE:
                break;

            case AWAITING_PURCHASE_DECISION:
                if (wasDouble && doubleRolls < 3 && !currentPlayer.isInJail()) {
                    currentPhase = GamePhase.PLAYER_ROLLING;
                    gameMessage = currentPlayer.name() + " rolled doubles, roll again!";
                } else {
                    currentPhase = GamePhase.MANAGING_PROPERTIES;
                    gameMessage = currentPlayer.name() + ": Manage properties or end turn.";
                }
                break;

            case HANDLING_CARD_EFFECT:
                currentCard = null;
                if (wasDouble && doubleRolls < 3 && !currentPlayer.isInJail()) {
                    currentPhase = GamePhase.PLAYER_ROLLING;
                    gameMessage = currentPlayer.name() + " rolled doubles, roll again!";
                } else {
                    currentPhase = GamePhase.MANAGING_PROPERTIES;
                    gameMessage = currentPlayer.name() + ": Manage properties or end turn.";
                }
                break;

            case MANAGING_PROPERTIES:
                if (currentPlayer.getMoney() < 0 && !hasSellableProperty(currentPlayer)) {
                    handleBankruptcy(currentPlayer);
                    currentPhase = GamePhase.GAME_OVER;
                    gameMessage = currentPlayer.name() + " went bankrupt!";
                } else if (wasDouble && doubleRolls < 3 && !currentPlayer.isInJail()) {
                    currentPhase = GamePhase.PLAYER_ROLLING;
                    gameMessage = currentPlayer.name() + " rolled doubles, roll again!";
                } else {
                    currentPhase = GamePhase.AWAITING_PLAYER_ACTION;
                    gameMessage = currentPlayer.name() + ": End turn or manage properties.";
                }
                break;

            case AWAITING_JAIL_DECISION:
                if (!currentPlayer.isInJail()) {
                    // Người chơi ra tù
                    endCurrentPlayerTurn();
                    gameMessage = getCurrentPlayer().name() + "'s turn: Roll the dice.";
                } else if (currentPlayer.getJailTurnsRemaining() <= 0) {
                    if (currentPlayer.getMoney() >= BAIL_AMOUNT) {
                        currentPlayer.changeMoney(-BAIL_AMOUNT);
                        currentPlayer.leaveJail();
                        gameMessage = currentPlayer.name() + " paid $" + BAIL_AMOUNT + " to leave jail.";
                        endCurrentPlayerTurn();
                        gameMessage = getCurrentPlayer().name() + "'s turn: Roll the dice.";
                    } else {
                        handleBankruptcy(currentPlayer);
                        currentPhase = GamePhase.GAME_OVER;
                        gameMessage = currentPlayer.name() + " went bankrupt due to inability to leave jail!";
                    }
                } else {
                    endCurrentPlayerTurn();
                    gameMessage = getCurrentPlayer().name() + "'s turn: Roll the dice.";
                }
                break;

            case CPU_THINKING:
                endCurrentPlayerTurn();
                currentPlayer = getCurrentPlayer();
                if (currentPlayer == null) {
                    currentPhase = GamePhase.GAME_OVER;
                    gameMessage = "No players left. Game over.";
                } else if (currentPlayer instanceof CPUPlayer) {
                    currentPhase = GamePhase.CPU_THINKING;
                    gameMessage = currentPlayer.name() + " (CPU) is thinking...";
                } else {
                    currentPhase = GamePhase.AWAITING_PLAYER_ACTION;
                    gameMessage = currentPlayer.name() + ": Press 'Roll' to start turn.";
                }
                break;

            case GAME_OVER:
                gameMessage = "Game over!";
                break;

            default:
                currentPhase = GamePhase.AWAITING_PLAYER_ACTION;
                gameMessage = "State error. Resetting turn for " + currentPlayer.name();
                break;
        }

        if (currentPhase != GamePhase.GAME_OVER) {
            int activePlayers = 0;
            Player winner = null;
            for (Player p : players) {
                if (!isPlayerBankrupt(p)) {
                    activePlayers++;
                    winner = p;
                }
            }
            if (activePlayers <= 1) {
                currentPhase = GamePhase.GAME_OVER;
                gameMessage = winner != null ? winner.name() + " wins!" : "Game over!";
            }
        }
    }

    public void handleCPUDecisions() {
        CPUPlayer cpu = (CPUPlayer) getCurrentPlayer();
        Square currentSquare = board.getSquareByIndex(cpu.getCurrentBoardIndex());

        if (currentPhase == GamePhase.AWAITING_PURCHASE_DECISION) {
            boolean wantsToBuy = cpu.decideToBuyProperty(currentSquare);
            playerDecidesPurchase(wantsToBuy);
            return;
        }

        if (cpu.isInJail()) {
            if (cpu.decideToUseJailFreeCard()) {
                cpu.useGetOutOfJailFreeCard();
                chanceDeck.returnOutOfJail();
                cpu.leaveJail();
                gameMessage = cpu.name() + " used Get Out of Jail Free card";
                endCurrentPlayerTurn();
                return;
            } else if (cpu.decideToPayToLeaveJail()) {
                cpu.changeMoney(-BAIL_AMOUNT);
                cpu.leaveJail();
                gameMessage = cpu.name() + " paid $" + BAIL_AMOUNT + " to leave jail";
                endCurrentPlayerTurn();
                return;
            }
        }

        for (Square square : cpu.getProperties()) {
            if (square instanceof Property) {
                Property prop = (Property) square;
                if (prop.hasMonopoly() && prop.groupBuild() && cpu.getMoney() >= prop.houseCost() && availableHouses > 0) {
                    playerRequestsBuildHouse(prop.name(), 1);
                    return;
                }
            }
        }

        endCurrentPlayerTurn();
    }

    public void endCurrentPlayerTurn() {
        Player currentPlayer = getCurrentPlayer();
        currentPlayer.resetDoubleRollsThisTurn();
        lastRollWasDouble = false;
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        if (players.size() <= 1) {
            currentPhase = GamePhase.GAME_OVER;
            gameMessage = players.isEmpty() ? "No players left!" : players.get(0).name() + " wins!";
            return;
        }

        currentPhase = GamePhase.PLAYER_ROLLING;
        gameMessage = getCurrentPlayer().name() + "'s turn: Roll the dice";
    }

    public void playerDecidesToBuyProperty() {
        Square currentSquare = board.getSquare(getCurrentPlayer().getCurrentBoardIndex());
        if (currentSquare.isOwnable() && !currentSquare.isOwned()) {
            playerDecidesPurchase(true);
        } else {
            if (lastRollWasDouble && getCurrentPlayer().getDoubleRollsThisTurn() < 3 && !getCurrentPlayer().isInJail()) {
                currentPhase = GamePhase.PLAYER_ROLLING;
                gameMessage = getCurrentPlayer().name() + " rolled doubles, roll again!";
            } else {
                currentPhase = GamePhase.MANAGING_PROPERTIES;
                gameMessage = getCurrentPlayer().name() + ": Manage properties or end turn.";
            }
        }
    }

    public void playerDeclinesToBuyProperty() {
        playerDecidesPurchase(false);
    }

    public void playerDecidesToPayToLeaveJail() {
        Player player = getCurrentPlayer();
        if (player.isInJail() && player.getMoney() >= BAIL_AMOUNT) {
            player.changeMoney(-BAIL_AMOUNT);
            player.leaveJail();
            gameMessage = player.name() + " paid $" + BAIL_AMOUNT + " to leave jail.";
            endCurrentPlayerTurn();
        } else {
            gameMessage = player.name() + " cannot afford to pay bail.";
            transitionToNextPhase();
        }
    }

    public void playerDecidesToUseJailCard() {
        Player player = getCurrentPlayer();
        if (player.isInJail() && player.getNumGetOutOfJailFreeCards() > 0) {
            player.useGetOutOfJailFreeCard();
            player.leaveJail();
            chanceDeck.returnOutOfJail();
            gameMessage = player.name() + " used a Get Out of Jail Free card.";
            endCurrentPlayerTurn();
        } else {
            gameMessage = player.name() + " has no Get Out of Jail Free card.";
            transitionToNextPhase();
        }
    }

    private boolean isPlayerBankrupt(Player player) {
        return !players.contains(player);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getLastDiceRollValue() {
        return lastDiceRollValue;
    }

    public int getLastDice1() {
        return lastDice1;
    }

    public int getLastDice2() {
        return lastDice2;
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public String getGameMessage() {
        return gameMessage;
    }

    public void setGameMessage(String message) {
        this.gameMessage = message;
    }

    public Iterable<Player> getAllPlayers() {
        return new ArrayList<>(players);
    }

    public Card getCurrentCard() {
        return currentCard;
    }

    public void clearCurrentCard() {
        currentCard = null;
    }

    public void setPhase(GamePhase phase) {
        this.currentPhase = phase;
    }

    public Board getBoard() {
        return board;
    }
}
