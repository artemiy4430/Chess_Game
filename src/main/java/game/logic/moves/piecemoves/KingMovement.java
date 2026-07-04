package game.logic.moves.piecemoves;

import game.logic.*;
import game.logic.moves.Moves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KingMovement extends Moves {
    private final int[][] directions = {
            {-1, -1}, //vlevo vverh
            {1, -1}, // vpravo vverh
            {-1, 1}, // vlevo vniz
            {1, 1}, // vpravo vniz
            {0, -1}, // vverh
            {0, 1}, // vniz
            {-1, 0}, // vlevo
            {1, 0} // vpravo
    };

    public KingMovement(Field field) {
        super(field);
    }

    @Override
    protected List<int[]> getDirections(Color color) {
        return Arrays.asList(directions);
    }

    @Override
    protected boolean isStartPosition(Coordinates coordinates) {
        Color figureColor = field.getFigure(coordinates).getColor();

        return switch (figureColor) {
            case WHITE -> Objects.equals(coordinates, new Coordinates(4, 7));
            case BLACK -> Objects.equals(coordinates, new Coordinates(4, 0));
        };

    }

    @Override
    protected boolean isSliding() {
        return false;
    }

    @Override

    public List<Coordinates> getAvailableMoves(Coordinates currentPosition) {
        if (field.getFigure(currentPosition).getType() != FigureType.KING) return null;

        List<Coordinates> availableMoves = new ArrayList<>();
        Color currentColor = field.getFigure(currentPosition).getColor();
        int currentX = currentPosition.getCoordinateX();
        int currentY = currentPosition.getCoordinateY();
        List<int[]> availableDirections = getDirections(currentColor);


        for (int i = 0; i < availableDirections.size(); i++) {
            int deltaX = availableDirections.get(i)[0];
            int deltaY = availableDirections.get(i)[1];
            Coordinates coords = getDirection(deltaX + currentX,
                    deltaY + currentY);
            if (!field.isWithinBoard(coords)) continue;
            Figure figure = field.getFigure(coords);

            if (figure != null) {
                if (figure.getColor() == currentColor) {
                } else {
                    coords.setAttackCoordinate(true);
                    availableMoves.add(coords);
                }
            } else {
                availableMoves.add(coords);
            }
        }
        return availableMoves;
    }
}