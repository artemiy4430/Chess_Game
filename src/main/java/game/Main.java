package game;


import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import game.controller.WASDController;
import game.logic.*;
import game.logic.players.Bot;
import game.logic.players.Contender;
import game.view.Table;
import game.view.UI;
import game.view.presenters.CursorPresenter;
import game.view.presenters.LogicPresenter;

// set JAVA_HOME=C:\Users\artem\.jdks\corretto-22.0.2
// set PATH=%JAVA_HOME%\bin;%PATH%             ]
//java -version
//mvn clean package
//java -jar target/Chess-1.0-SNAPSHOT.jar
//chcp 65001

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
        Bot bot1 = new Bot("bot", Color.BLACK, true);
        Bot bot2 = new Bot("bot", Color.WHITE, true);
        Field field = new Field();
        MatchManager matchManager = new MatchManager(field);
        Table table = new Table();
        Game game = new Game(matchManager, bot1, bot2);
        // Game game = new Game(matchManager, bot2);
        UI graphic = new UI(game, table);
        Cursor cursor = new Cursor(4, 6);
        LogicPresenter gamePresenter = new LogicPresenter(graphic, cursor);
        CursorPresenter cursorPresenter = new CursorPresenter(graphic, cursor);

        game.setListener(gamePresenter);
        cursor.setCursorEventListener(cursorPresenter);
        matchManager.setCurrentTurn(Color.WHITE);
        currentController = new WASDController(cursor, game, graphic);
        graphic.redraw(cursor);
        game.handleBotTurn(bot2);

        //    try {
        //        GlobalScreen.registerNativeHook();
        //        GlobalScreen.addNativeKeyListener(currentController);
        //    } catch (NativeHookException e) {
        //        e.printStackTrace();
        //    }

    }
}