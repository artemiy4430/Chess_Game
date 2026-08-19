package game.view.states;

public class MainMenuState extends MenuState {

    public MainMenuState(StateManager stateManager) {
        super(stateManager, new String[]{"START", "Settings", "Exit"});
    }

    @Override
    public void onEnter() {}

    @Override
    void selectOption(int selectedIndex) {
        switch (selectedIndex) {
            case 0 -> stateManager.setCurrentState(new GameState(stateManager));
            case 1 -> stateManager.setCurrentState(new SettingsState(stateManager));
            case 2 -> System.exit(0);
        }
    }

    @Override
    public void render() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("===========================");
        System.out.println("       MAIN MENU           ");
        System.out.println("===========================");
        System.out.println();

        super.render();
    }
}
