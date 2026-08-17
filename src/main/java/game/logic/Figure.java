package game.logic;

import java.util.Objects;

public class Figure {
    private Color color;
    private FigureType type;
    private boolean isMoved;
    private int moveCount;
    private boolean isLastMoved;

    public Figure() {
    }

    public Figure(Color color, FigureType type) {
        this.color = color;
        this.type = type;
        isMoved = false;
        moveCount = 0;
        isLastMoved = false;
    }

    public Color getColor() {
        return color;
    }

    public FigureType getType() {
        return type;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setType(FigureType type) {
        this.type = type;
    }

    public int getPrice() {
        return switch (type) {
            case PAWN -> 1;
            case ROOK -> 5;
            case KNIGHT, BISHOP -> 3;
            case KING -> 0;
            case QUEEN -> 9;
        };
    }


    public boolean isMoved() {
        return isMoved;
    }

    public void setMoved(boolean moved) {
        isMoved = moved;
    }

    public void promoteToQueen() {
        setType(FigureType.QUEEN);
    }

    public void promoteFigure(FigureType type) {
        setType(type);
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void incrementMoveCount() {
        this.moveCount += 1;
    }

    public boolean isLastMoved() {
        return isLastMoved;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    public void setLastMoved(boolean lastMoved) {
        isLastMoved = lastMoved;
    }

    @Override
    public String toString() {
        return type + " " + color;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Figure figure = (Figure) o;
        return isMoved == figure.isMoved && moveCount == figure.moveCount && isLastMoved == figure.isLastMoved && color == figure.color && type == figure.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type, isMoved, moveCount, isLastMoved);
    }
}