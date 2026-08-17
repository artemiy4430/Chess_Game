package game.view;

import game.logic.*;

import java.util.List;

public class Table {
    private static final int boardLength = 8;
    private static final String horBar = "━";
    private static final String vertBar = "┃";
    private static final String upperLeftAngle = "┏";
    private static final String upperRightAngle = "┓";
    private static final String lowerLeftAngle = "┗";
    private static final String lowerRightAngle = "┛";
    private static final char[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};
    private static final int cellSpaces = 7;
    private static final int digits = 8;
    private static final int additionalBorderSpace = 2;
    private static final int tableSpaces = 30;

    private final int logBoardLength = boardLength;
    private String whiteName;
    private String blackName;

    // --- white color ---
    private static final String BG_WHITE = "\u001B[107m";
    private static final String FG_WHITE = "\u001b[100m";
    ;

    // --- black color ---
    private static final String FG_BLACK = "\033[30m";
    private static final String BG_BLACK = "\u001B[40m";

    // --- other colors ---
    private static final String blueColor = "\u001B[44m";
    private static final String resetColor = "\u001B[0m";
    private static final String FG_GREY = "\u001b[37m";
    private static final String darkBlue = "\u001b[34m";
    private static final String darkRed = "\u001b[31m";
    private static String cursorColor = "\u001B[42m" + " " + resetColor; // green
    private static final String lockedColor = "\u001B[41m" + " " + resetColor; // red
    private static final String lockedCellColor = "\u001b[45m" + " " + resetColor; // purple
    private static final String targetCellColor = "\u001b[103m" + " " + resetColor; // yellow (darker) (target cell)
    private static final String targetCellColorLight = "\u001b[48;5;143m" + " " + resetColor; // yellow (bright) (target cell)
    private static final String prevTargetCellColor = "\u001B[48;5;215m" + " " + resetColor; // light orange
    private static final String prevLockedCellColor = "\u001B[48;5;217m" + " " + resetColor;

    // --- print cell ---
    private static final String fullBlockBlue = blueColor + " " + resetColor;
    private static final String fullBlockWhite = BG_WHITE + " " + resetColor;

    private CellParts currentCellPart;

    // upper-figure-parts
    private static final String rookUpperSymbol = "ШШШ";
    private static final String queenUpperSymbol = "▄▀▄";
    private static final String bishopUpperSymbol = " ▲ ";
    //   private static final String pawnUpperSymbol = " O ";
    private static final String knightUpperSymbol = " ▄▄";
    private static final String kingUpperSymbol = "+++";

    //middle-figure-parts
    private static final String rookMiddleLine = "▄┃▄"; // for bishop as well
    private static final String queenMiddleLine = " ┃ "; // for king as well
    private static final String knightMiddleLine = "▀██";
    private static final String pawnMiddleLine = "▄O▄";


    //bottom-figure-parts
    private static final String rookBottomLine = "▅▅▅"; // knight, as well
    private static final String kingBottomLine = "ннн";
    private static final String queenBottomLine = "███"; // queen bishop pawn as well


    private void drawSpaces(int n) {
        for (int i = 0; i < n; i++) {
            System.out.print(" ");
        }
    }

    public void drawTable(Cursor cursor, Game game) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        drawSpaces(tableSpaces + tableSpaces / 2);
        drawLetters();
        System.out.println();
        drawSpaces(tableSpaces + (tableSpaces / 4));
        drawSpaces(digits);
        drawTableBorder(upperLeftAngle, upperRightAngle);
        System.out.println();
        drawSpaces(tableSpaces / 2);

        int digit = boardLength;
        boolean flag = true;
        boolean isWhiteTurn = game.getCurrentTurn() == Color.WHITE;

        for (int j = 0, currentCellPartNumber = 0, counter = 1, y = 0; j < boardLength * 3;
             j++, counter++, currentCellPartNumber++) {
            //   isLinePrinted = j % 2 == 0;

            if (j != 0 && j % 3 == 0) {
                y++;
                currentCellPartNumber = 0;
            }

            if (counter % 2 == 0) {
                drawSpaces(tableSpaces - additionalBorderSpace);
                System.out.print(digit--);
                drawSpaces(additionalBorderSpace - 1);
            } else drawSpaces(tableSpaces);

            this.currentCellPart = CellParts.values()[currentCellPartNumber];
            drawContent(flag, cursor, y, game, this.currentCellPart);
            drawSpaces(tableSpaces / 2);

            if (counter > 2) {
                counter = 0;
                flag = !flag;
            }
        }

        drawSpaces(tableSpaces);
        drawTableBorder(lowerLeftAngle, lowerRightAngle);

        for (int i = 0; i < 3; i++) { // temporary
            System.out.println();
        }

    }

    private void drawTableBorder(String leftPart, String rightAngle) {
        System.out.print(leftPart);
        int totalWidth = (boardLength * cellSpaces) + additionalBorderSpace;
        for (int i = 0; i < totalWidth; i++) {
            System.out.print(horBar);
        }
        System.out.print(rightAngle);
    }

    // private void drawTableBorder(String leftPart, String rightAngle, int customWidth) { // LOG
    //     System.out.print(leftPart);
    //     int totalWidth = (customWidth * customWidth - 1) + additionalBorderSpace;
    //     for (int i = 0; i < totalWidth; i++) {
    //         System.out.print(horBar);
    //     }
    //     System.out.print(rightAngle);
    // }


    private void drawLetters() {
        int spacesBetweenLetters = cellSpaces / 2;
        drawSpaces(spacesBetweenLetters + additionalBorderSpace);
        for (int i = 0; i < letters.length; i++) {
            System.out.print(letters[i]);
            if (i != letters.length - 1) drawSpaces(boardLength - additionalBorderSpace);
            //   else System.out.println();
        }
    }

    private String getFigureColor(Figure figure) {
        if (figure == null) return null;
        return (figure.getColor() == Color.WHITE) ? FG_GREY : FG_BLACK;
    }

    private void drawContent(boolean startColor, Cursor cursor, int currentY, Game game, CellParts currentCellPart) {
        int cursorCoordinateX = cursor.getCursorCoordinateX();
        int cursorCoordinateY = cursor.getCursorCoordinateY();
        int addSpaceAmount = additionalBorderSpace / 2;
        boolean isOnCursorRow = false;
        boolean isBlue = startColor;
        Field field = game.getField();


        System.out.print(vertBar);
        drawSpaces(addSpaceAmount);
        if (currentY == cursorCoordinateY) {
            isOnCursorRow = true;
        }

        for (int i = 0; i < boardLength; i++) {
            isBlue = !isBlue;
            boolean isTargetCell = false;
            boolean isLockedCell = false;
            boolean isCursorCell = false;
            boolean isLockedCursorCell = false;
            boolean isPrevLockedCell = false;
            boolean isPrevTargetCell = false;
            boolean isDarkTargetCell = (i + currentY) % 2 == 0;
            Coordinates currentCoords = new Coordinates(i, currentY);
            Cell currentCell = field.getCell(currentCoords);
            Coordinates lockedCoords = (game.isLocked()) ? game.getLockedFigureCoordinates() : null;
            Coordinates prevLockedCoords = game.getPrevlockedFigureCoordinates();
            Coordinates prevTargetCoords = game.getPrevtargetFigureCoordinates();
            Figure figure = currentCell.getFigure();
            List<Coordinates> figureMoves = (game.isLocked()) ? game.getCurrentAvailableMoves() : null;
            String bgColor;

            for (int j = 0; j < cellSpaces; ) { // j++ to replace
                boolean flag = true;

                if (!game.isBotGame()) {
                    if (isOnCursorRow && i == cursorCoordinateX) {
                        if (!game.isLocked()) {
                            bgColor = cursorColor;
                            isCursorCell = true;
                        } else {
                            bgColor = lockedColor;
                            isLockedCursorCell = true;
                        }
                        //   bgColor = (!game.isLocked()) ? cursorColor : lockedColor;
                    } else if (prevLockedCoords != null && prevLockedCoords.equals(currentCoords)) {
                        bgColor = prevLockedCellColor;
                        isPrevLockedCell = true;
                    }  else if (prevTargetCoords != null && prevTargetCoords.equals(currentCoords)) {
                        bgColor = prevTargetCellColor;
                        isPrevTargetCell = true;
                    } else if (game.isLocked() && figureMoves.contains(new Coordinates(i, currentY))) {
                        if (!isDarkTargetCell) {
                            bgColor = targetCellColor;
                        } else {
                            bgColor = targetCellColorLight;
                        }
                        isTargetCell = true;

                    } else if (game.isLocked() && lockedCoords.equals(currentCoords)) {
                        bgColor = lockedCellColor;
                        isLockedCell = true;
                    }  else bgColor = (isBlue) ? fullBlockBlue : fullBlockWhite;
                } else if (prevLockedCoords != null && prevLockedCoords.equals(currentCoords)) {
                    bgColor = prevLockedCellColor;
                    isPrevLockedCell = true;
                }  else if (prevTargetCoords != null && prevTargetCoords.equals(currentCoords)) {
                    bgColor = prevTargetCellColor;
                    isPrevTargetCell = true;
                } else bgColor = (isBlue) ? fullBlockBlue : fullBlockWhite;

                if (figure != null) {
                    if (cellSpaces - j == 7 || cellSpaces - j == 1) {
                        System.out.print(bgColor);
                    } else {
                        if (currentCellPart == CellParts.UPPER && figure.getType() == FigureType.PAWN) {
                            //  bgColor += " " + resetColor;
                        } else if (!game.isBotGame()) {
                            if (isCursorCell) {
                                bgColor = "\u001B[42m";
                            } else if (isLockedCell) {
                                bgColor = "\u001b[45m";
                            } else if (isTargetCell) {
                                bgColor = (isDarkTargetCell) ? "\u001b[48;5;143m" : "\u001b[103m";
                            } else if (isLockedCursorCell) {
                                bgColor = "\u001b[41m";
                            } else if (isPrevTargetCell) {
                                bgColor = "\u001B[48;5;215m";
                            } else if (isPrevLockedCell) {
                                bgColor = "\u001B[48;5;217m";
                            } else bgColor = (isBlue) ? blueColor : BG_WHITE;
                        } else if (isPrevTargetCell) {
                            bgColor = "\u001B[48;5;215m";
                        } else if (isPrevLockedCell) {
                            bgColor = "\u001B[48;5;217m";
                        } else bgColor = (isBlue) ? blueColor : BG_WHITE;

                        drawFigure(figure, currentCellPart, bgColor);
                        j += 5;
                        flag = false;

                    }
                } else System.out.print(bgColor);
                //  } else System.out.print(bgColor);
                if (flag) j++;
            }
        }
        drawSpaces(addSpaceAmount);
        System.out.print(vertBar);
        System.out.println();
    }

    public void drawFigure(Figure figure, CellParts cellParts, String bgColor) { // to be implemented
        if (figure == null) return;
        String color = getFigureColor(figure);

        switch (figure.getType()) {
            case ROOK -> drawRook(cellParts, bgColor, color);
            case KNIGHT -> drawKnight(cellParts, bgColor, color);
            case BISHOP -> drawBishop(cellParts, bgColor, color);
            case QUEEN -> drawQueen(cellParts, bgColor, color);
            case PAWN -> drawPawn(cellParts, bgColor, color);
            case KING -> drawKing(cellParts, bgColor, color);
        }
    }

    private void drawRook(CellParts cellParts, String bgColor, String figureColor) {
        switch (cellParts) {
            case UPPER -> System.out.print(bgColor + (figureColor + " " + rookUpperSymbol + " " + resetColor));
            case MIDDLE -> System.out.print(bgColor + (figureColor + " " + rookMiddleLine + " " + resetColor));
            case BOTTOM -> System.out.print(bgColor + (figureColor + " " + rookBottomLine + " " + resetColor));
        }
    }

    private void drawKnight(CellParts cellParts, String bgColor, String figureColor) {
        switch (cellParts) {
            case UPPER -> System.out.print(bgColor + (figureColor + " " + knightUpperSymbol + " " + resetColor));
            case MIDDLE -> System.out.print(bgColor + (figureColor + " " + knightMiddleLine + " " + resetColor));
            case BOTTOM -> System.out.print(bgColor + (figureColor + " " + rookBottomLine + " " + resetColor));
        }
    }

    private void drawBishop(CellParts cellParts, String bgColor, String figureColor) {
        switch (cellParts) {
            case UPPER -> System.out.print(bgColor + (figureColor + " " + bishopUpperSymbol + " " + resetColor));
            case MIDDLE -> System.out.print(bgColor + (figureColor + " " + rookMiddleLine + " " + resetColor));
            case BOTTOM -> System.out.print(bgColor + (figureColor + " " + queenBottomLine + " " + resetColor));
        }
    }

    private void drawQueen(CellParts cellParts, String bgColor, String figureColor) {
        switch (cellParts) {
            case UPPER -> System.out.print(bgColor + (figureColor + " " + queenUpperSymbol + " " + resetColor));
            case MIDDLE -> System.out.print(bgColor + (figureColor + " " + queenMiddleLine + " " + resetColor));
            case BOTTOM -> System.out.print(bgColor + (figureColor + " " + queenBottomLine + " " + resetColor));
        }
    }

    private void drawKing(CellParts cellParts, String bgColor, String figureColor) {
        switch (cellParts) {
            case UPPER -> System.out.print(bgColor + (figureColor + " " + kingUpperSymbol + " " + resetColor));
            case MIDDLE -> System.out.print(bgColor + (figureColor + " " + queenMiddleLine + " " + resetColor));
            case BOTTOM -> System.out.print(bgColor + (figureColor + " " + kingBottomLine + " " + resetColor));
        }
    }

    private void drawPawn(CellParts cellParts, String bgColor, String figureColor) {
        switch (cellParts) {
            case UPPER -> {
                for (int i = 0; i < 5; i++) {
                    System.out.print(bgColor);
                }
            }
            case MIDDLE -> System.out.print(bgColor + (figureColor + " " + pawnMiddleLine + " " + resetColor));
            case BOTTOM -> System.out.print(bgColor + (figureColor + " " + queenBottomLine + " " + resetColor));
        }
    }


    // private void drawContent(String move, boolean isLinePrinted, boolean isWhite) { // LOG
    //     int addSpaceAmount = additionalBorderSpace / 2;
    //     int internalWidth = (logBoardLength * logBoardLength - 1);
    //     System.out.print(vertBar);
    //     drawSpaces(addSpaceAmount);
//
    //     if (isLinePrinted) {
    //         System.out.print(((isWhite) ? darkBlue : darkRed) + move + resetColor);
    //         int spacesNeeded = internalWidth - move.length();
    //         if (spacesNeeded > 0) {
    //             drawSpaces(spacesNeeded);
    //         }
    //     } else {
    //         drawSpaces(internalWidth);
    //     }
    //     drawSpaces(addSpaceAmount);
    // }

}
