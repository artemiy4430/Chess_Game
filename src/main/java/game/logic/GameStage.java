package game.logic;

public enum GameStage {
    START(0), MIDGAME(1), ENDGAME(2);

    private int index;

    GameStage(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
