package game.logic.players;

import game.logic.*;
import game.logic.players.moveconfig.Move;

import java.util.*;
import java.util.stream.IntStream;

public class Bot extends Contender {
    private List<FigureType> promotionTypes = List.of(FigureType.QUEEN, FigureType.KNIGHT,
            FigureType.ROOK, FigureType.BISHOP);
    private boolean hasCastled;
    private int depth;

    public Bot(String name, Color turn, int depth) {
        super(name, turn);
        this.depth = depth;
        this.hasCastled = false;
    }

    public Move calculateBestMove(MatchManager manager, int depth, Color botColor) {
        Field field = manager.getField();
        boolean isMinimizing = (botColor == Color.BLACK);
        List<Move> legalMoves = getAllAvailableLegalMoves(manager, botColor);
        List<Move> castlingMoves = new ArrayList<>();
     //   Map<Move, Integer> moveScores = new HashMap<>();
        if (legalMoves.isEmpty()) {
            return null;
        }
        Move bestMove = null;
        int bestScore = isMinimizing ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move move : legalMoves) {
            if (move == null) continue;
            boolean isCastling = move.isCastle() && !hasCastled;

            if (isCastling) {
                castlingMoves = performCastleMove(move, manager, botColor);
                if (castlingMoves == null || castlingMoves.isEmpty()) {
                    continue;
                }
            } else {
                simulateMove(field, move);
                if (move.promotionType() != null) {
                    manager.promoteFigure(move.to(), move.promotionType());
                }
            }
            int score = minmax(manager, depth - 1, !isMinimizing, alpha, beta);

            if (isCastling) {
                Move kingMove = castlingMoves.get(0);
                Move rookMove = castlingMoves.get(1);

                undoCastle(field, kingMove, rookMove);
            } else {
                if (move.promotionType() != null) {
                    undoPromotion(move.movedFigure());
                }
                undoMove(field, move);
            }

        //    moveScores.put(move, score);

            if (!isMinimizing) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestScore);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                beta = Math.min(beta, bestScore);
            }
        }
       return bestMove;

//        Move selection;
//        int threshold = switch (manager.getStage()) {
//            case START -> 5;
//            case MIDGAME -> 3;
//            case ENDGAME -> 0;
//        };
//
//        List<Move> candidateMoves = new ArrayList<>();
//        List<Integer> scores = new ArrayList<>();
//
//        for (Map.Entry<Move, Integer> entry : moveScores.entrySet()) {
//            int scoreDelta = isMinimizing
//                    ? (entry.getValue() - bestScore)
//                    : (bestScore - entry.getValue());
//
//            if (scoreDelta <= threshold) {
//                candidateMoves.add(entry.getKey());
//                scores.add(entry.getValue());
//            }
//        }
//
//        if (candidateMoves.isEmpty() && bestMove != null) {
//            if (bestMove.isCastle()) setHasCastled(true);
//            return bestMove;
//        }
//
//        selection = candidateMoves.get(random.nextInt(candidateMoves.size()));
//        if (selection.isCastle()) setHasCastled(true);
//        return selection;

    }

    private int minmax(MatchManager manager, int depth, boolean isMinimizing, int alpha, int beta) {
        if (manager.getKing(Color.WHITE) == null) return -100000000;
        if (manager.getKing(Color.BLACK) == null) return 100000000;
        Field field = manager.getField();
        Color currentTurn = isMinimizing ? Color.BLACK : Color.WHITE;
        List<Move> moves = getAllAvailableLegalMoves(manager, currentTurn);

        if (moves.isEmpty()) {
            if (manager.isKingUnderCheck(currentTurn)) {
                return isMinimizing ? (1000000 + depth) : (-1000000 - depth);
            }
            return 0;
        }

        if (depth == 0) {
            return evaluatePosition(manager);
        }

        int bestScore = isMinimizing ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        for (Move move : moves) {
            int score;

            if (move.isCastle()) {
                List<Move> castlingMoves = performCastleMove(move, manager, currentTurn);

                if (castlingMoves == null || castlingMoves.isEmpty()) continue;
                Move kingMove = castlingMoves.get(0);
                Move rookMove = castlingMoves.get(1);

                try {
                    score = minmax(manager, depth - 1, !isMinimizing, alpha, beta);
                } finally {
                    undoCastle(field, kingMove, rookMove);
                }
            } else {
                simulateMove(field, move);
                if (move.promotionType() != null) {
                    manager.promoteFigure(move.to(), move.promotionType());
                }
                try {
                    score = minmax(manager, depth - 1, !isMinimizing, alpha, beta);
                } finally {
                    if (move.promotionType() != null) {
                        undoPromotion(move.movedFigure());
                    }
                    undoMove(field, move);
                }
            }

            if (isMinimizing) {
                bestScore = Math.min(score, bestScore);
                beta = Math.min(beta, bestScore);
            } else {
                bestScore = Math.max(score, bestScore);
                alpha = Math.max(alpha, bestScore);
            }
            if (beta <= alpha) {
                break;
            }
        }
        return bestScore;
    }

    private List<Move> performCastleMove(Move move, MatchManager manager,
                                         Color turn) {
        Field field = manager.getField();
        Coordinates kingCoordinate = move.from();
        Coordinates rookCoordinate = manager.getCastlingRook(move.to(), turn);

        if (rookCoordinate == null) return null;
        Figure kingFigure = field.getFigure(kingCoordinate);
        Figure rookFigure = field.getFigure(rookCoordinate);
        boolean isShortCastle = move.to().getCoordinateX() > kingCoordinate.getCoordinateX();
        List<Move> castleMoves = new ArrayList<>();
        Coordinates kingLandingCoordinate = move.to();
        Coordinates rookLandingCoordinate = (isShortCastle)
                ? new Coordinates(kingLandingCoordinate.getCoordinateX() - 1, kingLandingCoordinate.getCoordinateY())
                : new Coordinates(kingLandingCoordinate.getCoordinateX() + 1, kingLandingCoordinate.getCoordinateY());
        Move kingMove = new Move(kingCoordinate, kingLandingCoordinate, kingFigure, null, null,
                false, false);
        Move rookMove = new Move(rookCoordinate, rookLandingCoordinate, rookFigure, null, null,
                false, false);

        castleMoves.add(kingMove);
        castleMoves.add(rookMove);
        castle(field, kingCoordinate, rookCoordinate, rookLandingCoordinate, kingLandingCoordinate);
        setHasCastled(true);

        return castleMoves;
    }

    private List<Move> getAllAvailableLegalMoves(MatchManager manager, Color currentTurn) {
        Field field = manager.getField();
        int size = field.getSize();
        List<Move> moves = new ArrayList<>(40);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Coordinates figureCoordinate = new Coordinates(x, y);
                Figure figure = field.getFigure(figureCoordinate);

                if (figure == null || figure.getColor() != currentTurn) {
                    continue;
                }

                List<Coordinates> figureAvailableMoves = manager.filterMoves(figureCoordinate);
                if (figureAvailableMoves == null || figureAvailableMoves.isEmpty()) {
                    continue;
                }

                for (Coordinates availableMove : figureAvailableMoves) {
                    if (!field.isWithinBoard(availableMove)) continue;

                    boolean isPromotion = isQueenPromotableOnNextMove(figureCoordinate, availableMove, manager);
                    boolean isEnPassant = figure.getType() == FigureType.PAWN
                            && manager.isValidEnPassant(figureCoordinate, availableMove, currentTurn);
                    boolean isCastle = false;

                    if (figure.getType() == FigureType.KING && !isPromotion && !isEnPassant && !hasCastled) {
                        Coordinates rookCoord = manager.getCastlingRook(availableMove, currentTurn);
                        if (rookCoord != null && manager.isValidCastle(rookCoord, currentTurn)
                                && field.getFigure(availableMove) == null) {
                            isCastle = true;
                        }
                    }

                    Figure capturedFigure = null;
                    if (isEnPassant) {
                        capturedFigure = field.getFigure(new Coordinates(availableMove.getCoordinateX(), figureCoordinate.getCoordinateY()));
                    } else if (field.getFigure(availableMove) != null) {
                        capturedFigure = field.getFigure(availableMove);
                    }

                    if (isPromotion) {
                        for (FigureType type : promotionTypes) {
                            moves.add(new Move(
                                    figureCoordinate,
                                    availableMove,
                                    figure,
                                    capturedFigure,
                                    type,
                                    isEnPassant,
                                    false
                            ));
                        }
                    } else {
                        moves.add(new Move(
                                figureCoordinate,
                                availableMove,
                                figure,
                                capturedFigure,
                                null,
                                isEnPassant,
                                isCastle
                        ));
                    }
                }
            }
        }

        moves.sort((m1, m2) -> Integer.compare(getMoveScore(m2), getMoveScore(m1)));
        return moves;
    }

    private int getMoveScore(Move move) {
        int score = 0;

        if (move.capturedFigure() != null) {
            int victimValue = move.capturedFigure().getPrice();
            int attackerValue = move.movedFigure().getPrice();
            score += 10000 + (victimValue * 10 - attackerValue);
        }

        if (move.promotionType() != null) {
            score += 8000;
        }

        if (move.isCastle()) {
            score += 5000;
        }

        return score;
    }

    private void makeMove(Field field, Coordinates coordinateFrom, Coordinates coordinateTo) {
        field.setFigure(coordinateFrom, coordinateTo);
    }

    private void simulateMove(Field field, Move move) {
        if (move.isEnPassant()) {
            simulateEnPassant(move, field);
        } else {
            makeMove(field, move.from(), move.to());
        }
    }

    private void undoMove(Field field, Move move) {
        field.setFigure(move.to(), move.from());

        if (move.capturedFigure() != null) {
            if (move.isEnPassant()) {
                Coordinates capturedPawnSquare = new Coordinates(move.to().getCoordinateX(), move.from().getCoordinateY());
                field.setFigure(move.capturedFigure(), capturedPawnSquare);
            } else {
                field.setFigure(move.capturedFigure(), move.to());
            }
        }
    }

    private void undoPromotion(Figure figure) {
        figure.setType(FigureType.PAWN);
    }

    private void castle(Field field, Coordinates kingFrom, Coordinates rookFrom,
                        Coordinates rookTo, Coordinates kingTo) {
        field.setFigure(kingFrom, kingTo);
        field.setFigure(rookFrom, rookTo);
    }

    private void undoCastle(Field field, Move kingMove, Move rookMove) {
        field.removeFigure(kingMove.to());
        field.removeFigure(rookMove.to());

        field.setFigure(kingMove.movedFigure(), kingMove.from());
        field.setFigure(rookMove.movedFigure(), rookMove.from());
        setHasCastled(false);
    }

    private boolean isQueenPromotableOnNextMove(Coordinates coordinateFrom, Coordinates coordinateTo,
                                                MatchManager manager) {
        Field field = manager.getField();
        Figure figure = field.getFigure(coordinateFrom);

        if (figure == null || figure.getType() != FigureType.PAWN) return false;

        return ((figure.getColor() == Color.BLACK && coordinateTo.getCoordinateY() == 7) ||
                (figure.getColor() == Color.WHITE && coordinateTo.getCoordinateY() == 0));
    }

    private void simulateEnPassant(Move move, Field field) {
        Coordinates targetFigureCoordinates = new Coordinates(move.to().getCoordinateX(), move.from().getCoordinateY());
        Figure attackingPawn = field.getFigure(move.from());

        field.removeFigure(targetFigureCoordinates);
        field.removeFigure(move.from());
        field.setFigure(attackingPawn, move.to());
    }

    private int evaluatePosition(MatchManager manager) {
        Coordinates whiteKing = manager.getKing(Color.WHITE);
        Coordinates blackKing = manager.getKing(Color.BLACK);

        if (blackKing == null) return 1000000;
        if (whiteKing == null) return -1000000;
        int totalPoints = 0;

        totalPoints += getTotalFigurePoints(manager, Color.WHITE);
        totalPoints -= getTotalFigurePoints(manager, Color.BLACK);

        totalPoints += evaluateFiguresPosition(manager);

        totalPoints += isOpponentUnderCheck(manager, Color.WHITE);
        totalPoints -= isOpponentUnderCheck(manager, Color.BLACK);


        totalPoints += evaluatePositionalRules(manager, Color.WHITE);
        totalPoints -= evaluatePositionalRules(manager, Color.BLACK);

        totalPoints -= hangingPiecePenalty(manager, Color.WHITE);
        totalPoints += hangingPiecePenalty(manager, Color.BLACK);
        GameStage stage = manager.getStage();

        if (stage != GameStage.ENDGAME) {
            totalPoints += evaluateCastlingRules(manager, Color.WHITE);
            totalPoints -= evaluateCastlingRules(manager, Color.BLACK);

            totalPoints += evaluateKingSafety(manager, Color.WHITE);
            totalPoints -= evaluateKingSafety(manager, Color.BLACK);
        }

        return totalPoints;
    }

    private int isOpponentUnderCheck(MatchManager manager, Color turn) {
        return (manager.isKingUnderCheck(manager.getOppositeColor(turn))) ? 30 : 0;
    }


    private int getTotalFigurePoints(MatchManager manager, Color turn) {
        Field field = manager.getField();

        return IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> field.getFigure(new Coordinates(j, i))))
                .mapToInt(figure -> {
                    if (figure != null && figure.getColor() == turn) {
                        return figure.getPrice() * 100;
                    }
                    return 0;
                }).sum();
    }

    private int getPSTFigure(Figure figure, int row, int col, GameStage stage) {
        int tableRow = getTableRow(figure.getColor(), row);

        return switch (figure.getType()) {
            case PAWN -> PieceSquareTables.PAWN_PST[stage.getIndex()][tableRow][col];
            case ROOK -> PieceSquareTables.ROOK_PST[stage.getIndex()][tableRow][col];
            case KNIGHT -> PieceSquareTables.KNIGHT_PST[stage.getIndex()][tableRow][col];
            case BISHOP -> PieceSquareTables.BISHOP_PST[stage.getIndex()][tableRow][col];
            case KING -> PieceSquareTables.KING_PST[stage.getIndex()][tableRow][col];
            case QUEEN -> PieceSquareTables.QUEEN_PST[stage.getIndex()][tableRow][col];
        };
    }

    private int evaluateFiguresPosition(MatchManager manager) {
        Field field = manager.getField();
        GameStage stage = manager.getStage();

        return IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)))
                .mapToInt(coordinates -> {
                    Figure figure = field.getFigure(coordinates);

                    if (figure == null) return 0;
                    int pstValue = getPSTFigure(figure, coordinates.getCoordinateY(), coordinates.getCoordinateX(), stage);

                    return (figure.getColor() == Color.WHITE) ? pstValue : -pstValue;

                }).sum();
    }

    private int evaluatePositionalRules(MatchManager manager, Color turn) {
        int score = 0;
        Field field = manager.getField();
        GameStage stage = manager.getStage();

        score += evaluateDoubledPawn(field, turn);
        if (stage != GameStage.START) {
            score += evaluatePassedPawn(field, stage, turn);
        }

        if (stage == GameStage.MIDGAME) {
            if (hasKingShield(manager, turn)) score += 20;
        }

        if (stage != GameStage.ENDGAME) {
            if (hasTwoBishopsActive(field, turn)) score += 30;
        }

        return score;
    }

    private int hangingPiecePenalty(MatchManager manager, Color turn) {
        Color oppositeTurn = manager.getOppositeColor(turn);
        Field field = manager.getField();
        int totalPenaltyScore = 0;
        List<Coordinates> opponentFigures = manager.getAvailableFigureCoordinates(oppositeTurn);
        List<Coordinates> friendlyFigures = manager.getAvailableFigureCoordinates(turn);

        for (Coordinates currentFigureCoord : friendlyFigures) {
            Figure currentFigure = field.getFigure(currentFigureCoord);

            if (currentFigure == null) continue;
            int attackers = 0;
            int defenders = 0;
            boolean attackedByCheaperPiece = false;
            int currentFigurePrice = currentFigure.getPrice();

            for (Coordinates opponentFigureCoord : opponentFigures) {
                if (manager.isProtectingSquare(opponentFigureCoord, currentFigureCoord)) {
                    Figure opponentFigure = field.getFigure(opponentFigureCoord);

                    if (opponentFigure == null) continue;
                    if (currentFigurePrice > opponentFigure.getPrice()) {
                        attackedByCheaperPiece = true;
                        break;
                    } else {
                        attackers++;
                    }
                }
            }
            if (attackedByCheaperPiece) {
                totalPenaltyScore += currentFigurePrice * 2;
                continue;
            }

            if (attackers > 0) {

                for (Coordinates friendlyCoord : friendlyFigures) {
                    if (friendlyCoord.equals(currentFigureCoord)) continue;

                    if (manager.isProtectingSquare(friendlyCoord, currentFigureCoord)) defenders++;
                }
                if (defenders < attackers) totalPenaltyScore += currentFigurePrice;
            }
        }

        return totalPenaltyScore;
    }


    private int evaluateDoubledPawn(Field field, Color color) {
        int totalPenaltyScore = 0;

        for (int i = 0; i < field.getSize(); i++) { // cols
            int totalPawns = 0;

            for (int j = 0; j < field.getSize(); j++) { // rows
                Figure currentFigure = field.getFigure(new Coordinates(i, j));

                if (currentFigure == null || currentFigure.getColor() != color
                        || currentFigure.getType() != FigureType.PAWN) continue;

                totalPawns++;

            }

            if (totalPawns > 1) {

                totalPenaltyScore += (totalPawns - 1) * 20;
            }
        }

        return totalPenaltyScore;
    }

    private int getTableRow(Color color, int row) {
        return (color == Color.WHITE) ? row : 7 - row;
    }

    private boolean hasKingShield(MatchManager manager, Color turn) {
        Coordinates king = manager.getKing(turn);
        Field field = manager.getField();

        if (king == null) {
            return false;
        }

        if (turn == Color.WHITE && king.getCoordinateY() > 5 || turn == Color.BLACK && king.getCoordinateY() < 2)
            return false;

        int pawnCount = 3;
        int row = (turn == Color.WHITE) ? king.getCoordinateY() - 1 : king.getCoordinateY() + 1;
        int col = king.getCoordinateX() - 1;

        while (pawnCount > 0) {
            Coordinates pawnCoordinate = new Coordinates(col, row);

            if (!field.isWithinBoard(pawnCoordinate)) {
                if (pawnCount == 3 || pawnCount == 1) {
                    pawnCount--;
                    col++;
                    continue;

                } else return false;
            }
            Figure figure = field.getFigure(pawnCoordinate);

            if (figure == null || figure.getColor() != turn || figure.getType() != FigureType.PAWN) return false;
            pawnCount--;
            col++;
        }

        return true;
    }

    private boolean hasTwoBishopsActive(Field field, Color turn) {

        return IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> field.getFigure(new Coordinates(j, i))))
                .filter(x -> x != null
                        && x.getColor() == turn && x.getType() == FigureType.BISHOP).count() == 2;
    }

    private int evaluatePassedPawn(Field field, GameStage stage, Color turn) {

        return IntStream.range(0, field.getSize())
                .boxed()
                .flatMap(i -> IntStream.range(0, field.getSize())
                        .mapToObj(j -> new Coordinates(j, i)))
                .mapToInt(coordinates -> {
                    Figure figure = field.getFigure(coordinates);

                    if (figure == null) return 0;

                    if (figure.getType() == FigureType.PAWN && figure.getColor() == turn
                            && isPassedPawn(field, coordinates, turn)) {

                        if (stage == GameStage.MIDGAME) return 10;
                        else return 30;
                    }

                    return 0;
                }).sum();
    }

    private boolean isPassedPawn(Field field, Coordinates pawnCoord, Color turn) {
        Color opponentColor = (turn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        int pawnX = pawnCoord.getCoordinateX();
        int pawnY = pawnCoord.getCoordinateY();

        int startY = (turn == Color.WHITE) ? pawnY - 1 : pawnY + 1;
        int endY = (turn == Color.WHITE) ? 0 : field.getSize() - 1;

        for (int col = pawnX - 1; col <= pawnX + 1; col++) {
            if (col < 0 || col > field.getSize()) continue;

            for (int row = startY;
                 (turn == Color.WHITE) ? row >= endY : row <= endY; row += (turn == Color.WHITE) ? -1 : 1) {
                if (!field.isWithinBoard(new Coordinates(col, row))) continue;
                Figure figure = field.getFigure(new Coordinates(row, col));

                if (figure != null && figure.getColor() == opponentColor && figure.getType() == FigureType.PAWN)
                    return false;
            }
        }
        return true;
    }

    private int evaluateKingSafety(MatchManager manager, Color turn) {
        Color enemyColor = manager.getOppositeColor(turn);
        Coordinates enemyKing = manager.getKing(enemyColor);
        if (enemyKing == null) return 0;

        int attackBonus = 0;
        int kingX = enemyKing.getCoordinateX();
        int kingY = enemyKing.getCoordinateY();
        List<Coordinates> friendlyFigures = manager.getAvailableFigureCoordinates(turn);

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                Coordinates zoneSquare = new Coordinates(kingX + dx, kingY + dy);

                if (manager.getField().isWithinBoard(zoneSquare)) {
                    for (Coordinates friendly : friendlyFigures) {
                        if (manager.isProtectingSquare(friendly, zoneSquare)) {
                            attackBonus += 15;
                        }
                    }
                }
            }
        }
        return attackBonus;
    }

    private int evaluateCastlingRules(MatchManager manager, Color turn) {
        int score = 0;

        if (this.hasCastled) {
            score += 60;
        } else if (hasLostCastlingRights(manager, turn)) {
            score -= 40;
        }

        return score;
    }

    private boolean hasLostCastlingRights(MatchManager manager, Color turn) {
        Coordinates kingCoords = manager.getKing(turn);
        Field field = manager.getField();
        Figure king = field.getFigure(kingCoords);
        boolean flag = true;

        if (kingCoords == null || king == null) return true;

        if (king.isMoved()) {
            return flag;
        } else {
            List<Integer> directionsX = List.of(7, 0);
            for (Integer directionX : directionsX) {
                Coordinates rookCoordinates = new Coordinates(directionX, kingCoords.getCoordinateY());
                Figure rook = field.getFigure(rookCoordinates);

                if (rook == null || rook.isMoved()) continue;

                flag = false;
            }
        }
        return flag;
    }

    public boolean isHasCastled() {
        return hasCastled;
    }

    public void setHasCastled(boolean hasCastled) {
        this.hasCastled = hasCastled;
    }

    public int getDepth() {
        return depth;
    }
}
