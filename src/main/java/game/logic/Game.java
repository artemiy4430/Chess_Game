package game.logic;

import game.logic.players.Bot;
import game.logic.players.Contender;
import game.logic.players.Player;
import game.logic.players.moveconfig.Move;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private GameEventListener listener;
    private boolean isLocked = false;
    private List<Coordinates> currentAvailableMoves;
    private Coordinates lockedFigureCoordinates;
    private Coordinates newFigureCoordinates;
    private MatchManager matchManager;
    private Bot bot;
    private Player player1;
    private Player player2;
    private boolean isGameOver = false;
    private Coordinates prevLockedFigureCoordinates;
    private Coordinates prevTargetFigureCoordinates;
    private Figure pendingMoving;
    private Figure pendingCaptured;

    public Game(MatchManager matchManager, Color selectedTurn, int depth) {
        this.matchManager = matchManager;
        this.currentAvailableMoves = new ArrayList<>();
        this.bot = new Bot("BOT", matchManager.getOppositeColor(selectedTurn), depth);
        this.player1 = new Player("PLAYER", selectedTurn);
        this.player2 = null;
    }

    public Game(MatchManager matchManager) {
        this.bot = null;
        this.matchManager = matchManager;
        this.currentAvailableMoves = new ArrayList<>();
        this.player1 = new Player("PLAYER1", Color.WHITE);
        this.player2 = new Player("PLAYER2", Color.WHITE);
    }

    public Contender getCurrentContender() {
        Color currentTurn = this.matchManager.getCurrentTurn();

        if (this.bot != null) {
            return (currentTurn == this.bot.getTurn()) ? this.bot : this.player1;
        } else return (currentTurn == this.player1.getTurn()) ? this.player1 : this.player2;
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

        if (bot != null && getCurrentTurn() == bot.getTurn()) return;
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
                if (isPromotion) {
                    System.out.println("PROMOTION");
                    this.matchManager.setPromotion(true);
                    this.pendingMoving = movingFigure;
                    this.pendingCaptured = capturedFigure;
                    return;
                }
                Move move = new Move(lockedFigureCoordinates, newFigureCoordinates,
                        movingFigure, capturedFigure, null, isEnPassant, isCastle);

                move(move, matchManager);
                endTurn();
            }
        }
    }

    public void unlock() {
        this.isLocked = false;
        this.lockedFigureCoordinates = null;
        this.newFigureCoordinates = null;
        this.pendingMoving = null;
        this.pendingCaptured = null;
        this.currentAvailableMoves.clear();
    }

    public void completePromotion(FigureType chosenType) {
        Move move = new Move(lockedFigureCoordinates,
                newFigureCoordinates, pendingMoving, pendingCaptured, chosenType, false, false);
        move(move, matchManager);
        matchManager.setPromotion(false);
        endTurn();
    }

    private void endTurn() {
        Color currentTurn = matchManager.getCurrentTurn();
        Color nextTurnColor = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;

        matchManager.clearTargetCells();
        matchManager.processTargetCells(getCurrentTurn());
        matchManager.isKingAttacked(getCurrentTurn());
        matchManager.updateStage();
        matchManager.clearLastMovedFigure(nextTurnColor);

        if (matchManager.endGameCheck()) {
            listener.onBoardChanged(getField());
            isGameOver = true;
            return;
        } else if (countConsecutiveMoves()) {
            matchManager.setTie(true);
            listener.onBoardChanged(getField());
            isGameOver = true;
            return;
        }
        this.prevLockedFigureCoordinates = this.lockedFigureCoordinates;
        this.prevTargetFigureCoordinates = this.newFigureCoordinates;
        unlock();
        matchManager.setCurrentTurn(nextTurnColor);
        listener.onBoardChanged(getField());

        if (bot != null && bot.getTurn() == nextTurnColor) {

            // BREAK RECURSIVE STACK OVERFLOW:
            // SwingUtilities.invokeLater places the bot task onto the event queue,
            // allowing endTurn() and move() to return completely FIRST!
            javax.swing.SwingUtilities.invokeLater(this::handleBotTurn); /// this line of code is FULLY copied from gemini :)
        }
    }

    public void handleBotTurn() {
        MatchManager manager = new MatchManager(matchManager);
        Move bestMove = bot.calculateBestMove(manager, bot.getDepth(), bot.getTurn());

        if (bestMove != null) {
            this.lockedFigureCoordinates = bestMove.from();
            this.newFigureCoordinates = bestMove.to();
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
        if (matchManager.isCurrMoveIsAttack()) {
            matchManager.consecutiveMovesReset(getCurrentTurn());
            return false;
        }

        Figure prevFigure = (getCurrentTurn() == Color.WHITE)
                ? matchManager.getPrevCheckerUsedWhite()
                : matchManager.getPrevCheckerUsedBlack();
        Figure currentFigure = getField().getFigure(this.newFigureCoordinates);

        if (currentFigure == null) return false;
        boolean flag = false;

        if (prevFigure != null && currentFigure.getType() != FigureType.PAWN && prevFigure.equals(currentFigure)) {
            flag = matchManager.consecutiveMovesUpdate();
        } else {
            matchManager.consecutiveMovesReset(getCurrentTurn());
        }
        if (getCurrentTurn() == Color.WHITE) {
            matchManager.setPrevCheckerUsedWhite(currentFigure);
        } else {
            matchManager.setPrevCheckerUsedBlack(currentFigure);
        }

        return flag;
    }

    public Bot getBot() {
        return bot;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
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

//    public boolean isBotGame() {
//        return bot1 != null && bot2 != null;
//    }

    public Coordinates getPrevTargetFigureCoordinates() {
        return prevTargetFigureCoordinates;
    }

    public Coordinates getPrevLockedFigureCoordinates() {
        return prevLockedFigureCoordinates;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }
}
