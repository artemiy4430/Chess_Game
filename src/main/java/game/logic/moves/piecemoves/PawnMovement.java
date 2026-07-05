package game.logic.moves.piecemoves;

import game.logic.*;
import game.logic.moves.Moves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PawnMovement extends Moves {
    /// / TODO взятие на проходе
    private final int[][] directions = {
            {-1, -1}, //vlevo vverh
            {1, -1}, // vpravo vverh +
            {-1, 1}, // vlevo vniz
            {1, 1}, // vpravo vniz +
            {0, -1}, // vverh
            {0, 1} // vniz +
    };

    public PawnMovement(Field field) {
        super(field);
    }

    @Override
    protected List<int[]> getDirections(Color color) {
        return switch (color) {
            case WHITE -> Arrays.stream(directions).filter(vector -> vector[1] == -1).toList();

            case BLACK -> Arrays.stream(directions).filter(vector -> vector[1] == 1).toList();
        };
    }

    @Override
    protected boolean isStartPosition(Coordinates coordinates) {
        Color figureColor = field.getFigure(coordinates).getColor();
        return (figureColor == Color.WHITE) ? coordinates.getCoordinateY() == 6 : coordinates.getCoordinateY() == 1; //// TODO make constants
    }

    @Override
    protected boolean isSliding() {
        return false;
    }

    @Override
    public List<Coordinates> getAvailableMoves(Coordinates currentPosition) {
        if (field.getFigure(currentPosition).getType() != FigureType.PAWN) return null;

        List<Coordinates> availableMoves = new ArrayList<>();
        Color currentColor = field.getFigure(currentPosition).getColor();
        int currentX = currentPosition.getCoordinateX();
        int currentY = currentPosition.getCoordinateY();
        boolean isStartPosition = isStartPosition(currentPosition);
        List<int[]> availableDirections = getDirections(currentColor);
        boolean isMoved = field.getFigure(currentPosition).isMoved();

        for (int i = 0; i < availableDirections.size(); i++) {
            int deltaX = availableDirections.get(i)[0];
            int deltaY = availableDirections.get(i)[1];
            Coordinates coords = getDirection(deltaX + currentX,
                    deltaY + currentY);

            if (!field.isWithinBoard(coords)) continue;

            if (i != availableDirections.size() - 1) {
                Figure figure = field.getFigure(coords);

                if (figure != null && figure.getColor() != currentColor) {
                    coords.setAttackCoordinate(true);
                    availableMoves.add(coords);
                }

            } else {
                for (int j = 1; j <= 2; j++) {

                    if (j == 2 && !isStartPosition && isMoved) {
                        break;
                    }
                    Coordinates moveCoordinates = new Coordinates(
                            currentPosition.getCoordinateX() + deltaX * j,
                            currentPosition.getCoordinateY() + deltaY * j
                    );

                    if (!field.isWithinBoard(moveCoordinates)) break;
                    Figure figure = field.getFigure(moveCoordinates);

                    if (figure != null) break;
                    availableMoves.add(moveCoordinates);
                }
            }
        }
        return availableMoves;
    }

//    public Coordinates getDirection(int x, int y) {
//        return new Coordinates(x, y);
//    }

}

// getAvailableMoves+ getDirection+