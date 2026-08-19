package game.view.states;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import game.view.UI;

public class GameOverState implements AppState {
    private StateManager stateManager;
    private UI graphic;

    public GameOverState(StateManager stateManager, UI graphic) {
        this.stateManager = stateManager;
        this.graphic = graphic;
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void render() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("===========================");
        System.out.println("        GAME OVER          ");
        System.out.println("===========================");
        System.out.println();
        graphic.printResult();
        System.out.println();
        System.out.println("Press [SPACE] or [ENTER] to return to Main Menu...");
    }

    @Override
    public void handleInput(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == NativeKeyEvent.VC_ENTER || keyCode == NativeKeyEvent.VC_SPACE) {
            stateManager.setCurrentState(new MainMenuState(stateManager));
        }
    }
}
