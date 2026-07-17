package game.view.presenters;

import game.logic.Cursor;
import game.logic.Field;
import game.logic.GameEventListener;
import game.view.UI;

public class LogicPresenter implements GameEventListener {
    private UI graphic;
    private Cursor cursor;

    public LogicPresenter(UI graphic, Cursor cursor) {
        this.graphic = graphic;
        this.cursor = cursor;
    }

    @Override
    public void onBoardChanged(Field field) {
        graphic.redraw(cursor);
    }

}
