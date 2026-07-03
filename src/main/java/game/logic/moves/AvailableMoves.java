package game.logic.moves;

import game.logic.Coordinates;
import java.util.List;

public interface AvailableMoves {
    List<Coordinates> getAvailableMoves(Coordinates currentPosition);
}