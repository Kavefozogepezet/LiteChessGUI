package me.lcgui.gui;

import jdk.jshell.spi.ExecutionControl;
import me.lcgui.app.LiteChessGUI;
import me.lcgui.engine.Engine;
import me.lcgui.engine.EngineConfig;
import me.lcgui.engine.EngineVerificationFailure;
import me.lcgui.game.Clock;
import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
import me.lcgui.game.setup.FEN;
import me.lcgui.game.setup.PGN;
import me.lcgui.lan.ClientThread;
import me.lcgui.misc.FileInputField;
import me.lcgui.player.EnginePlayer;
import me.lcgui.player.HumanPlayer;
import me.lcgui.player.LANPlayer;
import me.lcgui.player.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;

public class LCGUIWindow extends JFrame {
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

    public LCGUIWindow() {
        super("Lite Chess GUI");
        setSize(new Dimension(1280, 720));
        setMinimumSize(new Dimension(640, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            setIconImage(ImageIO.read(new File("lite_chess_gui_icon.png")));
        } catch (IOException ignore) {}

        createGUI();
        createMenuBar();
        Game defaultGame = getDefaultGame();
        newGame(defaultGame);

        setVisible(true);
        GUIRoot.setDividerLocation(0.5d);
        GUIRoot.setOneTouchExpandable(true);
        GUIRoot.setDividerSize(10);
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

    public GameView getGameView() {
        return gameView;
    }

    private Game getDefaultGame() {
        HumanPlayer
                p1 = new HumanPlayer(),
                p2 = new HumanPlayer();

        p1.setMoveSupplier(gameView);
        p2.setMoveSupplier(gameView);

        Game game = new Game();
        game.setPlayer(Side.White, p1);
        game.setPlayer(Side.Black, p2);

        return game;
    }

    public void createGUI() {
        GUIRoot = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                gameView.getRootComponent(),
                tabs
        );
        GUIRoot.setResizeWeight(0.5d);
        tabs.addTab("Moves", moveList.getRootComponent());
        add(GUIRoot);
    }

    public void createMenuBar() {
        // Game
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New");
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newGame.addActionListener((e) -> {
            NewGameDialog dialog = new NewGameDialog();
            if(dialog.show()) {
                Game game = null;
                try {
                    Player
                            p1 = dialog.createPlayer(Side.White),
                            p2 = dialog.createPlayer(Side.Black);
                    game = dialog.createGame();
                    game.setPlayer(Side.White, p1);
                    game.setPlayer(Side.Black, p2);
                    this.newGame(game);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            null, "Error while creating game:\n" + ex.getMessage(),
                            "New Game", JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });
        JMenuItem saveGame = new JMenuItem("Save");
        saveGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
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
        loadGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
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
        joinLAN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK));
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
        JMenuItem installEngine = new JMenuItem("Install Engine");
        installEngine.addActionListener((e) -> {
            String[] protocols = LiteChessGUI.protocols.keySet().toArray(new String[0]);

            JPanel panel = new JPanel();
            JComboBox<String> protocolOp = new JComboBox<>(protocols);
            FileInputField fileInput = new FileInputField();
            panel.setLayout(new GridLayout(4, 1));
            panel.add(new JLabel("Protocol:"));
            panel.add(protocolOp);
            panel.add(new JLabel("File:"));
            panel.add(fileInput);

            JOptionPane optionPane = new JOptionPane(
                    panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

            JDialog dialog = optionPane.createDialog("Install Engine");
            dialog.pack();
            dialog.setVisible(true);

            int selectedOp = (Integer) optionPane.getValue();
            if(selectedOp == JOptionPane.OK_OPTION) {
                File file = fileInput.getSelectedFile();
                try {
                    String protocol = (String) protocolOp.getSelectedItem();
                    String name = LiteChessGUI.engineManager.installEngine(file, protocol);
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
        JMenuItem manageEngines = new JMenuItem("Manage Engines");
        manageEngines.addActionListener((e) -> {
            if(LiteChessGUI.engineManager.getInstalledEngines().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null, "No engines installed.",
                        "Edit Config", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog(this, "Engine Config");
            dialog.add(new Button("jeee"));

            JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);
            for(var name : LiteChessGUI.engineManager.getInstalledEngines()) {
                EngineConfig config =  LiteChessGUI.engineManager.getConfig(name);
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

                JPanel options = new JPanel(new GridLayout(config.options.size(), 2));
                for(var option : config.options.values()) {
                    options.add(new JLabel(option.name));
                    options.add(option.getOptionDisplay());
                }
                options.setBorder(new TitledBorder("Options"));

                JButton uninstal = new JButton("Uninstall");
                uninstal.addActionListener((l) -> {
                    tabs.remove(panel);
                    LiteChessGUI.engineManager.uninstallEngine(name);
                    if(tabs.getTabCount() == 0)
                        dialog.dispose();
                });
                panel.add(options);
                panel.add(uninstal);
                panel.setBorder(new EmptyBorder(16, 16, 16, 16));

                tabs.addTab(name, null, panel);
            }
            dialog.add(tabs);
            dialog.pack();
            dialog.setVisible(true);
        });
        engineMenu.add(installEngine);
        engineMenu.add(manageEngines);
        menuBar.add(engineMenu);

        JMenu prefs = new JMenu("Preferences");
        JCheckBoxMenuItem logEngineCom = new JCheckBoxMenuItem("Log Engine Communication");
        logEngineCom.setState(LiteChessGUI.engineManager.isLogging());
        logEngineCom.addActionListener((l) -> {
            LiteChessGUI.engineManager.setLogging(logEngineCom.getState());
            gameView.getBoardView().createGUI();
        });
        JCheckBoxMenuItem autoDraw = new JCheckBoxMenuItem("Claim Draw Automatically");
        autoDraw.setState(LiteChessGUI.settings.autoClaimDraw);
        autoDraw.addActionListener((l) -> {
            LiteChessGUI.settings.autoClaimDraw = autoDraw.getState();
        });
        JCheckBoxMenuItem showCoords = new JCheckBoxMenuItem("Show Board Coordinates");
        showCoords.setState(LiteChessGUI.settings.showCoordinates);
        showCoords.addActionListener((l) -> {
            LiteChessGUI.settings.showCoordinates = showCoords.getState();
            gameView.getBoardView().createGUI();
        });
        JCheckBoxMenuItem showMoves = new JCheckBoxMenuItem("Show Possible Moves");
        showMoves.setState(LiteChessGUI.settings.showPossibleMoves);
        showMoves.addActionListener((l) -> {
            LiteChessGUI.settings.showPossibleMoves = showMoves.getState();
            gameView.getBoardView().getRootComponent().repaint();
        });
        JCheckBoxMenuItem showInfo = new JCheckBoxMenuItem("Show Square Info");
        showInfo.setState(LiteChessGUI.settings.showSquareInfo);
        showInfo.addActionListener((l) -> {
            LiteChessGUI.settings.showSquareInfo = showInfo.getState();
            gameView.getBoardView().getRootComponent().repaint();
        });
        JMenu styles = new JMenu("Select Style");
        styles.add(new JMenuItem("Default"));
        prefs.add(logEngineCom);
        prefs.add(autoDraw);
        prefs.add(new JSeparator(JSeparator.HORIZONTAL));
        prefs.add(showCoords);
        prefs.add(styles);
        menuBar.add(prefs);

        setJMenuBar(menuBar);
    }

    private class GameJoiner implements ActionListener {
        private final Game game = null;

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("CLIENT");

            JPanel panel = new JPanel();
            JTextField name = new JTextField("Human", 16);
            JTextField password = new JTextField("default", 16);

            panel.setLayout(new GridLayout(4, 1));
            panel.add(new JLabel("Your Name:"));
            panel.add(name);
            panel.add(new JLabel("Password:"));
            panel.add(password);

            int reply = JOptionPane.showConfirmDialog(
                    (Component) null, panel, "Joining",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if(reply != JOptionPane.OK_OPTION || "".equals(name.getText()))
                return;

            JOptionPane pane = new JOptionPane(
                    "Trying to connect to a game on your local network.",
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new String[] { "Cancel" }, "Cancel");
            JDialog dialog = pane.createDialog("Joining");

            ClientThread thread = new ClientThread(password.getText()); // TODO password
            thread.setConnectionCallback((s) -> {
                SwingUtilities.invokeLater(dialog::dispose);
            });

            LANPlayer player = new LANPlayer(thread, false);
            thread.start();

            dialog.pack();
            dialog.setVisible(true);

            thread.setConnectionCallback(null);
            dialog.dispose();

            if(!thread.isConnected()) {
                thread.interrupt();
                JOptionPane.showMessageDialog(null, "Failed to connect.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                HumanPlayer human = new HumanPlayer(name.getText());
                human.setMoveSupplier(gameView);
                player.initGame(human);
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
