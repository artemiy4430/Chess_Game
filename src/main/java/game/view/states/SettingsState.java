package game.view.states;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import game.logic.Color;
import game.view.GameConfig;

public class SettingsState extends MenuState {

    public SettingsState(StateManager stateManager) {
        super(stateManager, new String[0]);
    }

    @Override
    void selectOption(int selectedIndex) {
        String selection = menuOptions[selectedIndex];

        if (selection.startsWith("Game Mode")) {
            gameConfig.isVsBot = !gameConfig.isVsBot;
        } else if (selection.startsWith("DIFFICULTY")) {
            gameConfig.selectedDifficulty = (gameConfig.selectedDifficulty % 4) + 1;
        } else if (selection.startsWith("SELECTED COLOR")) {
            gameConfig.selectedColor = (gameConfig.selectedColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        } else if (selection.equals("EXIT")) {
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

    @Override
    public void render() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("===========================");
        System.out.println("    CHESS_GAME   ");
        System.out.println("===========================");
        System.out.println();

        super.render();
    }

    private void updateMenuText() {
        if (gameConfig.isVsBot) {
            this.menuOptions = new String[4];
            this.menuOptions[0] = "Game Mode: Player vs Bot";
            this.menuOptions[1] = getDifficultyString();
            this.menuOptions[2] = "SELECTED COLOR: " + (gameConfig.selectedColor == Color.WHITE ? "WHITE" : "BLACK");
            this.menuOptions[3] = "EXIT";
        } else {
            this.menuOptions = new String[2];
            this.menuOptions[0] = "Game Mode: Player vs Player";
            this.menuOptions[1] = "EXIT";
        }

        if (this.selectedIndex >= this.menuOptions.length) {
            this.selectedIndex = this.menuOptions.length - 1;
        }
    }

    private String getDifficultyString() {
        return switch (gameConfig.selectedDifficulty) {
            case 1 -> "DIFFICULTY: BOT ANDREW: LEVEL 1 ";
            case 2 -> "DIFFICULTY: BOT TIM: LEVEL 2 ";
            case 3 -> "DIFFICULTY: BOT DEN: LEVEL 3 ";
            case 4 -> "DIFFICULTY: BOT MISHA: LEVEL 4 (MAX) ";
            default -> "DIFFICULTY: UNKNOWN";
        };
    }

}