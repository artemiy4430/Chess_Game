package game;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import game.controller.WASDController;
import game.logic.*;
import game.view.Table;
import game.view.UI;
import game.view.presenters.CursorPresenter;
import game.view.presenters.LogicPresenter;

public class Main {
    public static WASDController currentController;

    // public void start(Cursor cursor, Game game, UI graphic) {
    //     currentController = new WASDController(cursor, game, graphic);
    //
    //     try {
    //         GlobalScreen.registerNativeHook();
    //         GlobalScreen.addNativeKeyListener(currentController);
    //     } catch (NativeHookException e) {
    //         e.printStackTrace();
    //     }
    //
    // }

    public static void main(String[] args) {
        Field field = new Field();
        MatchManager matchManager = new MatchManager(field);
        Table table = new Table();
        Game game = new Game(matchManager);
        UI graphic = new UI(game, table);
        Cursor cursor = new Cursor(5, 0);
        LogicPresenter gamePresenter = new LogicPresenter(graphic, cursor);
        CursorPresenter cursorPresenter = new CursorPresenter(graphic, cursor);
        game.setListener(gamePresenter);
        cursor.setCursorEventListener(cursorPresenter);
        matchManager.setCurrentTurn(Color.WHITE);
        currentController = new WASDController(cursor, game, graphic);

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(currentController);
        } catch (NativeHookException e) {
            e.printStackTrace();
        }

    }
}