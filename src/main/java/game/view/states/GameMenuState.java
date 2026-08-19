package game.view.states;

import game.logic.Game;
import game.logic.MatchManager;
import game.view.UI;

public class GameMenuState extends MenuState {
    private UI graphic;
    private boolean toDraw;
    private boolean toResign;

    public GameMenuState(StateManager stateManager, UI graphic) {
        super(stateManager, new String[0]);
        this.graphic = graphic;
        this.toDraw = false;
        this.toResign = false;
    }

    @Override
    void selectOption(int selectedIndex) {
        String selection = menuOptions[selectedIndex];
        Game game = graphic.getGame();

        if (selection.startsWith("RESUME")) {
            stateManager.setCurrentState(stateManager.getSuspendedState());
            stateManager.setSuspendedState(null);
            return;
        } else if (selection.startsWith("RESIGN")) {
            toResign = true;
        } else if (selection.startsWith("OFFER DRAW")) {
            toDraw = true;
        } else if (selection.startsWith("YES")) {
            MatchManager manager = game.getMatchManager();
            game.setGameOver(true);
            if (toResign) {
                manager.setWinner(manager.getOppositeColor(manager.getCurrentTurn()));
            } else {
                manager.setTie(true);
            }
            stateManager.setCurrentState(new GameOverState(stateManager, graphic));
            return;
        } else if (selection.startsWith("NO")) {
            this.toDraw = false;
            this.toResign = false;
        } else {
            stateManager.setCurrentState(new MainMenuState(stateManager));
            return;
        }

        updateMenuText();
        render();
    }

    @Override
    public void onEnter() {
        updateMenuText();
    }

    private void updateMenuText() {
        if (toDraw || toResign) {
            this.menuOptions = new String[]{"YES", "NO"};
        } else if (!gameConfig.isVsBot) {
            this.menuOptions = new String[]{"RESUME", "RESIGN", "OFFER DRAW"};
        } else {
            this.menuOptions = new String[]{"RESUME", "RESIGN"};
        }

        if (this.selectedIndex >= this.menuOptions.length) {
            this.selectedIndex = this.menuOptions.length - 1;
        }
    }

    private void printHeader() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("===========================");
        if (toResign || toDraw) {
            System.out.println("    CONFIRM_YOUR_CHOICE   ");
        } else System.out.println("    GAME_MENU   ");
        System.out.println("===========================");
        System.out.println();
    }


    @Override
    public void render() {
        printHeader();
        super.render();
    }
}
