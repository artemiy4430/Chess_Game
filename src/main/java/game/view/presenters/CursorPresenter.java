package game.view.presenters;

import game.logic.Cursor;
import game.logic.CursorEventListener;
import game.view.UI;

public class CursorPresenter implements CursorEventListener {
    private UI graphic;
    private Cursor cursor;

    public CursorPresenter(UI graphic, Cursor cursor) {
        this.graphic = graphic;
        this.cursor = cursor;
    }

    @Override
    public void onCursorMoved(int coordinateX, int coordinateY) {
        graphic.redraw(cursor);
    }

    public UI getGraphic() {
        return graphic;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setGraphic(UI graphic) {
        this.graphic = graphic;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }
}

