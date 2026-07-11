package game.logic;

import game.logic.moves.Moves;
import game.logic.moves.piecemoves.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class MatchManager {

    private Field field;
    private Color currentTurn;
    private boolean isUnderCheck;


    public MatchManager(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }


    // public boolean endGameCheck() {}

    public Color getWinner() {

    }


    private boolean checkWin() { // getWinner is called if checkWin = true
    }

    private boolean isTurnMovable() { // spisal

        return IntStream.range(0, field.getSize())
                .mapToObj(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)))
                .flatMap(stream -> stream) //// Flattens the stream of streams into a single Stream<Coordinates>
                .anyMatch(coords -> {
                    Figure figure = field.getFigure(coords);

                    if (figure != null && figure.getColor() == getCurrentTurn()) {
                        List<Coordinates> legalMoves = filterMoves(coords);
                        return !legalMoves.isEmpty();
                    }

                    return false;
                });
    }

    public boolean checkTie() {

    }


    private boolean isTie() {}// if there are certain pieces left on the board

    private boolean isStaleMate() {} // if staleMate


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

    //endGameCheck, getWinner, checkWin,
    // checkTie(staleMate or Tie(два короля или король слон против короля,
    // король конь против короля, аороль и два коня против короля)), promoteToQueenCheck
}
