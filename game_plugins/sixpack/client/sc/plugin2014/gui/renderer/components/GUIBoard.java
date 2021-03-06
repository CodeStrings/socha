package sc.plugin2014.gui.renderer.components;

import java.awt.*;
import java.util.List;
import sc.plugin2014.GameState;
import sc.plugin2014.entities.*;
import sc.plugin2014.gui.renderer.configuration.GUIConstants;
import sc.plugin2014.gui.renderer.util.ColorHelper;
import sc.plugin2014.gui.renderer.util.RendererUtil;
import sc.plugin2014.moves.LayMove;
import sc.plugin2014.util.Constants;
import sc.plugin2014.util.GameUtil;

public class GUIBoard {
    public static void draw(Graphics2D g2, int xStart, int yStart, int width,
            int height, List<GUIStone> toLayStones, GameState gameState,
            Component component, boolean dragging, GUIStone selectedStone) {
        int offsetX = calculateOffsetX(xStart, width);
        int offsetY = calculateOffsetY(yStart, height);

        for (Field field : gameState.getBoard().getFields()) {
            if (!field.isFree()) {
                GUIStone stone = new GUIStone(field.getStone(), -1);
                stone.setX((field.getPosX() * GUIConstants.STONE_WIDTH)
                        + offsetX);
                stone.setY((field.getPosY() * GUIConstants.STONE_HEIGHT)
                        + offsetY);
                stone.draw(g2, gameState.getCurrentPlayerColor());
            }
        }

        for (GUIStone toLayStone : toLayStones) {
            Field field = toLayStone.getField();
            if (field != null) {
                toLayStone.setX((field.getPosX() * GUIConstants.STONE_WIDTH)
                        + offsetX);
                toLayStone.setY((field.getPosY() * GUIConstants.STONE_HEIGHT)
                        + offsetY);
                toLayStone.draw(g2, gameState.getCurrentPlayerColor());
            }
        }

        g2.drawImage(RendererUtil.getImage("resource/game/beutel.png"), 10, 10,
                50, 55, null);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Sans", Font.BOLD, 14));
        g2.drawString(String.valueOf(gameState.getStoneCountInBag()), 28, 45);

        // g2.drawString("Nächsten Steine im Beutel:", 20, 60);

        int positionIndex = 0;
        for (int i = gameState.getNextStonesInBag().size() - 1; i >= 0; i--) {
            Stone nextStone = gameState.getNextStonesInBag().get(i);
            GUIStone nextGuiStone = new GUIStone(nextStone, -1);
            nextGuiStone.setX(20);
            nextGuiStone
                    .setY((positionIndex * (GUIConstants.STONE_HEIGHT + 2)) + 75);
            nextGuiStone.draw(g2, gameState.getCurrentPlayerColor());
            positionIndex++;
        }

        if (dragging) {
            Field fieldFromXY = getFieldUnderMouse(component, offsetX, offsetY,
                    gameState.getBoard(), xStart, yStart, width, height);

            if (fieldFromXY != null) {
                int fieldToHighlightX = (fieldFromXY.getPosX() * GUIConstants.STONE_WIDTH)
                        + offsetX;
                int fieldToHighlightY = (fieldFromXY.getPosY() * GUIConstants.STONE_HEIGHT)
                        + offsetY;

                if (isLayMovePossible(fieldFromXY, selectedStone.getStone(),
                        gameState, toLayStones)) {
                    g2.setColor(Color.GREEN);
                }
                else {
                    g2.setColor(Color.RED);
                }
                g2.fillRect(fieldToHighlightX, fieldToHighlightY,
                        GUIConstants.STONE_WIDTH, GUIConstants.STONE_HEIGHT);
            }
        }
    }

    private static boolean isLayMovePossible(Field belongingField, Stone stone,
            GameState gameState, List<GUIStone> toLayStones) {
        if ((belongingField == null) || !belongingField.isFree()) {
            return false;
        }

        if (!gameState.getBoard().hasStones() && (toLayStones.isEmpty())) {
            return true;
        }

        LayMove layMove = new LayMove();

        for (GUIStone guistone : toLayStones) {
            layMove.layStoneOntoField(guistone.getStone(), guistone.getField());
        }
        layMove.layStoneOntoField(stone, belongingField);

        return GameUtil.checkIfLayMoveIsValid(layMove, gameState.getBoard());
    }

    public static int calculateOffsetX(int xStart, int width) {
        int boardWidth = GUIConstants.STONE_WIDTH * Constants.FIELDS_IN_X_DIM;
        int deltaWidth = width - boardWidth;
        int gapWidth = deltaWidth / 2;

        return xStart + gapWidth;
    }

    public static int calculateOffsetY(int yStart, int height) {
        int boardHeight = GUIConstants.STONE_HEIGHT * Constants.FIELDS_IN_Y_DIM;
        int deltaHeight = height - boardHeight;
        int gapHeight = deltaHeight / 2;
        return yStart + gapHeight;
    }

    public static void drawGrid(Graphics2D g2, int xStart, int yStart,
            int width, int height, Board board, Component component) {
        int offsetX = calculateOffsetX(xStart, width);
        int offsetY = calculateOffsetY(yStart, height);

        g2.setColor(ColorHelper.getTransparentColor(Color.WHITE, 174));
        int maxInXDim = Constants.FIELDS_IN_X_DIM + 1;

        for (int i = 0; i < maxInXDim; i++) {
            int x = offsetX + (i * GUIConstants.STONE_WIDTH);
            g2.drawLine(x, offsetY, x, offsetY
                    + (Constants.FIELDS_IN_Y_DIM * GUIConstants.STONE_HEIGHT));
        }

        int maxInYDim = Constants.FIELDS_IN_Y_DIM + 1;

        for (int i = 0; i < maxInYDim; i++) {
            int y = offsetY + (i * GUIConstants.STONE_HEIGHT);
            g2.drawLine(offsetX, y, offsetX
                    + (Constants.FIELDS_IN_X_DIM * GUIConstants.STONE_WIDTH), y);
        }

        Field fieldFromXY = getFieldUnderMouse(component, offsetX, offsetY,
                board, xStart, yStart, width, height);

        if (fieldFromXY != null) {
            g2.drawString(
                    "Legen auf: (" + fieldFromXY.getPosX() + ", "
                            + fieldFromXY.getPosY() + ")",
                    offsetX
                            + (Constants.FIELDS_IN_X_DIM * GUIConstants.STONE_WIDTH)
                            + 30,
                    (offsetY + (Constants.FIELDS_IN_Y_DIM * GUIConstants.STONE_HEIGHT)) - 5);
        }
    }

    private static Field getFieldUnderMouse(Component component, int offsetX,
            int offsetY, Board board, int xStart, int yStart, int width,
            int height) {
        Point mouseLocation = component.getMousePosition();

        if ((mouseLocation != null)
                && (mouseLocation.x >= offsetX)
                && (mouseLocation.x <= ((Constants.FIELDS_IN_X_DIM * GUIConstants.STONE_WIDTH) + offsetX))) {
            if ((mouseLocation.y >= offsetY)
                    && (mouseLocation.y <= ((Constants.FIELDS_IN_Y_DIM * GUIConstants.STONE_HEIGHT) + offsetY))) {
                return getBelongingFieldFromXY(board, xStart, yStart, width,
                        height, mouseLocation.x, mouseLocation.y);
            }
        }
        return null;
    }

    public static Field getBelongingField(Board board, int xStart, int yStart,
            int width, int height, GUIStone stone) {
        return getBelongingFieldFromXY(board, xStart, yStart, width, height,
                stone.getX(), stone.getY());
    }

    private static Field getBelongingFieldFromXY(Board board, int xStart,
            int yStart, int width, int height, int x, int y) {
        int offsetX = calculateOffsetX(xStart, width);
        int offsetY = calculateOffsetY(yStart, height);

        int xCalculated = (x - offsetX) / GUIConstants.STONE_WIDTH;
        int yCalculated = (y - offsetY) / GUIConstants.STONE_HEIGHT;

        try {
            return board.getField(xCalculated, yCalculated);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}
