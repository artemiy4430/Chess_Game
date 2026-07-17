package game.logic;

import game.logic.moves.Moves;
import game.logic.moves.piecemoves.*;

import java.util.ArrayList;
import java.util.List;

public class Game {
    /// todo: везде посоздовать глобальные переменные где есть getField
    ///
    private GameEventListener listener;
    private boolean isLocked = false;
    private List<Coordinates> currentAvailableMoves;
    private Coordinates lockedFigureCoordinates;
    private boolean isQueenPromoted;
    private MatchManager matchManager;

    public Game(MatchManager matchManager) {
        this.matchManager = matchManager;
        this.currentAvailableMoves = new ArrayList<>();
    }

    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }

    public void processSpace(Cursor cursor) {
        Coordinates currentCursorCoordinates = new Coordinates(cursor.getCursorCoordinateX(), cursor.getCursorCoordinateY());
        Figure currentFigure = getField().getFigure(currentCursorCoordinates);

        if (!isLocked) {
            if (currentFigure != null && currentFigure.getColor() == getCurrentTurn() && lockedFigureCoordinates == null) {
                List<Coordinates> figureAvailableMoves = matchManager.filterMoves(currentCursorCoordinates);

                if (!figureAvailableMoves.isEmpty()) {
                    isLocked = true;
                    this.lockedFigureCoordinates = currentCursorCoordinates;
                    currentAvailableMoves.addAll(figureAvailableMoves);
                    listener.onBoardChanged(getField());
                }

            }
        } else {
            if (lockedFigureCoordinates.equals(currentCursorCoordinates)) {
                unlock();
                return;
            }

            Figure movingFigure = getField().getFigure(lockedFigureCoordinates);
            if (movingFigure != null) {
                if (!currentAvailableMoves.contains(currentCursorCoordinates)) return;
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
        isLocked = false;
        this.lockedFigureCoordinates = null;
        this.currentAvailableMoves.clear();
        listener.onBoardChanged(getField());
    }

    private void endTurn() {
        matchManager.clearTargetCells();
        matchManager.processTargetCells(getCurrentTurn());
        Color nextTurnColor = (this.getCurrentTurn() == Color.WHITE) ? Color.BLACK
                : Color.WHITE;
        matchManager.isKingAttacked((getCurrentTurn() == Color.WHITE) ? Color.BLACK : Color.WHITE);
        // matchManaget.gameOverCheck()
        matchManager.setCurrentTurn(nextTurnColor);
    }


    public void move(Coordinates startCoordinates, Coordinates targetCoordinates) {
        Figure currentFigure = getField().getFigure(startCoordinates);

        if (!matchManager.isValidMove(startCoordinates, targetCoordinates) && currentFigure == null) return;

        if (getField().getFigure(targetCoordinates) == null) {
            defaultMove(startCoordinates, targetCoordinates);
        } else {
            capture(startCoordinates, targetCoordinates);
        }

        listener.onBoardChanged(getField());
        endTurn();
    }

    private void capture(Coordinates startCoordinates, Coordinates targetCoordinates) {
        Figure targetFigure = getField().getFigure(targetCoordinates);

        if (targetFigure != null) {
            getField().removeFigure(targetCoordinates);
            defaultMove(startCoordinates, targetCoordinates);
        }
    }

    private void defaultMove(Coordinates startCoordinates, Coordinates targetCoordinates) {
        Figure currentFigure = getField().getFigure(startCoordinates);

        if (!currentFigure.isMoved()) {
            currentFigure.setMoved(true);
        }

        getField().setFigure(startCoordinates, targetCoordinates);
        matchManager.promoteToQueenCheck(targetCoordinates);
        // zamedlit potom
    }

    private void castle(Coordinates kingLandingCoordinates) {
        Coordinates selectedRook = matchManager.getCastlingRook(kingLandingCoordinates);

        if (!matchManager.isValidCastle(selectedRook)) return;
        Coordinates kingCoordinates = matchManager.getKing(getCurrentTurn());
        int distance = matchManager.getDistanceWithKing(selectedRook);
        boolean isShortCastle = distance < 4;

        if (isShortCastle) {
            getField().setFigure(kingCoordinates, new Coordinates(kingCoordinates.getCoordinateX() + 2, kingCoordinates.getCoordinateY()));
            getField().setFigure(selectedRook, new Coordinates(selectedRook.getCoordinateX() - 2, selectedRook.getCoordinateY()));
        } else {
            getField().setFigure(kingCoordinates, new Coordinates(kingCoordinates.getCoordinateX() - 2, kingCoordinates.getCoordinateY()));
            getField().setFigure(selectedRook, new Coordinates(selectedRook.getCoordinateX() + 3, selectedRook.getCoordinateY()));
        }
    }

    public MatchManager getMatchManager() {
        return matchManager;
    }

    public Field getField() {
        return this.matchManager.getField();
    }

    public void setMatchManager(MatchManager matchManager) {
        this.matchManager = matchManager;
    }

    public Color getCurrentTurn() {
        return matchManager.getCurrentTurn();
    }

    public Coordinates getLockedFigureCoordinates() {
        return lockedFigureCoordinates;
    }

    public void setLockedFigureCoordinates(Coordinates lockedFigureCoordinates) {
        this.lockedFigureCoordinates = lockedFigureCoordinates;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public List<Coordinates> getCurrentAvailableMoves() {
        return currentAvailableMoves;
    }

    public void setCurrentAvailableMoves(List<Coordinates> currentAvailableMoves) {
        this.currentAvailableMoves = currentAvailableMoves;
    }
}

//TODO: fix castling, endGameConditios, en-passant(vzyatie na prohode)