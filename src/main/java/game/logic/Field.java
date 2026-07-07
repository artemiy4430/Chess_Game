package game.logic;

import java.util.ArrayList;
import java.util.List;

public class Field {
    private int size;
    private List<List<Cell>> board;

    public Field() { //// to change
        this.board = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            List<Cell> row = new ArrayList<>();

            for (int j = 0; j < size; j++) {
                row.add(new Cell());
            }

            this.board.add(row);
        }
    }

    public int getSize() {
        return size;
    }

    public List<List<Cell>> getBoard() {
        return board;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setBoard(List<List<Cell>> board) {
        this.board = board;
    }

    public boolean isWithinBoard(Coordinates coordinates) {
        int col = coordinates.getCoordinateX();
        int row = coordinates.getCoordinateY();
        return (col >= 0 && col < size && row >= 0 && row < size);
    }

    public Figure getFigure(Coordinates coordinates) {
        if (contains(coordinates)) {
            return board.get(coordinates.getCoordinateY()).get(coordinates.getCoordinateX()).getFigure();
        }
        return null;
    }

    private boolean contains(Coordinates coordinates) {
        return board.get(coordinates.getCoordinateY()).get(coordinates.getCoordinateX()).getFigure() != null;
    }

    public Cell getCell(Coordinates coordinates) {
        if (isWithinBoard(coordinates))
            return board.get(coordinates.getCoordinateY()).get(coordinates.getCoordinateX());
        return null;
    }

    public void setFigure(Coordinates oldCoordinates, Coordinates newCoordinates) {
        Figure figure = getFigure(oldCoordinates);

        if (!isWithinBoard(newCoordinates) || figure == null) return;
        Cell newCell = getCell(newCoordinates);
        Cell currentCell = getCell(oldCoordinates);

        if (newCell.getFigure() == null) {
            newCell.setFigure(figure);
            currentCell.setFigure(null);
        }
    }

    public void setFigure(Figure figure, Coordinates newCoordinates) { // overloading
        if (!isWithinBoard(newCoordinates) || figure == null) return;
        Cell newCell = getCell(newCoordinates);

        if (newCell.getFigure() == null) {
            newCell.setFigure(figure);
        }
    }


    public void removeFigure(Coordinates coordinates) {
        Cell cell = getCell(coordinates);
        Figure figure = cell.getFigure();

        if (figure != null) {
            cell.setFigure(null);
        }
    }

}
