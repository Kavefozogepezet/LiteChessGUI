package GUI;

import app.LiteChessGUI;
import engine.*;
import game.Clock;
import game.Game;
import game.board.Side;
import game.setup.FEN;
import game.setup.PGN;
import jdk.jshell.spi.ExecutionControl;
import lan.ClientThread;
import lan.ServerThread;
import player.EnginePlayer;
import player.HumanPlayer;
import player.LANPlayer;
import player.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class GamePage implements Page {
    private static final int MOVES_TAB = 0;
    private static final int ENGINE_TAB_1 = 1;
    private static final int ENGINE_TAB_2 = 2;

    private JSplitPane GUIRoot;
    private final JMenuBar menuBar = new JMenuBar();

    private final GameView gameView = new GameView();

    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    private final MoveListPanel moveList = new MoveListPanel();
    private final EngineOutPanel[] engineOutputs = {
            new EngineOutPanel(), new EngineOutPanel()
    };

    //TEMP

    public GamePage() {
        createGUI();
        createMenuBar();
        Game defaultGame = new Game();
        defaultGame.setPlayer(Side.White, new HumanPlayer());
        defaultGame.setPlayer(Side.Black, new HumanPlayer());
        newGame(defaultGame);
    }

    public void newGame(Game game) {
        gameView.setGame(game);
        moveList.followGame(game);

        for(int tabIdx = tabs.getTabCount() - 1; tabIdx > MOVES_TAB; tabIdx-- )
            tabs.remove(tabIdx);

        Engine engine1 = null;
        if(game.getPlayer(Side.White) instanceof EnginePlayer ep) {
            var engineOut = engineOutputs[Side.White.ordinal()];
            engine1 = ep.getEngine();
            engineOut.listenToEngine(engine1);
            tabs.add(engine1.getEngineName(), engineOut.getRootComponent());
        }
        if(game.getPlayer(Side.Black) instanceof EnginePlayer ep) {
            var engineOut = engineOutputs[Side.Black.ordinal()];
            if(ep.getEngine() != engine1) {
                engineOut.listenToEngine(ep.getEngine());
                tabs.add(ep.getEngine().getEngineName(), engineOut.getRootComponent());
            }
        }

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
        // Game
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New");
        newGame.addActionListener((e) -> {
            NewGameDialog dialog = new NewGameDialog();
            if(dialog.show() == JOptionPane.OK_OPTION) {
                Game game = null;
                try {
                    Player
                            p1 = dialog.createPlayer(Side.White),
                            p2 = dialog.createPlayer(Side.Black);
                    Clock.Format timeControl = dialog.getTimeControl();
                    game = dialog.useTimeControl()
                            ? new Game(timeControl)
                            : new Game();
                    game.setPlayer(Side.White, p1);
                    game.setPlayer(Side.Black, p2);
                    this.newGame(game);
                } catch (ExecutionControl.NotImplementedException | EngineVerificationFailure ex) {
                    JOptionPane.showMessageDialog(
                            null, "Error while creating game:\n" + ex.getMessage(),
                            "New Game", JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });
        JMenuItem saveGame = new JMenuItem("Save");
        saveGame.addActionListener((l) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(
                    new FileNameExtensionFilter("Lite Chess Game", "lcg")
            );
            int temp = fileChooser.showSaveDialog(null);
            if(temp == JFileChooser.APPROVE_OPTION) {
                String msg;
                File file = fileChooser.getSelectedFile();
                try (
                        var fileOut = new ObjectOutputStream(new FileOutputStream(file))
                ){
                    Game game = gameView.getGame();
                    fileOut.writeObject(game);
                    fileOut.writeObject(game.getPlayer(Side.White));
                    fileOut.writeObject(game.getPlayer(Side.Black));
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
                String msg = "Game loaded successfully.";
                int msgtype = JOptionPane.INFORMATION_MESSAGE;
                try (
                        var fileIn = new ObjectInputStream(new FileInputStream(file))
                ){
                    Game game = (Game) fileIn.readObject();
                    Player
                            white = (Player) fileIn.readObject(),
                            black = (Player) fileIn.readObject();
                    game.setPlayer(Side.White, white);
                    game.setPlayer(Side.Black, black);
                    newGame(game);
                } catch (Exception ex) {
                    msg = "Failed to load game.\n" + ex.getMessage();
                    msgtype = JOptionPane.ERROR_MESSAGE;
                }
                JOptionPane.showMessageDialog(null, msg, "Game Load", msgtype);
            }
        });
        JMenuItem joinLAN = new JMenuItem("Join LAN game");
        joinLAN.addActionListener(new GameJoiner());
        JMenuItem copyPGN = new JMenuItem("Copy PGN");
        copyPGN.addActionListener((l) -> {
            PGN pgn = new PGN(gameView.getGame());
            var pgnStr = new StringSelection(pgn.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(pgnStr, pgnStr);
        });
        JMenuItem copyFEN = new JMenuItem("Copy FEN");
        copyFEN.addActionListener((l) -> {
            FEN fen = new FEN(gameView.getGame());
            var fenStr = new StringSelection(fen.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(fenStr, fenStr);
        });
        gameMenu.add(newGame);
        gameMenu.add(saveGame);
        gameMenu.add(loadGame);
        gameMenu.add(new JSeparator());
        gameMenu.add(joinLAN);
        gameMenu.add(new JSeparator());
        gameMenu.add(copyPGN);
        gameMenu.add(copyFEN);
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

    private class GameJoiner implements ActionListener {
        private Game game = null;
        private LANPlayer player = null;

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("CLIENT");

            String reply = JOptionPane.showInputDialog((Component) null, (Object)"Your name:", "Joining", JOptionPane.PLAIN_MESSAGE);
            if(reply == null || "".equals(reply))
                return;

            JOptionPane pane = new JOptionPane(
                    "Trying to connect to a game on your local network.",
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new String[] { "Cancel" }, "Cancel");
            JDialog dialog = pane.createDialog("Joining");

            ClientThread thread = new ClientThread(""); // TODO password
            thread.setConnectionCallback((s) -> {
                SwingUtilities.invokeLater(dialog::dispose);
            });

            player = new LANPlayer(thread, false);
            thread.start();

            dialog.add(pane);
            dialog.pack();
            dialog.show();

            thread.setConnectionCallback(null);
            dialog.dispose();

            if(!thread.isConnected()) {
                thread.interrupt();
                JOptionPane.showMessageDialog(null, "Failed to connect.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                player.initGame(reply);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Failed to communicate.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            Game game = player.getGame();
            gameView.setGame(game);
            game.startGame();
        }
    }
}
