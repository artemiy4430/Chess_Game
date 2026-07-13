package game;

import game.logic.Cursor;
import game.logic.Field;
import game.logic.Game;
import game.logic.MatchManager;
import game.view.Table;

public class Main {
    public static void main(String[] args) {
        Field field = new Field();
        MatchManager matchManager = new MatchManager(field);
        Table table = new Table();
        Cursor cursor = new Cursor(0,0);
        Game game = new Game(matchManager);

        table.drawTable(cursor, game);
    }
}