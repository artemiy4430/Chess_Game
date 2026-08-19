package game.view.states;

import game.logic.FigureType;
import game.logic.Game;

public class ChoiceState extends MenuState {
   private Game game;

    public ChoiceState(StateManager stateManager, Game game) {
        super(stateManager, new String[]{"QUEEN", "ROOK", "KNIGHT", "BISHOP"});
        this.game = game;
    }

    @Override
    void selectOption(int selectedIndex) {
        String selection = menuOptions[selectedIndex];
        FigureType type = FigureType.QUEEN;

        if (selection.startsWith("QUEEN")) {
            type = FigureType.QUEEN;
        } else if (selection.startsWith("ROOK")) {
            type = FigureType.ROOK;
        } else if (selection.startsWith("KNIGHT")) {
            type = FigureType.KNIGHT;
        } else   type = FigureType.BISHOP;
        game.completePromotion(type);
        stateManager.setCurrentState(stateManager.getSuspendedState());
        stateManager.setSuspendedState(null);
        render();
    }

    @Override
    public void onEnter() {
        render();
    }

    private void printHeader() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("===========================");
        System.out.println("    CHOOSE_PROMOTION_TYPE   ");
        System.out.println("===========================");
        System.out.println();
    }

    @Override
    public void render() {
        printHeader();
        super.render();
    }
}
