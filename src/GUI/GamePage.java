package GUI;

import app.LiteChessGUI;
import engine.*;
import game.Clock;
import game.Game;
import game.board.Side;
import jdk.jshell.spi.ExecutionControl;
import player.EnginePlayer;
import player.HumanPlayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.Stack;

public class GamePage implements Page {
    //public static final Engine stockfish = null;
    private static final int ENGINE_TAB_1 = 1;
    private static final int ENGINE_TAB_2 = 2;

    JSplitPane GUIRoot;
    JMenuBar menuBar = new JMenuBar();

    GameView gameView = new GameView();

    JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    MoveListPanel moveList = new MoveListPanel();
    EngineOutPanel engineOut = new EngineOutPanel();

    //TEMP

    public GamePage() {
        createGUI();
        createMenuBar();
        //engineOut.listenToEngine(stockfish);
        newGame(new Game(new HumanPlayer(), new HumanPlayer()));
    }

    public void newGame(Game game) {
        gameView.getGame().resign();
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
        return GUIRoot;
    }

    @Override
    public Component getRootComponent() {
        return GUIRoot;
    }

    @Override
    public void adjustGUI() {
        GUIRoot.setDividerLocation(0.5d);
    }

    @Override
    public JMenuBar createMenuBar() {
        /*JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);*/

        // Game
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New");
        newGame.addActionListener((e) -> {
            Object[] choices = LiteChessGUI.engineManager.getInstalledEngines().toArray();
            Object[] withHuman = new Object[choices.length + 1];
            System.arraycopy(choices, 0, withHuman, 0, choices.length);
            withHuman[withHuman.length - 1] = "Human";
            String selected = (String)JOptionPane.showInputDialog(
                    LiteChessGUI.getWindow(),
                    "Select a player",
                    "New Game",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    withHuman,
                    withHuman[0]
            );
            if(selected == null)
                return;
            Game game = null;
            if(selected.equals("Human")) {
                game = new Game(
                        new HumanPlayer(),
                        new HumanPlayer(),
                        Clock.Format.Bullet
                );
            }
            else {
                try {
                    game = new Game(
                            new EnginePlayer(selected),
                            new EnginePlayer(selected),
                            Clock.Format.Bullet
                    );
                    if(game.getPlayer(Side.White) instanceof EnginePlayer ep)
                        engineOut.listenToEngine(ep.getEngine());
                } catch (ExecutionControl.NotImplementedException | EngineVerificationFailure ignored) {
                    return;
                }
            }
            this.newGame(game);
        });
        JMenuItem saveGame = new JMenuItem("Save");
        saveGame.addActionListener((l) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(
                    new FileNameExtensionFilter("Lite Chess Game", "lcg")
            );
            int temp = fileChooser.showSaveDialog(null);
            if(temp == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (
                        var fileOut = new ObjectOutputStream(new FileOutputStream(file))
                ){
                    fileOut.writeObject(gameView.getGame());
                    JOptionPane.showMessageDialog(
                            null, "Game saved successfully.",
                            "Game Save", JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            null, "Failed to save game.\n" + ex.getMessage(),
                            "Game Save", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        JMenuItem loadGame = new JMenuItem("Load");
        loadGame.addActionListener((l) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(
                    new FileNameExtensionFilter("Lite Chess Game", "lcg")
            );
            int temp = fileChooser.showOpenDialog(null);
            if(temp == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (
                        var fileIn = new ObjectInputStream(new FileInputStream(file))
                ){
                    Game game = (Game) fileIn.readObject();
                    newGame(game);
                    JOptionPane.showMessageDialog(
                            null, "Game Load",
                            "Game loaded successfully.", JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            null, "Game Load",
                            "Failed to save game.", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        gameMenu.add(newGame);
        gameMenu.add(saveGame);
        gameMenu.add(loadGame);
        menuBar.add(gameMenu);

        // Engine
        JMenu engineMenu = new JMenu("Engines");
        JMenuItem installUCI = new JMenuItem("Install UCI Engine");
        installUCI.addActionListener((e) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(
                    new FileNameExtensionFilter("Executable", "exe")
            );
            int temp = fileChooser.showOpenDialog(null);

            if(temp == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    String name = LiteChessGUI.engineManager.installEngine(file, EngineConfig.Protocol.UCI);
                    JOptionPane.showMessageDialog(
                            null, name + " is successfully installed.",
                            "Installation successful", JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            null, ex.getMessage(),
                            "Installation failed", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        JMenuItem editConfigs = new JMenuItem("Edit engine configurations");
        editConfigs.addActionListener((e) -> {
            if(LiteChessGUI.engineManager.getInstalledEngines().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null, "No engines installed.",
                        "Edit Config", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog(LiteChessGUI.getWindow(), "Engine Config");
            dialog.add(new Button("jeee"));

            JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);
            for(var name : LiteChessGUI.engineManager.getInstalledEngines()) {
                EngineConfig config =  LiteChessGUI.engineManager.getConfig(name);
                JPanel panel = new JPanel(new GridLayout(config.options.size(), 2));
                for(var option : config.options.values()) {
                    panel.add(new JLabel(option.name));
                    panel.add(option.getOptionDisplay());
                }
                panel.setBorder(new EmptyBorder(32, 32, 32, 32));
                tabs.addTab(name, null, panel);
            }
            dialog.add(tabs);
            dialog.pack();
            dialog.setVisible(true);
        });
        engineMenu.add(installUCI);
        engineMenu.add(editConfigs);
        menuBar.add(engineMenu);

        return menuBar;
    }

    @Override
    public JMenuBar getMenuBar() {
        return menuBar;
    }
}
