package game.view;

import game.logic.Color;

public class GameConfig {
    public boolean isVsBot;
    public int selectedDifficulty = 1;
    public Color selectedColor = Color.WHITE;


    public GameConfig(boolean isVsBot, int selectedDifficulty, Color selectedColor) {
        this.isVsBot = isVsBot;
        this.selectedDifficulty = selectedDifficulty;
        this.selectedColor = selectedColor;
    }

    public GameConfig() {
        this.isVsBot = false;
    }

    public void setVsBot(boolean vsBot) {
        isVsBot = vsBot;
    }

    public void setSelectedDifficulty(int selectedDifficulty) {
        this.selectedDifficulty = selectedDifficulty;
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
    }
}
