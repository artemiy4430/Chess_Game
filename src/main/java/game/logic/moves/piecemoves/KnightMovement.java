package game.logic.moves.piecemoves;

import game.logic.*;
import game.logic.moves.Moves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KnightMovement extends Moves {

    protected final int[][] directions = {
            {-1, -2}, // vertical-vverh-vlevo
            {1, -2}, // vertical-vverh-vpravo
            {-2, -1}, // horiz-vverh-vlevo
            {2, -1}, // horiz-vverh-vpravo
            {-2, 1}, // horiz-vniz-vlevo
            {2, 1}, // horiz-vniz-vpravo
            {-1, 2}, // vertical-vniz-vlevo
            {1, 2}   // vertical-vniz-vpravo
    };

    public KnightMovement(Field field) {
        super(field);
    }

    @Override
    protected List<int[]> getDirections(Color color) {
        return Arrays.asList(directions);
    }

    @Override
    protected boolean isStartPosition(Coordinates coordinates) {
        return false; // no need
    }

    @Override
    protected boolean isSliding() {
        return false;
    }

}