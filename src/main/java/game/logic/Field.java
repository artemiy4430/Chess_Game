package game.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Field {
    private int size;
    private List<List<Cell>> board;
    private final int WHITEPAWNROW = 6;
    private final int BLACKPAWNROW = 1;
    private final int WHITEFIGUREROW = 7;
    private final int BLACKFIGUREROW = 0;

    public Field() {
        this.size = 8;
         generate();
       // customFigureSet();
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

    private void generate() {
        fillWithCells();

        IntStream.range(0, this.size)
                .forEach(i -> IntStream.range(0, this.size)
                        .forEach(j -> {
                            Coordinates coordinates = new Coordinates(j, i);
                            if (coordinates.getCoordinateY() > BLACKPAWNROW && coordinates.getCoordinateY() < WHITEPAWNROW)
                                return;
                            assignFigure(coordinates);
                        }));
    }

    private void customFigureSet() {
        fillWithCells();
        Figure figure = new Figure(Color.WHITE, FigureType.PAWN);
        getCell(new Coordinates(0, 1)).setFigure(figure);
       // assignFigure();

        // generate();
        // setFigure(new Coordinates(3, 6), new Coordinates(3, 4));
        // removeFigure(new Coordinates(4, 1));
        // setFigure(new Coordinates(3 , 1), new Coordinates(3 , 3));
        // setFigure(new Coordinates(2 , 7), new Coordinates(4 , 3));
    }

    private void assignFigure(Coordinates coordinates) {
        int coordinateY = coordinates.getCoordinateY();
        Cell currentCell = getCell(coordinates);

        if (coordinateY == 0 || coordinateY == 7) {
            currentCell.setFigure(getFigureByIndex(coordinates));
        } else if (coordinateY == BLACKPAWNROW || coordinateY == WHITEPAWNROW) {
            currentCell.setFigure(new Figure((coordinateY == WHITEPAWNROW) ? Color.WHITE : Color.BLACK, FigureType.PAWN));
        }
    }

    private Figure getFigureByIndex(Coordinates coordinates) {
        int coordinateY = coordinates.getCoordinateY();
        int coordinateX = coordinates.getCoordinateX();

        if (coordinateY != WHITEFIGUREROW && coordinateY != 0) return null;
        Color color = (coordinateY == WHITEFIGUREROW) ? Color.WHITE : Color.BLACK;
        int targetX = (coordinateX > 4) ? (this.size - 1 - coordinateX) : coordinateX;

        return switch (targetX) {
            case 0 -> new Figure(color, FigureType.ROOK);
            case 1 -> new Figure(color, FigureType.KNIGHT);
            case 2 -> new Figure(color, FigureType.BISHOP);
            case 3 -> new Figure(color, FigureType.QUEEN);
            case 4 -> new Figure(color, FigureType.KING);
            default -> throw new IllegalStateException("Unexpected value: " + coordinateX);
        };
    }

    private void fillWithCells() {
        this.board = IntStream.range(0, this.size)
                .mapToObj(i -> IntStream.range(0, this.size)
                        .mapToObj(j -> {
                            Cell currentCell = new Cell();

                            if ((i + j) % 2 == 0) {
                                currentCell.setColor(Color.WHITE);
                            } else {
                                currentCell.setColor(Color.BLACK);
                            }

                            return currentCell;
                        }).collect(Collectors.toCollection(ArrayList::new)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
