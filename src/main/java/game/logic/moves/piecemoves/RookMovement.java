package game.logic.moves.piecemoves;

import game.logic.*;
import game.logic.moves.Moves;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RookMovement extends Moves {

    private final int[][] directions = {
            {-1, 0}, // vlevo
            {1, 0}, // vpravo
            {0, -1}, // vverh
            {0, 1} // vniz
    };

    public RookMovement(Field field) {
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
            case WHITE -> Objects.equals(coordinates, new Coordinates(0, 7)) ||
                    Objects.equals(coordinates, new Coordinates(7, 7));
            case BLACK -> Objects.equals(coordinates, new Coordinates(0, 0)) ||
                    Objects.equals(coordinates, new Coordinates(7, 0));
        };
    }

    @Override
    protected boolean isSliding() {
        return true;
    }

//    @Override
//    public List<Coordinates> getAvailableMoves(Coordinates currentPosition) {
//        if (field.getFigure(currentPosition).getType() != FigureType.ROOK) return null;
//
//        List<Coordinates> availableMoves = new ArrayList<>();
//        Color currentColor = field.getFigure(currentPosition).getColor();
//        int currentX = currentPosition.getCoordinateX();
//        int currentY = currentPosition.getCoordinateY();
//        List<int[]> availableDirections = getDirections(currentColor);
//
//        for (int i = 0; i < availableDirections.size(); i++) {
//            int deltaX = availableDirections.get(i)[0];
//            int deltaY = availableDirections.get(i)[1];
//
//            for (int j = 1; j < field.getSize(); j++) {
//                Coordinates moveCoordinates = new Coordinates(
//                        currentX + deltaX * j,
//                        currentY + deltaY * j
//                );
//
//                if (!field.isWithinBoard(moveCoordinates)) break;
//                Figure figure = field.getFigure(moveCoordinates);
//
//                if (figure != null) {
//                    if (figure.getColor() == currentColor) {
//                    } else {
//                        moveCoordinates.setAttackCoordinate(true);
//                        availableMoves.add(moveCoordinates);
//                    }
//                    break;
//                } else {
//                    availableMoves.add(moveCoordinates);
//                }
//            }
//        }
//        return availableMoves;
//    }
}
