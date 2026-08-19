package game.logic;

import java.util.Objects;

public class Cell {
    private Color color;
    private Figure figure;
    private boolean isAttacked;

    public Cell() {}

    public Cell(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public boolean isAttacked() {
        return isAttacked;
    }

    public void setAttacked(boolean attacked) {
        isAttacked = attacked;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return color == cell.color && Objects.equals(figure, cell.figure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, figure);
    }
}
