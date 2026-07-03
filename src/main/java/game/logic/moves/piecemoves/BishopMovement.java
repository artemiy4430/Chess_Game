package game.logic.moves.piecemoves;

import game.logic.*;
import game.logic.moves.Moves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BishopMovement extends Moves {

    protected final int[][] directions = {
            // Diagonals
            {-1, -1}, // vlevo vverh
            {1, -1},  // vlevo vniz
            {-1, 1},  // vpravo vverh
            {1, 1},     // vpravo vniz
    };

    public BishopMovement(Field field) {
        super(field);
    }

    @Override
    protected List<int[]> getDirections(Color color) {
        return Arrays.asList(directions);
    }

    @Override
    protected boolean isStartPosition(Coordinates coordinates) {
        return false;
    } // isnt needed

    @Override
    protected boolean isSliding() {
        return true;
    }

    @Override
    public List<Coordinates> getAvailableMoves(Coordinates currentPosition) {
        if (field.getFigure(currentPosition).getType() != FigureType.BISHOP) return null;

        List<Coordinates> availableMoves = new ArrayList<>();
        Color currentColor = field.getFigure(currentPosition).getColor();
        int currentX = currentPosition.getCoordinateX();
        int currentY = currentPosition.getCoordinateY();
        List<int[]> availableDirections = getDirections(currentColor);

        for (int i = 0; i < availableDirections.size(); i++) {
            int deltaX = availableDirections.get(i)[0];
            int deltaY = availableDirections.get(i)[1];

            for (int j = 1; j < field.getSize(); j++) {
                Coordinates moveCoordinates = new Coordinates(
                        currentX + deltaX * j,
                        currentY + deltaY * j
                );

                if (!field.isWithinBoard(moveCoordinates)) break;
                Figure figure = field.getFigure(moveCoordinates);

                if (figure != null) {
                    if (figure.getColor() == currentColor) {
                    } else {
                        moveCoordinates.setAttackCoordinate(true);
                        availableMoves.add(moveCoordinates);
                    }
                    break;
                } else {
                    availableMoves.add(moveCoordinates);
                }
            }
        }
        return availableMoves;
    }

}
