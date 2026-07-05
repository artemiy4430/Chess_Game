package game.logic.moves;

import game.logic.*;

import java.util.ArrayList;
import java.util.List;

public abstract class Moves implements AvailableMoves {
    protected Field field;
//
//    protected final int[][] directions = {
//            // Diagonals
//            {-1, -1}, // vlevo vverh
//            {1, -1},  // vlevo vniz - tolko bit`
//            {-1, 1},  // vpravo vverh
//            {1, 1},     // vpravo vniz
//
//            //Orthogonals
//            {0, -1}, // vverh
//            {0, 1}, // vniz
//            {-1,0}, // vlevo
//            {1, 0} // vpravo
//            //KnightMovement class will need to ignore this directions array entirely and define its own int[][]
//            // knightJumps array containing the 8 specific L-shapes (e.g., { {-2, -1}, {-2, 1}, {-1, -2} ... })
//    };


    public Moves(Field field) {
        this.field = field;
    }

    public Coordinates getDirection(int x, int y) {
        return new Coordinates(x, y);
    }

    protected abstract List<int[]> getDirections(Color color);

    protected abstract boolean isStartPosition(Coordinates coordinates);

    protected abstract boolean isSliding();

    @Override
    public List<Coordinates> getAvailableMoves(Coordinates currentPosition) {
        Figure currentFigure = field.getFigure(currentPosition);
        if (currentFigure == null) return new ArrayList<>();

        List<Coordinates> availableMoves = new ArrayList<>();
        Color currentColor = currentFigure.getColor();
        int currentX = currentPosition.getCoordinateX();
        int currentY = currentPosition.getCoordinateY();
        List<int[]> availableDirections = getDirections(currentColor);

        for (int i = 0; i < availableDirections.size(); i++) {
            int deltaX = availableDirections.get(i)[0];
            int deltaY = availableDirections.get(i)[1];
            int limit = isSliding() ? field.getSize() : 2;

            for (int j = 1; j < limit; j++) {
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