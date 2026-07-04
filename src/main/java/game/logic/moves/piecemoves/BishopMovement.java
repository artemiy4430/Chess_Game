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

}
