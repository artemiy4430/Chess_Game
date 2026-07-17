package game.logic;

public class Cursor {
    private int cursorCoordinateX;
    private int cursorCoordinateY;
    private final int maxSize = 8;
    private CursorEventListener cursorEventListener;

    public Cursor(int cursorCoordinateX, int cursorCoordinateY) {
        this.cursorCoordinateX = cursorCoordinateX;
        this.cursorCoordinateY = cursorCoordinateY;
    }

    public int getCursorCoordinateX() {
        return cursorCoordinateX;
    }

    public int getCursorCoordinateY() {
        return cursorCoordinateY;
    }

    public void setCursorEventListener(CursorEventListener cursorEventListener) {
        this.cursorEventListener = cursorEventListener;
    }

    public void moveUp() {
        if (cursorCoordinateY - 1 < 0) return;
        cursorCoordinateY--;
        cursorEventListener.onCursorMoved(cursorCoordinateX, cursorCoordinateY);
    }

    public void moveDown() {
        if (cursorCoordinateY + 1 >= maxSize) return;
        cursorCoordinateY++;
        cursorEventListener.onCursorMoved(cursorCoordinateX, cursorCoordinateY);
    }

    public void moveLeft() {
        if (cursorCoordinateX - 1 < 0) return;
        cursorCoordinateX--;
        cursorEventListener.onCursorMoved(cursorCoordinateX, cursorCoordinateY);
    }

    public void moveRight() {
        if (cursorCoordinateX + 1 >= maxSize) return;
        cursorCoordinateX++;
        cursorEventListener.onCursorMoved(cursorCoordinateX, cursorCoordinateY);
    }

    public void setCursorCoordinateX(int cursorCoordinateX) {
        this.cursorCoordinateX = cursorCoordinateX;
    }

    public void setCursorCoordinateY(int cursorCoordinateY) {
        this.cursorCoordinateY = cursorCoordinateY;
    }
}
