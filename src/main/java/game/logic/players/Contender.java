package game.logic.players;

import game.logic.Color;

public abstract class Contender {
    protected String name;
    protected Color turn;

    public Contender(String name, Color turn) {
        this.name = name;
        this.turn = turn;
    }

    public String getName() {
        return name;
    }

    public Color getTurn() {
        return turn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTurn(Color turn) {
        this.turn = turn;
    }
}
