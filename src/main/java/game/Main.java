package game;


import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import game.logic.*;
import game.view.states.MainMenuState;
import game.view.states.StateManager;

import java.util.logging.Level;
import java.util.logging.Logger;

//// pls run this commands in strict sequence starting from the top
// set JAVA_HOME=C:\Users\artem\.jdks\corretto-22.0.2
// set PATH=%JAVA_HOME%\bin;%PATH%
//java -version
//mvn clean package
//chcp 65001
//java -jar target/Chess-1.0-SNAPSHOT.jar


public class Main {
    public static void run() {
        StateManager stateManager = new StateManager();
        stateManager.setCurrentState(new MainMenuState(stateManager));

        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(stateManager);

        } catch (NativeHookException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void main(String[] args) {
        run();
    }
}