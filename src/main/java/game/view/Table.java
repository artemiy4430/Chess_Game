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
    private static final String dot = "•";
    private final int logBoardLength = boardLength;
    private String whiteName;
    private String blackName;

    // --- white color ---
    private static final String BG_WHITE = "\u001B[107m";
    private static final String FG_WHITE = "\u001b[97m";

    // --- black color ---
    private static final String FG_BLACK = "\033[30m";
    private static final String BG_BLACK = "\u001B[40m";

    // --- other colors ---
    private static final String blueColor = "\u001B[44m";
    private static final String resetColor = "\u001B[0m";
    //private static final String FG_GREY = "\u001b[100m";
    private static final String darkBlue = "\u001b[34m";
    private static final String darkRed = "\u001b[31m";
    private static final String cursorColor = "\u001B[42m" + " " + resetColor; // green
    private static final String lockedColor = "\u001B[41m" + " " + resetColor; // red
    private static final String lockedCellColor = "\u001b[45m" + " " + resetColor; // purple
    private static final String targetCellColor = "\u001b[43m" + " " + resetColor; // yellow (dog piss) (target cell)

    // --- print cell ---
    private static final String fullBlockBlue = blueColor + " " + resetColor;
    private static final String fullBlockWhite = BG_WHITE + " " + resetColor;


    private void drawSpaces(int n) {
        for (int i = 0; i < n; i++) {
            System.out.print(" ");
        }
    }

    public void drawTable(Cursor cursor, Game game) {
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
        boolean isLinePrinted;
        boolean isWhiteTurn = game.getCurrentTurn() == Color.WHITE;

        for (int j = 0, counter = 1, y = 0; j < boardLength * 3; j++, counter++) {
            boolean isCheckerLine = false;
            isLinePrinted = j % 2 == 0;

            if (j != 0 && j % 3 == 0) y++;
            if (counter % 2 == 0) {
                isCheckerLine = true;
                drawSpaces(tableSpaces - additionalBorderSpace);
                System.out.print(digit--);
                drawSpaces(additionalBorderSpace - 1);
            } else drawSpaces(tableSpaces);

            drawContent(flag, cursor, y, game, isCheckerLine);
            drawSpaces(tableSpaces / 2);

            if (counter > 2) {
                counter = 0;
                flag = !flag;
            }
        }

        drawSpaces(tableSpaces);
        drawTableBorder(lowerLeftAngle, lowerRightAngle);
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
        return (figure.getColor() == Color.WHITE) ? BG_WHITE : FG_BLACK;
    }

    private void drawContent(boolean startColor, Cursor cursor, int currentY, Game game, boolean isCheckerLine) {
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

            for (int j = 0; j < cellSpaces; j++) {
                Coordinates currentCoords = new Coordinates(i, currentY);
                Cell currentCell = field.getCell(currentCoords);
                Coordinates lockedCoords = (game.isLocked()) ? game.getLockedFigureCoordinates() : null;
                Figure figure = currentCell.getFigure();
                List<Coordinates> figureMoves = (game.isLocked()) ? game.getCurrentAvailableMoves() : null;
                String bgColor;

              //  if (isOnCursorRow && i == cursorCoordinateX) {
              //      bgColor = (!game.isLocked()) ? cursorColor : lockedColor;
              //  } else if (game.isLocked() && figureMoves.contains(new Coordinates(i, currentY))) {
              //      bgColor = targetCellColor;
              //  } else if (game.isLocked() && lockedCoords.equals(currentCoords)) {
              //      bgColor = lockedCellColor;
              //  } else
                    bgColor = (isBlue) ? fullBlockBlue : fullBlockWhite;

               // if (checker != null && isCheckerLine) {
               //     if (cellSpaces - j < 3 || cellSpaces - j > 5) System.out.print(bgColor);
               //     else if (checker.isActive()) {
               //         drawFigure(checker);
               //     } else if (flag) {
               //         drawFigure(checker);
               //         setInactiveDrawn(true);
               //     } else System.out.print(bgColor);
               // } else
                    System.out.print(bgColor);
            }
        }
        drawSpaces(addSpaceAmount);
        System.out.print(vertBar);
        System.out.println();
    }



    // public void drawFigure(Figure figure) { // to be implemented
    //     if (checker == null) return;
    //     String color = getCheckerColor(checker);
    //     if (!checker.isQueen()) {
    //         System.out.print(color + " " + resetColor);
    //     } else drawQueen(checker);
    // }

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
