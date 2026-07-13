package game.logic;

import game.logic.moves.Moves;
import game.logic.moves.piecemoves.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class MatchManager {

    private Field field;
    private Color currentTurn;
    private boolean isUnderCheck;
    private Color winner;
    private boolean isTie;
    private boolean isStaleMate;


    public MatchManager(Field field) {
        this.field = field;
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

    private boolean checkWin() { // getWinner is called if checkWin = true
        if (isUnderCheck && !isTurnMovable()) {
            setWinner(currentTurn);
            return true;
        }
        return false;
    }

    private boolean isTurnMovable() {

        return IntStream.range(0, field.getSize())
                .mapToObj(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)))
                .flatMap(stream -> stream) //// Flattens the stream of streams into a single Stream<Coordinates>
                .anyMatch(coords -> {
                    Figure figure = field.getFigure(coords);

                    if (figure != null && figure.getColor() == this.currentTurn) {
                        List<Coordinates> legalMoves = filterMoves(coords);
                        return !legalMoves.isEmpty();
                    }

                    return false;
                });
    }

    private boolean checkTie() {
        if (!isUnderCheck) {
            if (isTieCheck()) {
                setTie(true);
            } else if (isStaleMate) {
                setStaleMate(true);
            }
        }
        return false;
    }


    private boolean isTieCheck() { // if there are certain pieces left on the board два короля+, король слон против короля+,
        // король конь против короля+, король и два коня против короля+, два разнопольных слона+
        return isTieByOneBishop() || isTieByTwoBishopsSameCellColor() || isTieByTwoKnights() || isTieByTwoKings();

    }

    private boolean isTieByTwoKings() {
        return IntStream.range(0, field.getSize())

                .mapToObj(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)).map(coordinates -> {
                            Figure figure = field.getFigure(coordinates);
                            if (figure != null) return figure;
                            return null;
                        })).flatMap(stream -> stream)
                .anyMatch(x -> x != null && x.getType() != FigureType.KING);
    }


    private boolean isTieByTwoBishopsSameCellColor() { // два однопольных слона
        AtomicBoolean isWhiteAdded = new AtomicBoolean(false);
        AtomicBoolean isBlackAdded = new AtomicBoolean(false);

        return IntStream.range(0, field.getSize())
                .mapToObj(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)).map(coordinates -> {
                            Cell cell = new Cell();
                            Figure figure = cell.getFigure();

                            if (figure != null && figure.getType() == FigureType.BISHOP) {
                                if (figure.getColor() == Color.WHITE && !isWhiteAdded.get()) {
                                    isWhiteAdded.set(true);
                                    return cell;
                                } else if (figure.getColor() == Color.BLACK && !isBlackAdded.get()) {
                                    isBlackAdded.set(true);
                                    return cell;
                                }
                            }
                            return null;
                        })

                ).flatMap(stream -> stream)
                .filter(x -> x != null && x.getColor() == Color.WHITE)
                .count() == 1;
    }

    private boolean isTieByOneBishop() {
        return IntStream.range(0, field.getSize())
                .mapToObj(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)).map(coordinates -> {
                            Figure figure = field.getFigure(coordinates);

                            if (figure != null && (figure.getType() == FigureType.KING || figure.getType() == FigureType.BISHOP)) {
                                return figure;
                            }
                            return null;
                        })).flatMap(stream -> stream)
                .filter(x -> x != null && x.getType() == FigureType.BISHOP).count() == 1;

    }

    private boolean isTieByTwoKnights() {
        return IntStream.range(0, field.getSize())
                .mapToObj(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)).map(coordinates -> {
                            Figure figure = field.getFigure(coordinates);

                            if (figure != null && (figure.getType() == FigureType.KING || figure.getType() == FigureType.KNIGHT)) {
                                return figure;
                            }
                            return null;
                        })).flatMap(stream -> stream)
                .filter(x -> x != null && x.getType() == FigureType.KNIGHT).count() > 2;
    }

    private boolean isStaleMateCheck() {
        return IntStream.range(0, field.getSize())
                .mapToObj(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i))
                        .map(coordinates -> {
                            Figure figure = field.getFigure(coordinates);
                            if (figure != null) {
                                return filterMoves(coordinates).size();
                            }
                            return 0;
                        })).flatMap(stream -> stream).noneMatch(x -> x > 0);
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

                if (currentFigure != null && currentFigure.getColor() != currentTurn) {
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
        for (int i = 0; i < field.getSize(); i++) {
            for (int j = 0; j < field.getSize(); j++) {
                Coordinates targetCoordinates = new Coordinates(j, i);
                Figure figure = field.getFigure(targetCoordinates);

                if (figure != null && figure.getColor() == turn && figure.getType() == FigureType.KING)
                    return targetCoordinates;
            }
        }
        return null;
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
        // вызывать всегда когда король под шахом если фигура король то всегда фильтровать
        Figure figure = field.getFigure(coordinates);
        List<Coordinates> figureAvailableMoves = getMovementType(figure).getAvailableMoves(coordinates);
        List<Coordinates> filteredMoves = new ArrayList<>();
        Color currentColor = figure.getColor();
        boolean isKing = figure.getType().equals(FigureType.KING);

        for (int i = 0; i < figureAvailableMoves.size(); i++) {
            Coordinates target = figureAvailableMoves.get(i);
            Figure removedFigure = field.getFigure(target);
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

                if (currentFigure != null && currentFigure.getColor() == currentColor && currentFigure.getType() == FigureType.ROOK) {

                    if (isValidCastle(rookCoords)) {

                        if (rookX == 7) {
                            filteredMoves.add(new Coordinates(coordinates.getCoordinateX() + 2, kingY));
                        } else {
                            filteredMoves.add(new Coordinates(coordinates.getCoordinateX() - 2, kingY));
                        }
                    }
                }
            }
        }
        return filteredMoves;
    }

    private boolean isKingUnderCheck(Color opponentColor) {
        Coordinates kingCoordinates = getKing((opponentColor == Color.WHITE) ? Color.BLACK : Color.WHITE);

        for (int i = 0; i < field.getSize(); i++) {
            for (int j = 0; j < field.getSize(); j++) {
                Coordinates cellCoordinates = new Coordinates(j, i);
                Figure figure = field.getFigure(cellCoordinates);

                if (figure != null && figure.getColor().equals(opponentColor)) {
                    List<Coordinates> figureAvailableMoves = getMovementType(figure).getAvailableMoves(cellCoordinates);

                    if (figureAvailableMoves != null && !figureAvailableMoves.isEmpty()) {
                        if (figure.getType() == FigureType.PAWN) {
                            int index = figureAvailableMoves.indexOf(kingCoordinates);

                            if (index > -1 && figureAvailableMoves.get(index).isAttackCoordinate()) {
                                return true;
                            }

                        } else if (figureAvailableMoves.contains(kingCoordinates)) return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean isValidCastle(Coordinates selectedRook) { // если король не дыигался + ладья которая выбрана для ракировки так же не сдвинута +
        // между ними нету фигур
        if (isUnderCheck) return false;
        Coordinates kingCoordinates = getKing(currentTurn);
        int distance = getDistanceWithKing(selectedRook);
        boolean isShortCastle = distance < 4;

        if (!field.getFigure(kingCoordinates).isMoved() && !field.getFigure(selectedRook).isMoved()) {
            for (int i = 1; i < distance; i++) {
                int currentCoordinateX = (isShortCastle) ? kingCoordinates.getCoordinateX() + i : kingCoordinates.getCoordinateX() - i;
                Coordinates checkCoords = new Coordinates(currentCoordinateX, kingCoordinates.getCoordinateY());

                if (field.getFigure(checkCoords) != null) return false;

                if (isShortCastle || i != 3) {
                    if (field.getCell(checkCoords).isAttacked()) return false;
                }
            }
            return true;
        }
        return false;
    }

    public int getDistanceWithKing(Coordinates selectedFigure) {
        Coordinates kingCoordinates = getKing(currentTurn);

        if (selectedFigure.getCoordinateY() != kingCoordinates.getCoordinateY()) return -1;

        return Math.abs(kingCoordinates.getCoordinateX() - selectedFigure.getCoordinateX());
    }


    public Coordinates getCastlingRook(Coordinates coordinates) {
        Coordinates kingCoordinates = getKing(currentTurn);

        if (coordinates.getCoordinateY() != kingCoordinates.getCoordinateY()) return null;
        boolean isShortCastle = getDistanceWithKing(coordinates) > 0;

        return (isShortCastle) ? new Coordinates(7, kingCoordinates.getCoordinateY())
                : new Coordinates(0, kingCoordinates.getCoordinateY());
    }

    public Color getCurrentTurn() {
        return currentTurn;
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

    //endGameCheck, getWinner, checkWin,
    // checkTie(staleMate or Tie(два короля или король слон против короля,
    // король конь против короля, аороль и два коня против короля)), promoteToQueenCheck
}