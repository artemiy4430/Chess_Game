package game.controller;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import game.logic.Cursor;
import game.logic.Directions;
import game.logic.Game;
import game.view.UI;

public class WASDController implements NativeKeyListener {
    private Cursor cursor;
    private Game game;
    private UI graphic;

    public WASDController(Cursor cursor, Game game, UI graphic) {
        this.cursor = cursor;
        this.game = game;
        this.graphic = graphic;
    }

    public static void gameOver(NativeKeyListener listener) {
        GlobalScreen.removeNativeKeyListener(listener);
    }

    public void moveCursor(Directions direction) {
       // if (game.get.isGameOver()) return;
        switch (direction) {
            case UP -> cursor.moveUp();
            case DOWN -> cursor.moveDown();
            case LEFT -> cursor.moveLeft();
            case RIGHT -> cursor.moveRight();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        switch (nativeEvent.getKeyCode()) {
            case NativeKeyEvent.VC_W: {
                moveCursor(Directions.UP);
                graphic.redraw(cursor);
            }
            break;

            case NativeKeyEvent.VC_A: {
                moveCursor(Directions.LEFT); // ENUM (directions)
                graphic.redraw(cursor);
            }
            break;

            case NativeKeyEvent.VC_S: {
                moveCursor(Directions.DOWN);
                graphic.redraw(cursor);
            }

            break;
            case NativeKeyEvent.VC_D: {
                moveCursor(Directions.RIGHT);
                graphic.redraw(cursor);
            }
            break;

            case NativeKeyEvent.VC_SPACE: {
                game.processSpace(cursor);
            }
            break;
        }
    }


}
