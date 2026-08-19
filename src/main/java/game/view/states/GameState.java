package game.view.states;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import game.logic.*;
import game.view.GameConfig;
import game.view.Table;
import game.view.UI;
import game.view.presenters.CursorPresenter;
import game.view.presenters.LogicPresenter;


public class GameState extends MenuState {
    private Game game;
    private Cursor cursor;
    private UI graphic;
    private boolean isInitialized = false;
    private boolean isTransitioning = false;

    public GameState(StateManager stateManager) {
        super(stateManager, null);
        GameConfig config = stateManager.getGameConfig();
        Field field = new Field();
        MatchManager matchManager = new MatchManager(field);
        Table table = new Table();

        this.game = (config.isVsBot)
                ? new Game(matchManager, this.gameConfig.selectedColor, this.gameConfig.selectedDifficulty)
                : new Game(matchManager);
        this.graphic = new UI(game, table);
        this.cursor = new Cursor(4, 6);
    }

    @Override
    void selectOption(int selectedIndex) {
    }

    @Override
    public void onEnter() {
        if (!isInitialized) {
            CursorPresenter cursorPresenter = new CursorPresenter(graphic, cursor);
            LogicPresenter logicPresenter = new LogicPresenter(graphic, cursor);

            cursor.setCursorEventListener(cursorPresenter);
            game.setListener(logicPresenter);
            isInitialized = true;
            render();
            if (gameConfig.isVsBot && gameConfig.selectedColor == Color.BLACK) {
                game.handleBotTurn();
            }
        } else render();
    }

    @Override
    public void render() {
        graphic.redraw(cursor);
    }

    public void moveCursor(Directions direction) {
        switch (direction) {
            case UP -> cursor.moveUp();
            case DOWN -> cursor.moveDown();
            case LEFT -> cursor.moveLeft();
            case RIGHT -> cursor.moveRight();
        }
    }

    @Override
    public void handleInput(NativeKeyEvent e) {
        MatchManager manager = game.getMatchManager();

        if (isTransitioning) {
            return;
        }
        if (this.game == null) {
            System.out.println("Wait, game is initializing...");
            return;
        }

        switch (e.getKeyCode()) {
            case NativeKeyEvent.VC_UP:
            case NativeKeyEvent.VC_W: {
                moveCursor(Directions.UP);
                render();
            }
            break;
            case NativeKeyEvent.VC_LEFT:
            case NativeKeyEvent.VC_A: {
                moveCursor(Directions.LEFT);
                render();
            }
            break;
            case NativeKeyEvent.VC_DOWN:
            case NativeKeyEvent.VC_S: {
                moveCursor(Directions.DOWN);
                render();
            }
            break;
            case NativeKeyEvent.VC_RIGHT:
            case NativeKeyEvent.VC_D: {
                moveCursor(Directions.RIGHT);
                render();
            }
            break;

            case NativeKeyEvent.VC_ENTER:
            case NativeKeyEvent.VC_SPACE: {
                game.processSpace(cursor);
            }
            break;
            case NativeKeyEvent.VC_Q: {
                stateManager.setSuspendedState(this);
                stateManager.setCurrentState(new GameMenuState(stateManager, graphic));
            }
        }
        if (manager.isPromotion()) {
            ChoiceState choiceState = new ChoiceState(stateManager, game);
            stateManager.setSuspendedState(this);
            stateManager.setCurrentState(choiceState);
            render();
        }

        if (game.isGameOver()) {
            isTransitioning = true;
            render();

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                stateManager.setCurrentState(new GameOverState(stateManager, graphic));

            }).start();

        }
    }
}
