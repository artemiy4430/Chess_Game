package game.view.states;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import game.view.GameConfig;

public class StateManager implements NativeKeyListener {
    private AppState currentState;
    private GameConfig gameConfig = new GameConfig();
    private GameState suspendedState = null;

    public void setCurrentState(AppState currentState) {
        this.currentState = currentState;
        this.currentState.onEnter();
        this.currentState.render();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (currentState != null) {
            currentState.handleInput(e);
        }
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public GameState getSuspendedState() {
        return suspendedState;
    }

    public AppState getCurrentState() {
        return currentState;
    }

    public void setSuspendedState(GameState suspendedState) {
        this.suspendedState = suspendedState;
    }
}