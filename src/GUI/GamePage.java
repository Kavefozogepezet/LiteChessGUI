package GUI;

import app.Main;
import engine.*;
import game.Clock;
import game.Game;
import jdk.jshell.spi.ExecutionControl;
import player.EnginePlayer;
import player.HumanPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GamePage implements GUICreator {
    //public static final Engine stockfish = null;
    private static final int ENGINE_TAB_1 = 1;
    private static final int ENGINE_TAB_2 = 2;

    JSplitPane GUIRoot;

    GameView gameView = new GameView();

    JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    MoveListPanel moveList = new MoveListPanel();
    EngineOutPanel engineOut = new EngineOutPanel();

    //TEMP

    public GamePage() {
        createGUI();
        //engineOut.listenToEngine(stockfish);
        newGame("Human");
    }

    public void newGame(String string) {
        gameView.getGame().resign();
        Game game;
        if(string.equals("Human")) {
             game = new Game(
                    new HumanPlayer(gameView),
                    new HumanPlayer(gameView),
                    Clock.Format.Bullet
            );
        }
        else {
            try {
                Engine engine = Main.engineManager.getInstance(string);
                game = new Game(
                        new EnginePlayer(engine),
                        new EnginePlayer(engine),
                        Clock.Format.Bullet
                );
                engineOut.listenToEngine(engine);
            } catch (ExecutionControl.NotImplementedException e) {
                throw new RuntimeException(e);
            } catch (EngineVerificationFailure e) {
                throw new RuntimeException(e);
            }
        }
        //stockfish.playingThis(game); // not necessary, engine player should set it if needed
        //tabs.setTitleAt(ENGINE_TAB_1, stockfish.getEngineName());
        gameView.setGame(game);
        moveList.followGame(game);

        game.startGame();
    }

    @Override
    public Component createGUI() {
        GUIRoot = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                gameView.getRootComponent(),
                tabs
        );
        tabs.addTab("Moves", moveList.getRootComponent());
        tabs.addTab("Engine", engineOut.getRootComponent());
        GUIRoot.setOneTouchExpandable(true);
        return GUIRoot;
    }

    @Override
    public Component getRootComponent() {
        return GUIRoot;
    }

    public void adjustGUI() {
        GUIRoot.setDividerLocation(0.5d);
    }
}
