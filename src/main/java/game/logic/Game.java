package game.logic;

import game.logic.moves.Moves;
import game.logic.moves.piecemoves.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private Field field;
    private GameEventListener listener;
    private Color currentTurn;
    private boolean isLocked = false;
    private List<Coordinates> currentAvailableMoves;
    private Coordinates lockedFigureCoordinates;
    private boolean isQueenPromoted;
    private MatchManager matchManager;
    private boolean isUnderCheck; //

    public Game(Field field) {
        this.field = field;
        currentAvailableMoves = new ArrayList<>();
    }

    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }

    public void processSpace(Cursor cursor) {
        Coordinates currentCursorCoordinates = new Coordinates(cursor.getCursorCoordinateX(), cursor.getCursorCoordinateY());
        Figure currentFigure = field.getFigure(currentCursorCoordinates);

        if (!isLocked) {
            if (currentFigure != null && currentFigure.getColor() == currentTurn && lockedFigureCoordinates == null) {
                List<Coordinates> figureAvailableMoves = filterMoves(currentCursorCoordinates);

                if (!figureAvailableMoves.isEmpty()) {
                    isLocked = true;
                    this.lockedFigureCoordinates = currentCursorCoordinates;
                    currentAvailableMoves.addAll(figureAvailableMoves);
                    listener.onBoardChanged(field);
                }

            }
        } else {
            if (lockedFigureCoordinates.equals(currentCursorCoordinates)) {
                unlock();
                return;
            }
            Figure movingFigure = field.getFigure(lockedFigureCoordinates);

            if (movingFigure != null && currentAvailableMoves.contains(currentCursorCoordinates)) {
                if (movingFigure.getType() == FigureType.KING
                        && lockedFigureCoordinates.getCoordinateY() == currentCursorCoordinates.getCoordinateY()
                        && Math.abs(lockedFigureCoordinates.getCoordinateX() - currentCursorCoordinates.getCoordinateX()) > 2) {
                    castle(currentCursorCoordinates);
                } else {
                    move(lockedFigureCoordinates, currentCursorCoordinates);
                }
                unlock();
            }
        }
    }

    public void unlock() {

    }

    private void endTurn() {
        clearTargetCells();
        processTargetCells(currentTurn);
        Color nextTurnColor = (this.currentTurn == Color.WHITE) ? Color.BLACK
                : Color.WHITE;
        isKingAttacked((this.currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE);
        // matchManaget.gameOverCheck()
        this.currentTurn = nextTurnColor;
    }


    public void move(Coordinates startCoordinates, Coordinates targetCoordinates) {
        Figure currentFigure = field.getFigure(startCoordinates);

        if (!isValidMove(startCoordinates, targetCoordinates) && currentFigure == null) return;

        if (field.getFigure(targetCoordinates) == null) {
            defaultMove(startCoordinates, targetCoordinates);
        } else {
            capture(startCoordinates, targetCoordinates);
        }

        listener.onBoardChanged(field);
        endTurn();
    }

    private void capture(Coordinates startCoordinates, Coordinates targetCoordinates) {
        Figure targetFigure = field.getFigure(targetCoordinates);

        if (targetFigure != null) {
            field.removeFigure(targetCoordinates);
            defaultMove(startCoordinates, targetCoordinates);
        }
    }

    private void defaultMove(Coordinates startCoordinates, Coordinates targetCoordinates) {
        Figure currentFigure = field.getFigure(startCoordinates);

        if (!currentFigure.isMoved()) {
            currentFigure.setMoved(true);
        }

        field.setFigure(startCoordinates, targetCoordinates);
        //matchManager.promoteToQueenCheck();
        // zamedlit potom
    }

    private boolean isValidMove(Coordinates startCoords, Coordinates endCoords) {
        Figure figure = field.getFigure(startCoords);

        if (getMovementType(figure).getAvailableMoves(startCoords).contains(endCoords)) return true;

        return false;
    }

//    private List<Coordinates> getAttackOpportunity(Coordinates currentPosition) {} ?

    private void processTargetCells(Color currentTurn) { // dlya korolya chtobi smotret chto pod boyem,
        // potom v konce hoda ono budet ochischatsa

        ////TODO: написать метод getKing(Color turn) + , boolean isKingAttacked(), вызывать последний в конуе енд терн и в методе processSpace не давать залочится ни на одной фигуре кроме короля(или тем которая перекрыает) если true

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

    private void clearTargetCells() { // pered peredachey choda vizvat'
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

    private Coordinates getKing(Color turn) {
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

    private void isKingAttacked(Color turn) { // вызывается после совершенного хода проверяется противоположный цвет короля
        setUnderCheck(field.getCell(getKing((turn == Color.WHITE ? Color.BLACK : Color.WHITE))).isAttacked());
    }

    private void setUnderCheck(boolean underCheck) {
        isUnderCheck = underCheck;
    }

    public boolean isUnderCheck() {
        return isUnderCheck;
    }

    private List<Coordinates> filterMoves(Coordinates coordinates) { // вызывать всегда когда король под шахом если фигура король то всегда фильтровать
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


    private boolean isValidCastle(Coordinates selectedRook) { // если король не дыигался + ладья которая выбрана для ракировки так же не сдвинута +
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

    private int getDistanceWithKing(Coordinates selectedFigure) {
        Coordinates kingCoordinates = getKing(currentTurn);

        if (selectedFigure.getCoordinateY() != kingCoordinates.getCoordinateY()) return -1;

        return Math.abs(kingCoordinates.getCoordinateX() - selectedFigure.getCoordinateX());
    }

    /// /TODO: castle(Coordinates selectedRook) ...

    private void castle(Coordinates kingLandingCoordinates) {
        Coordinates selectedRook = getCastlingRook(kingLandingCoordinates);

        if (!isValidCastle(selectedRook)) return;
        Coordinates kingCoordinates = getKing(currentTurn);
        int distance = getDistanceWithKing(selectedRook);
        boolean isShortCastle = distance < 4;

        if (isShortCastle) {
            field.setFigure(kingCoordinates, new Coordinates(kingCoordinates.getCoordinateX() + 2, kingCoordinates.getCoordinateY()));
            field.setFigure(selectedRook, new Coordinates(selectedRook.getCoordinateX() - 2, selectedRook.getCoordinateY()));
        } else {
            field.setFigure(kingCoordinates, new Coordinates(kingCoordinates.getCoordinateX() - 2, kingCoordinates.getCoordinateY()));
            field.setFigure(selectedRook, new Coordinates(selectedRook.getCoordinateX() + 2, selectedRook.getCoordinateY()));
        }
    }


    private Coordinates getCastlingRook(Coordinates coordinates) {
        Coordinates kingCoordinates = getKing(currentTurn);

        if (coordinates.getCoordinateY() != kingCoordinates.getCoordinateY()) return null;
        boolean isShortCastle = getDistanceWithKing(coordinates) > 0;

        return (isShortCastle) ? new Coordinates(7, kingCoordinates.getCoordinateY())
                : new Coordinates(0, kingCoordinates.getCoordinateY());
    }

}


// if the method is called under certain figure where the isMoved = false, it mustr be set true;
//setGameEventListener, processSpace, unlock,
// actionCheck(), endTurn(), moveAbilityCheck,
// checkAttackOpportunity()?, move, capture, defaultMove, isValidMove, captureCheck,
