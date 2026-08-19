package game.view.states;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import game.view.GameConfig;

public abstract class MenuState implements AppState {
    protected StateManager stateManager;
    protected int selectedIndex;
    protected String[] menuOptions;
    protected GameConfig gameConfig;

    public MenuState(StateManager stateManager, String[] menuOptions) {
        this.stateManager = stateManager;
        this.selectedIndex = 0;
        this.menuOptions = menuOptions;
        this.gameConfig = stateManager.getGameConfig();
    }

    abstract void selectOption(int selectedIndex);

    @Override
    public void render() {
        for (int i = 0; i < menuOptions.length; i++) {
            if (i == selectedIndex) {
                System.out.print("\u001B[40m >  " + menuOptions[i] + "  < \u001B[K\u001B[0m\n");
            } else {
                System.out.print("\u001B[44m" + menuOptions[i] + " \u001B[K\u001B[0m\n");
            }
        }
    }

    @Override
    public void handleInput(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == NativeKeyEvent.VC_W || keyCode == NativeKeyEvent.VC_UP) {
            selectedIndex = Math.max(0, selectedIndex - 1);
            render();
        } else if (keyCode == NativeKeyEvent.VC_S || keyCode == NativeKeyEvent.VC_DOWN) {
            selectedIndex = Math.min(menuOptions.length - 1, selectedIndex + 1);
            render();
        } else if (keyCode == NativeKeyEvent.VC_ENTER || keyCode == NativeKeyEvent.VC_SPACE || keyCode == NativeKeyEvent.VC_TAB) {
            selectOption(this.selectedIndex);
        }
    }
}