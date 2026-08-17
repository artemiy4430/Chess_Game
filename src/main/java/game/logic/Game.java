package game.logic;

import game.logic.moves.piecemoves.*;
import game.logic.players.Bot;
import game.logic.players.Contender;
import game.logic.players.moveconfig.Move;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private GameEventListener listener;
    private boolean isLocked = false;
    private List<Coordinates> currentAvailableMoves;
    private Coordinates lockedFigureCoordinates;
    private Coordinates newFigureCoordinates;
    private boolean isQueenPromoted;
    private MatchManager matchManager;
    private Bot bot1;
    private Bot bot2;
    private GameStage currentStage;
    private Coordinates prevlockedFigureCoordinates;
    private Coordinates prevtargetFigureCoordinates;

    public Game(MatchManager matchManager) {
        this.matchManager = matchManager;
        this.currentAvailableMoves = new ArrayList<>();
    }

    public Game(MatchManager matchManager, Bot bot) {
        this.matchManager = matchManager;
        this.currentAvailableMoves = new ArrayList<>();
        this.bot1 = bot;
    }

    public Game(MatchManager matchManager, Bot bot1, Bot bot2) {
        this.matchManager = matchManager;
        this.currentAvailableMoves = new ArrayList<>();
        this.bot1 = bot1;
        this.bot2 = bot2;
        currentStage = matchManager.getStage();
    }

    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }

    private void lock(List<Coordinates> availableMoves, Coordinates currentCursorCoordinates) {
        if (availableMoves == null || availableMoves.isEmpty() || currentCursorCoordinates == null) return;
        isLocked = true;
        this.lockedFigureCoordinates = currentCursorCoordinates;
        currentAvailableMoves.addAll(availableMoves);
    }


    public void processSpace(Cursor cursor) {
        Color currentTurn = getCurrentTurn();

        if (bot1 != null && getCurrentTurn() == bot1.getTurn()
                || bot2 != null && getCurrentTurn() == bot2.getTurn()) return;
        Field field = getField();
        Coordinates currentCursorCoordinates = new Coordinates(cursor.getCursorCoordinateX(),
                cursor.getCursorCoordinateY());
        Figure currentFigure = field.getFigure(currentCursorCoordinates);


        if (!this.isLocked) {
            if (currentFigure != null && currentFigure.getColor() == currentTurn) {
                List<Coordinates> figureAvailableMoves = matchManager.filterMoves(currentCursorCoordinates);
                lock(figureAvailableMoves, currentCursorCoordinates);
                listener.onBoardChanged(getField());
            }
        } else {
            if (this.lockedFigureCoordinates.equals(currentCursorCoordinates)) {
                unlock();
                listener.onBoardChanged(getField());
                return;
            }
            Figure movingFigure = field.getFigure(this.lockedFigureCoordinates);

            if (movingFigure != null) {
                List<Coordinates> availableMoves = this.currentAvailableMoves;
                if (!availableMoves.contains(currentCursorCoordinates)) return;
                this.newFigureCoordinates = currentCursorCoordinates;
                Figure capturedFigure = field.getFigure(currentCursorCoordinates);
                boolean isEnPassant = matchManager.isValidEnPassant(lockedFigureCoordinates, newFigureCoordinates,
                        currentTurn);
                boolean isPromotion = (!isEnPassant)
                        && matchManager.isQueenPromotable(currentCursorCoordinates, movingFigure);
                boolean isCastle = (!isPromotion) && (!isEnPassant) && movingFigure.getType() == FigureType.KING
                        && Math.abs(currentCursorCoordinates.getCoordinateX()
                        - lockedFigureCoordinates.getCoordinateX()) > 1;

                if (isEnPassant) {
                    capturedFigure = new Figure(matchManager.getOppositeColor(currentTurn), FigureType.PAWN);
                }
                Move move = new Move(lockedFigureCoordinates, newFigureCoordinates,
                        movingFigure, capturedFigure, (isPromotion) ? FigureType.QUEEN : null, isEnPassant, isCastle);

                //  boolean contains = false;
                //  for (Coordinates coordinates : availableMoves) {
                //      if (coordinates.equals(currentCursorCoordinates)) {
                //          contains = true;
                //          currentCursorCoordinates.setAttackCoordinate(coordinates.isAttackCoordinate());
                //      }
                //  } ????


                move(move, matchManager);
                endTurn();
                // unlock(); ????
            }
        }
    }


    public void unlock() {
        this.isLocked = false;
        this.lockedFigureCoordinates = null;
        this.newFigureCoordinates = null;
        this.currentAvailableMoves.clear();
    }

    private void endTurn() {
        Color currentTurn = matchManager.getCurrentTurn();
        Color nextTurnColor = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;

        matchManager.clearTargetCells();
        matchManager.processTargetCells(getCurrentTurn());
        matchManager.isKingAttacked(getCurrentTurn());
        matchManager.updateStage();
        matchManager.clearLastMovedFigure(nextTurnColor);

        if (currentStage != matchManager.getStage()) {
            currentStage = matchManager.getStage();
         //   System.out.println("STAGE WAS UPDATED " + currentStage);
        }
       // System.out.println(currentStage);

        if (matchManager.endGameCheck()) {
            System.out.println("GAME OVER!");
            listener.onBoardChanged(getField());
            return;
        } else if (countConsecutiveMoves()) {
            matchManager.setTie(true);
            System.out.println("GAME TIED!");
            listener.onBoardChanged(getField());
            return;
        }
        this.prevlockedFigureCoordinates = this.lockedFigureCoordinates;
        this.prevtargetFigureCoordinates = this.newFigureCoordinates;
        unlock();
        listener.onBoardChanged(getField());

        matchManager.setCurrentTurn(nextTurnColor);

        if (bot1 != null) {
            Bot currentBot = bot1;

            if (bot2 != null) {
                currentBot = (bot1.getTurn() == getCurrentTurn()) ? bot1 : bot2;
            } else if (bot1.getTurn() != getCurrentTurn()) {
                return;
            }

            final Bot botToExecute = currentBot;

            // BREAK RECURSIVE STACK OVERFLOW:
            // SwingUtilities.invokeLater places the bot task onto the event queue,
            // allowing endTurn() and move() to return completely FIRST!
            javax.swing.SwingUtilities.invokeLater(() -> handleBotTurn(botToExecute));
        }
    }

    public void handleBotTurn(Bot bot) {
        MatchManager manager = new MatchManager(matchManager);
        Move bestMove = bot.calculateBestMove(manager, 4, bot.getTurn());

        if (bestMove != null) {
            if (bot1 != null || bot2 != null) {
                this.lockedFigureCoordinates = bestMove.from();
                this.newFigureCoordinates = bestMove.to();
            }

            move(bestMove, matchManager);
            endTurn();
        }
    }


    public void move(Move move, MatchManager manager) {
        Color turn = getCurrentTurn();
        Field field = manager.getField();

        if (move.capturedFigure() == null) {
            if (move.isCastle()) {
                castle(move.to(), turn);
            } else {
                defaultMove(field, move.from(), move.to());
            }
            manager.setCurrMoveIsAttack(false);
        } else {
            manager.incrementPoints(field.getFigure(move.to()));
            capture(field, move);
            manager.setCurrMoveIsAttack(true);
        }

        if (!move.isCastle() && move.promotionType() != null) {
            manager.promoteFigure(move.to(), move.promotionType());
        }

        if (turn == Color.WHITE) manager.setPrevCheckerUsedWhite(field.getFigure(move.to()));
        else manager.setPrevCheckerUsedWhite(getField().getFigure(move.to()));

        manager.setTurnCounter(manager.getTurnCounter() + 1);
    }

    private void capture(Field field, Move move) {
        if (!move.isEnPassant()) {
            field.removeFigure(move.to());
            defaultMove(field, move.from(), move.to());
        } else {
            enPassant(field, move.from(), move.to());
        }
    }

    private void enPassant(Field field, Coordinates moveFrom, Coordinates moveTo) {
        Coordinates targetFigureCoordinates = new Coordinates(moveTo.getCoordinateX(),
                moveFrom.getCoordinateY());

        field.setFigure(moveFrom, moveTo);
        field.removeFigure(targetFigureCoordinates);
    }

    private void defaultMove(Field field, Coordinates moveFrom, Coordinates moveTo) {
        Figure currentFigure = field.getFigure(moveFrom);

        if (!currentFigure.isMoved()) {
            currentFigure.setMoved(true);
        }
        currentFigure.incrementMoveCount();
        field.setFigure(moveFrom, moveTo);
        field.setLastMoved(moveTo);
    }

    private void castle(Coordinates kingTo, Color turn) {
        Field field = getField();
        Coordinates kingCoordinates = matchManager.getKing(turn);
        Coordinates selectedRook = matchManager.getCastlingRook(kingTo, turn);
        if (kingCoordinates == null || selectedRook == null) return;
        int kingX = kingCoordinates.getCoordinateX();
        int kingY = kingCoordinates.getCoordinateY();
        boolean isShortCastle = kingTo.getCoordinateX() > kingX;

        Coordinates kingDestination = (isShortCastle)
                ? new Coordinates(kingX + 2, kingY)
                : new Coordinates(kingX - 2, kingY);

        Coordinates rookDestination = (isShortCastle)
                ? new Coordinates(kingX + 1, kingY)
                : new Coordinates(kingX - 1, kingY);

        Figure king = field.getFigure(kingCoordinates);
        Figure rook = field.getFigure(selectedRook);
        if (king == null || rook == null) return;

        king.setMoved(true);
        field.setLastMoved(kingCoordinates);
        king.incrementMoveCount();

        rook.setMoved(true);
        rook.incrementMoveCount();
        field.removeFigure(kingCoordinates);
        field.removeFigure(selectedRook);

        field.setFigure(king, kingDestination);
        field.setFigure(rook, rookDestination);

        field.setLastMoved(kingDestination);
    }

    public boolean countConsecutiveMoves() {
        boolean flag = false;

        if (matchManager.isCurrMoveIsAttack()) {
            matchManager.consecutiveMovesReset(getCurrentTurn());
            return false;
        }
        Figure prevFigure = (getCurrentTurn() == Color.WHITE) ? matchManager.getPrevCheckerUsedWhite() : matchManager.getPrevCheckerUsedBlack();
        Figure currentFigure = getField().getFigure(newFigureCoordinates);

        if (prevFigure != null && currentFigure.getType() != FigureType.PAWN && prevFigure.equals(currentFigure)) {
            flag = matchManager.consecutiveMovesUpdate();
        } else {
            matchManager.consecutiveMovesReset(getCurrentTurn());
        }

        return flag;
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

    public boolean isBotGame() {
        return bot1 != null && bot2 != null;
    }

    public Coordinates getPrevtargetFigureCoordinates() {
        return prevtargetFigureCoordinates;
    }

    public Coordinates getPrevlockedFigureCoordinates() {
        return prevlockedFigureCoordinates;
    }
}
