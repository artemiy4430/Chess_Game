package game.logic;

import game.logic.moves.Moves;
import game.logic.moves.piecemoves.*;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private Field field;
    private GameEventListener listener;
    private Color currentTurn; // ?
    private boolean isLocked = false;
    private List<Coordinates> currentAvailableMoves;
    private Coordinates lockedCheckerCoordinates;
    private boolean isQueenPromoted;
    private MatchManager matchManager;

    public Game(Field field) {
        this.field = field;
        currentAvailableMoves = new ArrayList<>();
    }

    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }

    public void processSpace(Cursor cursor) {
    }

    private void endTurn() {
        clearTargetCells();
        // matchManaget.gameOverCheck()
        this.currentTurn = (this.currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        processTargetCells(currentTurn);
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
        // potom v conce hoda ono budet ochischatsa

        for (int i = 0; i < field.getSize(); i++) {
            for (int j = 0; j < field.getSize(); j++) {
                Figure currentFigure = field.getFigure(new Coordinates(j, i));

                if (currentFigure != null && currentFigure.getColor() != currentTurn) {
                    Moves moves = getMovementType(currentFigure);
                    List<Coordinates> availableMoves = moves.getAvailableMoves(new Coordinates(j, i));

                    if (availableMoves != null && !availableMoves.isEmpty() &&
                            currentFigure.getType() != FigureType.PAWN) {
                        for (int k = 0; k < availableMoves.size(); k++) {
                            Cell cell = field.getCell(availableMoves.get(k));

                            cell.setAttacked(true);
                        }

                    } else if (availableMoves != null && !availableMoves.isEmpty() &&
                            currentFigure.getType() == FigureType.PAWN) {
                        for (int k = 0; k < availableMoves.size(); k++) {
                            Coordinates coordinates = availableMoves.get(k);

                            if (coordinates.isAttackCoordinate()) {
                                field.getCell(coordinates).setAttacked(true);
                            }

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



}
// if the method is called under certain figure where the isMoved = false, it mustr be set true;
//setGameEventListener, processSpace, unlock,
// actionCheck(), endTurn(), moveAbilityCheck,
// checkAttackOpportunity()?, move, capture, defaultMove, isValidMove, captureCheck,
