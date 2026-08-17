package game.logic;

import game.logic.moves.Moves;
import game.logic.moves.piecemoves.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class MatchManager {

    private Field field;
    private Color currentTurn;
    private boolean isUnderCheck;
    private Color winner;
    private boolean isTie;
    private boolean isStaleMate;
    private boolean isQueenPromoted;
    private int consecutiveMovesWhite = 0;
    private int consecutiveMovesBlack = 0;
    private boolean currMoveIsAttack;
    private Figure prevCheckerUsedWhite = null;
    private Figure prevCheckerUsedBlack = null;
    private GameStage stage;
    private int turnCounter;
    private int whiteTurnPoints = 0;
    private int blackTurnPoints = 0;

    public MatchManager(Field field) {
        this.field = field;
        this.stage = GameStage.START;
        this.turnCounter = 0;
    }

    public MatchManager(final MatchManager manager) {
        this.field = new Field(manager.getField());
        this.stage = manager.getStage();
        this.turnCounter = manager.getTurnCounter();
        this.currentTurn = manager.getCurrentTurn();
        this.isUnderCheck = manager.isUnderCheck();
        this.consecutiveMovesWhite = manager.getConsecutiveMovesWhite();
        this.consecutiveMovesBlack = manager.getConsecutiveMovesBlack();
        this.currMoveIsAttack = manager.isCurrMoveIsAttack();
        this.whiteTurnPoints = manager.getWhiteTurnPoints();
        this.blackTurnPoints = manager.getBlackTurnPoints();
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }


    public boolean endGameCheck() {
        return checkWin() || checkTie();
    }

//    public Color setWinner() {
//        return this.currentTurn == Color.WHITE ? Color.BLACK : Color.WHITE;
//    }

    public Color getWinner() {
        return winner;
    }

    public boolean checkWin() { // getWinner is called if checkWin = true
        boolean flag = isTurnMovable();

        if (isUnderCheck && !flag) {
            setWinner(currentTurn);
            return true;
        }
        return false;
    }

    private boolean isTurnMovable() {
        Color oppositeTurn = (this.currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;

        //IntStream -> Stream<Integer>
        return IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)))
                .anyMatch(coords -> {
                    Figure figure = field.getFigure(coords);

                    if (figure != null && figure.getColor() == oppositeTurn) {
                        List<Coordinates> legalMoves = filterMoves(coords);

                        if (!legalMoves.isEmpty()) {
                            return true;
                        }
                    }

                    return false;
                });
    }

    public boolean checkTie() {
        if (isStaleMateCheck()) {
            setStaleMate(true);
            return true;

        } else if (isTieCheck()) {
            setTie(true);
            return true;
        }

        return false;
    }


    private boolean isTieCheck() { // if there are certain pieces left on the board два короля+, король слон против короля+,
        // король конь против короля+, король и два коня против короля+, два разнопольных слона+
        return isTieByOneBishopOrKnight() || isTieByTwoBishopsSameCellColor() || isTieByTwoKings();

    }

    private boolean isTieByTwoKings() { // Ensures EXACTLY 2 pieces remain on the entire board
        List<Figure> figuresList = IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> field.getFigure(new Coordinates(j, i))))
                .filter(Objects::nonNull).toList();

        if (figuresList.size() != 2) return false;

        return true;
    }


    private boolean isTieByTwoBishopsSameCellColor() { // два однопольных слона
        List<Figure> figuresList = IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> field.getFigure(new Coordinates(j, i))))
                .filter(Objects::nonNull).toList();

        if (figuresList.size() != 4) return false;

        List<Figure> bishops = figuresList.stream().filter(x -> x.getType() == FigureType.BISHOP).toList();

        boolean bishopsOrKings = figuresList.stream().allMatch(x -> x.getType() == FigureType.BISHOP
                || x.getType() == FigureType.KING);

        if (!bishopsOrKings) return false;
        Color bishop1CellColor = field.getCell(bishops.get(0)).getColor();
        Color bishop2CellColor = field.getCell(bishops.get(1)).getColor();

        return bishop1CellColor == bishop2CellColor;


    }

    private boolean isTieByOneBishopOrKnight() {
        List<Figure> figuresOnBoard = IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> field.getFigure(new Coordinates(j, i))))
                .filter(Objects::nonNull).toList();

        if (figuresOnBoard.size() != 3) return false;

        return figuresOnBoard.stream().allMatch(figure ->
                figure.getType() == FigureType.KING ||
                        figure.getType() == FigureType.BISHOP ||
                        figure.getType() == FigureType.KNIGHT
        );

    }

    private boolean isStaleMateCheck() {
        return !isTurnMovable();
    } // if staleMate


    public boolean isValidMove(Coordinates startCoords, Coordinates endCoords) {
        Figure figure = field.getFigure(startCoords);

        if (getMovementType(figure).getAvailableMoves(startCoords).contains(endCoords)) return true;

        return false;
    }


    public void processTargetCells(Color currentTurn) { // dlya korolya chtobi smotret chto pod boyem,
        // potom v konce hoda ono budet ochischatsa

        for (int i = 0; i < field.getSize(); i++) {
            for (int j = 0; j < field.getSize(); j++) {
                Figure currentFigure = field.getFigure(new Coordinates(j, i));

                if (currentFigure != null && currentFigure.getColor() == currentTurn) {
                    Moves moves = getMovementType(currentFigure);
                    List<Coordinates> availableMoves = moves.getAvailableMoves(new Coordinates(j, i));

                    if (availableMoves != null && !availableMoves.isEmpty()) {
                        for (int k = 0; k < availableMoves.size(); k++) {
                            Coordinates coordinates = availableMoves.get(k);
                            Cell cell = field.getCell(coordinates);

                            if (currentFigure.getType() == FigureType.PAWN) {
                                if (coordinates.isAttackCoordinate()) {
                                    field.getCell(coordinates).setAttacked(true);
                                }
                            } else cell.setAttacked(true);
                        }
                    }
                }
            }
        }
    }

    public void clearTargetCells() { // pered peredachey choda vizvat'
        for (int i = 0; i < field.getSize(); i++) {
            for (int j = 0; j < field.getSize(); j++) {
                Cell cell = field.getCell(new Coordinates(j, i));

                if (cell.isAttacked()) cell.setAttacked(false);
            }
        }
    }

    private Moves getMovementType(Figure figure) {
        return switch (figure.getType()) {
            case PAWN -> new PawnMovement(field);
            case KING -> new KingMovement(field);
            case ROOK -> new RookMovement(field);
            case KNIGHT -> new KnightMovement(field);
            case BISHOP -> new BishopMovement(field);
            case QUEEN -> new QueenMovement(field);
        };
    }

    public Coordinates getKing(Color turn) {

        return IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)))
                .filter(x -> field.getFigure(x) != null
                        && field.getFigure(x).getColor() == turn && field.getFigure(x).getType() == FigureType.KING)
                .findFirst()
                .orElse(null);
    }

    public void isKingAttacked(Color turn) { // вызывается после совершенного хода проверяется противоположный цвет короля
        setUnderCheck(field.getCell(getKing((turn == Color.WHITE ? Color.BLACK : Color.WHITE))).isAttacked());
    }

    private void setUnderCheck(boolean underCheck) {
        isUnderCheck = underCheck;
    }

    public boolean isUnderCheck() {
        return isUnderCheck;
    }

    public List<Coordinates> filterMoves(Coordinates coordinates) {
        Figure figure = field.getFigure(coordinates);
        List<Coordinates> figureAvailableMoves = getMovementType(figure).getAvailableMoves(coordinates);
        List<Coordinates> filteredMoves = new ArrayList<>();
        Color currentColor = figure.getColor();
        boolean isKing = figure.getType().equals(FigureType.KING);
        boolean isPawn = figure.getType().equals(FigureType.PAWN);

        for (int i = 0; i < figureAvailableMoves.size(); i++) {
            Coordinates target = figureAvailableMoves.get(i);
            Figure removedFigure = field.getFigure(target);
            if (removedFigure != null && removedFigure.getType() == FigureType.KING) {
                continue;
            }

            boolean isAttack = removedFigure != null;

            if (isAttack) {
                field.removeFigure(target);
            }
            field.setFigure(coordinates, target);

            if (!isKingUnderCheck(currentColor)) {
                filteredMoves.add(target);
            }

            field.setFigure(target, coordinates);

            if (isAttack) {
                field.setFigure(removedFigure, target);
            }
        }

        if (isKing) {
            int kingY = coordinates.getCoordinateY();
            int[] rookXPositions = {7, 0};

            for (int rookX : rookXPositions) {
                Coordinates rookCoords = new Coordinates(rookX, kingY);
                Figure currentFigure = field.getFigure(rookCoords);

                if (currentFigure != null && currentFigure.getColor() == currentColor
                        && currentFigure.getType() == FigureType.ROOK) {
                    if (isValidCastle(rookCoords, currentColor)) {

                        if (rookX == 7) {
                            filteredMoves.add(new Coordinates(coordinates.getCoordinateX() + 2, kingY));
                        } else {
                            filteredMoves.add(new Coordinates(coordinates.getCoordinateX() - 2, kingY));
                        }
                    }
                }
            }
        } else if (isPawn) {
            int pawnY = coordinates.getCoordinateY();
            List<Integer> enPassantPositions = List.of(-1, 1);

            for (int direction : enPassantPositions) {
                Coordinates landingCoordinates = new Coordinates(coordinates.getCoordinateX() + direction,
                        (currentColor == Color.WHITE) ? pawnY - 1 : pawnY + 1);
                Coordinates targetFigureCoordinates = new Coordinates(landingCoordinates.getCoordinateX(), pawnY);

                if (!field.isWithinBoard(targetFigureCoordinates)) continue;
                Figure currentFigure = field.getFigure(targetFigureCoordinates);

                if (currentFigure != null && isValidEnPassant(coordinates, landingCoordinates, currentColor)) {
                    Coordinates enPassantCoordinates = new Coordinates(landingCoordinates.getCoordinateX(),
                            (currentColor == Color.WHITE) ? pawnY - 1 : pawnY + 1);
                    enPassantCoordinates.setAttackCoordinate(true);
                    enPassantCoordinates.setEnpassant(true);
                    filteredMoves.add(enPassantCoordinates);
                }
            }

        }
        return filteredMoves;
    }

    public boolean isValidEnPassant(Coordinates coordinatesFrom, Coordinates coordinatesTo, Color turn) {
        if (coordinatesFrom == null || field.getFigure(coordinatesFrom) == null || coordinatesTo == null)
            return false;
        Figure movingFigure = field.getFigure(coordinatesFrom);

        if (movingFigure.getType() != FigureType.PAWN || movingFigure.getColor() != turn) return false;
        int pawnY = coordinatesFrom.getCoordinateY();

        if (turn == Color.WHITE && pawnY != 3 || turn == Color.BLACK && pawnY != 4) return false;
        Coordinates targetFigureCoordinates = new Coordinates(coordinatesTo.getCoordinateX(), pawnY);
        Figure targetFigure = field.getFigure(targetFigureCoordinates);

        if (targetFigure == null || targetFigure.getColor() == turn || targetFigure.getType() != FigureType.PAWN)
            return false;

        if (targetFigure.isLastMoved() && targetFigure.getMoveCount() == 1) {
            if (field.getFigure(coordinatesTo) == null) return true;
        }

        return false;
    }

    public boolean isKingUnderCheck(Color kingColor) {
        Coordinates kingCoordinates = getKing(kingColor);
        if (kingCoordinates == null) return false;
        Color attackerColor = (kingColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

        for (int i = 0; i < field.getSize(); i++) {
            for (int j = 0; j < field.getSize(); j++) {
                Coordinates cellCoordinates = new Coordinates(j, i);
                Figure figure = field.getFigure(cellCoordinates);

                if (figure != null && figure.getColor().equals(attackerColor)) {
                    List<Coordinates> figureAvailableMoves = getMovementType(figure).getAvailableMoves(cellCoordinates);

                    if (figureAvailableMoves != null && !figureAvailableMoves.isEmpty()) {
                        if (figure.getType() == FigureType.PAWN) {
                            int index = figureAvailableMoves.indexOf(kingCoordinates);
                            if (index > -1 && figureAvailableMoves.get(index).isAttackCoordinate()) {
                                return true;
                            }
                        } else if (figureAvailableMoves.contains(kingCoordinates)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public Color getOppositeColor(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    public boolean isValidCastle(Coordinates kingTo, Color turn) {
        if (isUnderCheck) return false;
        Coordinates kingCoordinates = getKing(turn);
        Coordinates rookCoordinates = getCastlingRook(kingTo, turn);

        if (kingCoordinates == null || rookCoordinates == null) return false;
        Figure kingFigure = field.getFigure(kingCoordinates);
        Figure rookFigure = field.getFigure(rookCoordinates);

        if (kingFigure == null || rookFigure == null) return false;
        if (kingFigure.getType() != FigureType.KING || kingFigure.getColor() != turn
                || rookFigure.getType() != FigureType.ROOK || rookFigure.getColor() != turn) return false;
        int kingX = kingCoordinates.getCoordinateX();
        int rookX = rookCoordinates.getCoordinateX();

        int kingY = kingCoordinates.getCoordinateY();
        int rookY = rookCoordinates.getCoordinateY();

        if (kingY != rookY) return false;

        boolean isShortCastle = kingTo.getCoordinateX() > kingX;
        int max = ((isShortCastle) ? 2 : 3);

        if (!kingFigure.isMoved() && !rookFigure.isMoved()) {
            for (int i = 1; i <= max; i++) {
                int currentCoordinateX = (isShortCastle) ? kingX + i : kingX - i;
                Coordinates checkCoords = new Coordinates(currentCoordinateX, kingY);

                if (!field.isWithinBoard(checkCoords) || field.getFigure(checkCoords) != null) return false;
                if (i <= 2 && field.getCell(checkCoords).isAttacked()) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

//    private boolean isValidCastle(int rookX, Color turn) {
//        if (this.isUnderCheck) return false;
//        Coordinates kingCoordinates = getKing(turn);
//
//        if (kingCoordinates == null) return false;
//        Coordinates rookCoordinates = new Coordinates(rookX, kingCoordinates.getCoordinateY());
//
//        Figure kingFigure = field.getFigure(kingCoordinates);
//        Figure rookFigure = field.getFigure(rookCoordinates);
//
//        if (kingFigure == null || rookFigure == null) return false;
//        if (kingFigure.getType() != FigureType.KING || kingFigure.getColor() != turn
//                || rookFigure.getType() != FigureType.ROOK || rookFigure.getColor() != turn) return false;
//        int kingX = kingCoordinates.getCoordinateX();
//
//        int kingY = kingCoordinates.getCoordinateY();
//        int rookY = rookCoordinates.getCoordinateY();
//
//        if (kingY != rookY) return false;
//        int distance = getDistanceWithKing(kingX, rookX);
//        boolean isShortCastle = distance < 4;
//        int max = ((isShortCastle) ? 2 : 3);
//
//        if (!kingFigure.isMoved() && !rookFigure.isMoved()) {
//            for (int i = 1; i < max; i++) {
//                int currentCoordinateX = (isShortCastle) ? kingX + i : kingX - i;
//                Coordinates checkCoords = new Coordinates(currentCoordinateX, kingY);
//
//                if (!field.isWithinBoard(checkCoords) || field.getFigure(checkCoords) != null
//                        || field.getCell(checkCoords).isAttacked()) return false;
//            }
//
//            return true;
//        }
//
//        return false;
//    }

//    public int getDistanceWithKing(int kingX, int coordinateX) {
//        return Math.abs(kingX - coordinateX);
//    }

    public Coordinates getCastlingRook(Coordinates kingTo, Color turn) {
        if (kingTo == null) return null;
        Coordinates kingCoordinates = getKing(turn);

        if (kingCoordinates == null) return null;
        if (kingCoordinates.getCoordinateY() != kingTo.getCoordinateY()) return null;
        boolean isShortCastle = kingTo.getCoordinateX() > kingCoordinates.getCoordinateX();
        int rookX = isShortCastle ? 7 : 0;

        return new Coordinates(rookX, kingCoordinates.getCoordinateY());
    }

//    public void promoteFigure(Coordinates coords) { // for user (temporary)
//        Figure figure = field.getFigure(coords);
//        figure.promoteToQueen();
//        //  isQueenPromoted = true; for move logging
//    }


    public void promoteFigure(Coordinates coords, FigureType selectedType) { // for bot
        if (selectedType == null || coords == null) return;

        Figure figure = field.getFigure(coords);
        figure.promoteFigure(selectedType);
    }

    public boolean isQueenPromotable(Coordinates coords) {
        Figure figure = field.getFigure(coords);

        if (figure == null || figure.getType() != FigureType.PAWN) return false;

        return ((figure.getColor() == Color.BLACK && coords.getCoordinateY() == 7) ||
                (figure.getColor() == Color.WHITE && coords.getCoordinateY() == 0));

    }

    public boolean isQueenPromotable(Coordinates targetFigureCoordinates, Figure movingFigure) {

        if (movingFigure == null || movingFigure.getType() != FigureType.PAWN) return false;

        return ((movingFigure.getColor() == Color.BLACK && targetFigureCoordinates.getCoordinateY() == 7) ||
                (movingFigure.getColor() == Color.WHITE && targetFigureCoordinates.getCoordinateY() == 0));

    }

    public void clearLastMovedFigure(Color turn) {
        IntStream.range(0, field.getSize())
                .forEach(i -> IntStream.range(0, field.getSize())
                        .forEach(j -> {
                                    Coordinates figureCoordinates = new Coordinates(j, i);
                                    Figure currentFigure = field.getFigure(figureCoordinates);

                                    if (currentFigure != null && currentFigure.getColor() == turn) {
                                        if (currentFigure.isLastMoved()) {
                                            currentFigure.setLastMoved(false);
                                        }
                                    }

                                }
                        ));
    }

    public void updateStage() {
        this.stage = calculateGameStage();
    }

    private GameStage calculateGameStage() {
        List<Figure> activeFigures = IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> field.getFigure(new Coordinates(j, i))))
                .filter(Objects::nonNull)
                .toList();

        int nonPawnMaterialWhite = calculateMaterial(activeFigures, Color.WHITE);
        int nonPawnMaterialBlack = calculateMaterial(activeFigures, Color.BLACK);

        //    System.out.println(nonPawnMaterialWhite + " " + nonPawnMaterialBlack);
        if ((nonPawnMaterialBlack <= 1150 || nonPawnMaterialWhite <= 1150) && stage == GameStage.MIDGAME) {
            return GameStage.ENDGAME;
        }

        if (stage == GameStage.START && (this.turnCounter > 8 || isMidGameDevelopment(activeFigures))) {
            return GameStage.MIDGAME;
        }

        return this.stage;
    }

    private int calculateMaterial(List<Figure> activeFigures, Color turn) {
        return activeFigures.stream()
                .filter(f -> f.getType() != FigureType.PAWN && f.getType() != FigureType.KING && f.getColor() == turn)
                .mapToInt(Figure::getPrice).sum() * 100;
    }

    private boolean isMidGameDevelopment(List<Figure> figures) {
        long movedPiecesCount = figures.stream()
                .filter(f -> f.getType() != FigureType.PAWN && f.getType() != FigureType.KING)
                .filter(Figure::isMoved)
                .count();

        return movedPiecesCount >= 3;
    }

//    private boolean isEndGameMaterial(List<Figure> figures) {
//        boolean whiteQueenPresent = figures.stream()
//                .anyMatch(f -> f.getColor() == Color.WHITE && f.getType() == FigureType.QUEEN);
//        boolean blackQueenPresent = figures.stream()
//                .anyMatch(f -> f.getColor() == Color.BLACK && f.getType() == FigureType.QUEEN);
//
//        return !whiteQueenPresent && !blackQueenPresent;
//    }


    public void setCurrMoveIsAttack(boolean currMoveIsAttack) {
        this.currMoveIsAttack = currMoveIsAttack;
    }

    boolean consecutiveMovesUpdate() {
        switch (this.currentTurn) {
            case Color.WHITE:
                this.consecutiveMovesWhite++;
            case Color.BLACK:
                this.consecutiveMovesBlack++;
        }
        return this.consecutiveMovesWhite >= 7 && this.consecutiveMovesBlack >= 7;
    }

    public void consecutiveMovesReset(Color turn) {
        if (turn == Color.WHITE) {
            setConsecutiveMovesWhite(0);
        } else setConsecutiveMovesBlack(0);
    }

    public void incrementPoints(Figure capturedFigure) {
        if (currentTurn == Color.WHITE) {
            whiteTurnPoints += (capturedFigure != null) ? capturedFigure.getPrice() : 1;
        } else {
            blackTurnPoints += (capturedFigure != null) ? capturedFigure.getPrice() : 1;
        }
    }


    public List<Coordinates> getAllAvailableAttackMoves(Color turn) {
        List<Coordinates> figures = IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)))
                .filter(x -> field.getFigure(x) != null && field.getFigure(x).getColor() == turn)
                .toList();

        return figures.stream()
                .flatMap(coordinates -> getMovementType(field.getFigure(coordinates))
                        .getAvailableMoves(coordinates).stream())
                .filter(Coordinates::isAttackCoordinate).toList();
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public List<Coordinates> getAvailableFigureCoordinates(Color turn) {
        return IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)))
                .filter(x -> {
                    Figure figure = field.getFigure(x);

                    if (figure != null && figure.getColor() == turn) {
                        List<Coordinates> availableMoves = filterMoves(x);

                        if (availableMoves == null || availableMoves.isEmpty()) return false;

                        return true;
                    }

                    return false;
                }).toList();
    }

    public boolean isProtectingSquare(Coordinates protectorCoord, Coordinates targetCoord) {
        Figure protector = field.getFigure(protectorCoord);
        if (protector == null) return false;

        if (protector.getType() == FigureType.PAWN) {
            int dy = (protector.getColor() == Color.WHITE) ? -1 : 1;
            int targetY = protectorCoord.getCoordinateY() + dy;
            int targetX1 = protectorCoord.getCoordinateX() - 1;
            int targetX2 = protectorCoord.getCoordinateX() + 1;

            return targetCoord.getCoordinateY() == targetY &&
                    (targetCoord.getCoordinateX() == targetX1 || targetCoord.getCoordinateX() == targetX2);
        } else {
            List<Coordinates> availableMoves = getMovementType(protector).getAvailableMoves(protectorCoord);
            if (availableMoves == null) return false;
            for (Coordinates coordinates : availableMoves) {
                if (coordinates.equals(targetCoord)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setCurrentTurn(Color currentTurn) {
        this.currentTurn = currentTurn;
    }

    public boolean isTie() {
        return isTie;
    }

    public boolean isStaleMate() {
        return isStaleMate;
    }

    public void setWinner(Color winner) {
        this.winner = winner;
    }

    public void setTie(boolean tie) {
        isTie = tie;
    }

    public void setStaleMate(boolean staleMate) {
        isStaleMate = staleMate;
    }

    public int getConsecutiveMovesWhite() {
        return consecutiveMovesWhite;
    }

    public int getConsecutiveMovesBlack() {
        return consecutiveMovesBlack;
    }

    public boolean isCurrMoveIsAttack() {
        return currMoveIsAttack;
    }

    public void setConsecutiveMovesWhite(int consecutiveMovesWhite) {
        this.consecutiveMovesWhite = consecutiveMovesWhite;
    }

    public void setConsecutiveMovesBlack(int consecutiveMovesBlack) {
        this.consecutiveMovesBlack = consecutiveMovesBlack;
    }

    public Figure getPrevCheckerUsedWhite() {
        return prevCheckerUsedWhite;
    }

    public Figure getPrevCheckerUsedBlack() {
        return prevCheckerUsedBlack;
    }

    public void setPrevCheckerUsedWhite(Figure prevCheckerUsedWhite) {
        this.prevCheckerUsedWhite = prevCheckerUsedWhite;
    }

    public void setPrevCheckerUsedBlack(Figure prevCheckerUsedBlack) {
        this.prevCheckerUsedBlack = prevCheckerUsedBlack;
    }

    public GameStage getStage() {
        return stage;
    }

    public void setStage(GameStage stage) {
        this.stage = stage;
    }


    public boolean isQueenPromoted() {
        return isQueenPromoted;
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public void setQueenPromoted(boolean queenPromoted) {
        isQueenPromoted = queenPromoted;
    }

    public void setTurnCounter(int turnCounter) {
        this.turnCounter = turnCounter;
    }

    public int getWhiteTurnPoints() {
        return whiteTurnPoints;
    }

    public int getBlackTurnPoints() {
        return blackTurnPoints;
    }


    public void setWhiteTurnPoints(int whiteTurnPoints) {
        this.whiteTurnPoints = whiteTurnPoints;
    }

    public void setBlackTurnPoints(int blackTurnPoints) {
        this.blackTurnPoints = blackTurnPoints;
    }


}