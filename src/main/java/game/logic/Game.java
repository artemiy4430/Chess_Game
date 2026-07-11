package game.logic;

import game.logic.moves.Moves;
import game.logic.moves.piecemoves.*;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private GameEventListener listener;
    private boolean isLocked = false;
    private List<Coordinates> currentAvailableMoves;
    private Coordinates lockedFigureCoordinates;
    private boolean isQueenPromoted;
    private MatchManager matchManager;
    private Field field = getField();

    public Game(MatchManager matchManager) {
        this.matchManager = matchManager;
        currentAvailableMoves = new ArrayList<>();
    }

    public void setListener(GameEventListener listener) {
        this.listener = listener;
    } // -

    public void processSpace(Cursor cursor) { // -
        Coordinates currentCursorCoordinates = new Coordinates(cursor.getCursorCoordinateX(), cursor.getCursorCoordinateY());
        Figure currentFigure = field.getFigure(currentCursorCoordinates);

        if (!isLocked) {
            if (currentFigure != null && currentFigure.getColor() == getCurrentTurn() && lockedFigureCoordinates == null) {
                List<Coordinates> figureAvailableMoves = matchManager.filterMoves(currentCursorCoordinates);

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

    public void unlock() { // -
        isLocked = false;
        this.lockedFigureCoordinates = null;
        listener.onBoardChanged(field);
    }

    private void endTurn() { // -
        matchManager.clearTargetCells();
        matchManager.processTargetCells(getCurrentTurn());
        Color nextTurnColor = (this.getCurrentTurn() == Color.WHITE) ? Color.BLACK
                : Color.WHITE;
       matchManager.isKingAttacked((getCurrentTurn() == Color.WHITE) ? Color.BLACK : Color.WHITE);
        // matchManaget.gameOverCheck()
        matchManager.setCurrentTurn(nextTurnColor);
    }


    public void move(Coordinates startCoordinates, Coordinates targetCoordinates) { // -
        Figure currentFigure = field.getFigure(startCoordinates);

        if (!matchManager.isValidMove(startCoordinates, targetCoordinates) && currentFigure == null) return;

        if (field.getFigure(targetCoordinates) == null) {
            defaultMove(startCoordinates, targetCoordinates);
        } else {
            capture(startCoordinates, targetCoordinates);
        }

        listener.onBoardChanged(field);
        endTurn();
    }

    private void capture(Coordinates startCoordinates, Coordinates targetCoordinates) { // -
        Figure targetFigure = field.getFigure(targetCoordinates);

        if (targetFigure != null) {
            field.removeFigure(targetCoordinates);
            defaultMove(startCoordinates, targetCoordinates);
        }
    }

    private void defaultMove(Coordinates startCoordinates, Coordinates targetCoordinates) { // -
        Figure currentFigure = field.getFigure(startCoordinates);

        if (!currentFigure.isMoved()) {
            currentFigure.setMoved(true);
        }

        field.setFigure(startCoordinates, targetCoordinates);
        //matchManager.promoteToQueenCheck();
        // zamedlit potom
    }

    private void castle(Coordinates kingLandingCoordinates) {
        Coordinates selectedRook = matchManager.getCastlingRook(kingLandingCoordinates);

        if (!matchManager.isValidCastle(selectedRook)) return;
        Coordinates kingCoordinates = matchManager.getKing(getCurrentTurn());
        int distance = matchManager.getDistanceWithKing(selectedRook);
        boolean isShortCastle = distance < 4;

        if (isShortCastle) {
            field.setFigure(kingCoordinates, new Coordinates(kingCoordinates.getCoordinateX() + 2, kingCoordinates.getCoordinateY()));
            field.setFigure(selectedRook, new Coordinates(selectedRook.getCoordinateX() - 2, selectedRook.getCoordinateY()));
        } else {
            field.setFigure(kingCoordinates, new Coordinates(kingCoordinates.getCoordinateX() - 2, kingCoordinates.getCoordinateY()));
            field.setFigure(selectedRook, new Coordinates(selectedRook.getCoordinateX() + 3, selectedRook.getCoordinateY()));
        }
    }

    private MatchManager getMatchManager() {
        return matchManager;
    }

    private Field getField() {
        return matchManager.getField();
    }

    public void setMatchManager(MatchManager matchManager) {
        this.matchManager = matchManager;
    }

    private Color getCurrentTurn() {
        return matchManager.getCurrentTurn();
    }

}


// if the method is called under certain figure where the isMoved = false, it mustr be set true;
//setGameEventListener, processSpace, unlock,
// actionCheck(), endTurn(), moveAbilityCheck,
// checkAttackOpportunity()?, move, capture, defaultMove, isValidMove, captureCheck,
