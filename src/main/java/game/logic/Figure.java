package game.logic;

public class Figure {
    private Color color;
    private FigureType type;
    protected boolean isMoved;

    public Figure() {
    }

    public Figure(Color color, FigureType type) {
        this.color = color;
        this.type = type;
        isMoved = false;
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

    private int getPrice() {
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
}