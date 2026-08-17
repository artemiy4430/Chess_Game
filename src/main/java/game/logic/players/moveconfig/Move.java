package game.logic.players.moveconfig;

import game.logic.Coordinates;
import game.logic.Figure;
import game.logic.FigureType;

public record Move(
        Coordinates from,
        Coordinates to,
        Figure movedFigure,
        Figure capturedFigure,      // Null if empty square
        FigureType promotionType,    // Null if not a promotion
        boolean isEnPassant,
        boolean isCastle
) {
    @Override
    public String toString() {
        return "Move{" +
                "from=" + from +
                ", to=" + to +
                ", movedFigure=" + movedFigure +
                ", capturedFigure=" + capturedFigure +
                ", promotionType=" + promotionType +
                ", isEnPassant=" + isEnPassant +
                ", isCastle=" + isCastle +
                '}';
    }
}
