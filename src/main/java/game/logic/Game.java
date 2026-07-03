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

    public Game(Field field) {
        this.field = field;
        currentAvailableMoves = new ArrayList<>();
    }

    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }

    public void processSpace(Cursor cursor) {
    }


    public void move(Coordinates startingCoordinates, Coordinates targetCoordinates) {
    }

    private void capture(Coordinates startingCoordinates, Coordinates targetCoordinates) {
    }

    private void defaultMove(Coordinates startingCoordinates, Coordinates targetCoordinates) {
    }

    private boolean isValidMove(Coordinates startCoords, Coordinates endCoords) {
    }

    private List<Coordinates> getAttackOpportunity(Coordinates currentPosition) {
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
// checkAttackOpportunity(), move, capture, defaultMove, isValidMove, captureCheck,
