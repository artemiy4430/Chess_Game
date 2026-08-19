package game.view.states;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

public interface AppState {
    void onEnter();
    void render();
    void handleInput(NativeKeyEvent e);
}
