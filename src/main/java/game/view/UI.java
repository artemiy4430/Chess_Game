package game.view;

import game.logic.Color;
import game.logic.Cursor;
import game.logic.Game;
import game.logic.MatchManager;

public class UI {
    private Game game;
    private Table table;

    public UI(Game game, Table table) {
        this.game = game;
        this.table = table;
    }

    public void redraw(Cursor cursor) {
        if (game.isGameOver()) {
            printResult();
        }
        table.drawTable(cursor, game);
        System.out.println();
        printScore();
    }

    private void printScore() {
        MatchManager manager = game.getMatchManager();

        System.out.println("WHITE " + manager.getTotalScore(Color.WHITE)
                + " : " + manager.getTotalScore(Color.BLACK) + " BLACK ");
        System.out.println(game.getCurrentContender() + " turn ");
    }

    public void printResult() {
        MatchManager manager = game.getMatchManager();
        Color winner = manager.getWinner();
        System.out.println("GAME OVER");
        if (winner != null) {
            System.out.println("WINNER: " + winner);
        } else {
            if (manager.isStaleMate()) {
                System.out.println("STALEMATE");
            } else System.out.println("TIE");
        }
    }

    public Game getGame() {
        return game;
    }

    public Table getTable() {
        return table;
    }
}
