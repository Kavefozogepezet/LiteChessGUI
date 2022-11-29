package me.lcgui.gui;

import me.lcgui.app.LiteChessGUI;
import me.lcgui.engine.*;
import me.lcgui.game.Game;
import me.lcgui.game.IncorrectNotationException;
import me.lcgui.game.board.Side;
import me.lcgui.game.player.HumanPlayer;
import me.lcgui.game.player.LANPlayer;
import me.lcgui.game.player.Player;
import me.lcgui.game.setup.FEN;
import me.lcgui.game.setup.PGN;
import me.lcgui.lan.ClientThread;
import me.lcgui.misc.FileInputField;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

public class LCGUIWindow extends JFrame {
    private static final BufferedImage icon;

    static {
        BufferedImage tempIcon = null;
        try {
            tempIcon = ImageIO.read(new File("lite_chess_gui_icon.png"));
        } catch (IOException ignored) {}
        icon = tempIcon;
    }

    public static Dimension defaultSize = new Dimension(960, 720);
    public static Dimension minimumSize = new Dimension(640, 480);

    private static final int MOVES_TAB = 0;
    private static final int ENGINE_TAB_1 = 1;
    private static final int ENGINE_TAB_2 = 2;

    private JSplitPane GUIRoot;
    private final JMenuBar menuBar = new JMenuBar();

    private final GameView gameView = new GameView();

    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    private final MoveListPanel moveList = new MoveListPanel();

    private JFrame docFrame = null;

    public LCGUIWindow() {
        super("Lite Chess GUI");
        setSize(defaultSize);
        setMinimumSize(minimumSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(icon);

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

        for(var engineName : LiteChessGUI.engineManager.getRunningEngines()) {
            try {
                Engine engine = LiteChessGUI.engineManager.getInstance(engineName);
                EngineOutPanel enginePanel = new EngineOutPanel();
                enginePanel.listenToEngine(engine);
                tabs.add(engine.getEngineName(), enginePanel.getRootComponent());
                engine.release();
            } catch (Exception ignored) {}
        }

        if(!game.hasEnded())
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

    private void createMenuBar() {
        menuBar.add(createGameMenu());
        menuBar.add(createEngineMenu());
        menuBar.add(createPreferencesMenu());
        menuBar.add(createHelpMenu());

        setJMenuBar(menuBar);
    }

    private JMenu createGameMenu() {
        JMenu gameMenu = new JMenu("Game");

        JMenuItem newGame = new JMenuItem("New");
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newGame.addActionListener(this::onNewGame);

        JMenuItem saveGame = new JMenuItem("Save");
        saveGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveGame.addActionListener(this::onSaveGame);

        JMenuItem loadGame = new JMenuItem("Load");
        loadGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        loadGame.addActionListener(this::onLoadGame);

        JMenuItem joinLAN = new JMenuItem("Join LAN game");
        joinLAN.addActionListener(this::onJoinLan);
        joinLAN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK));

        JMenuItem importPGN = new JMenuItem("Import PGN");
        importPGN.addActionListener(this::onImportPGN);

        JMenuItem copyPGN = new JMenuItem("Copy PGN");
        copyPGN.addActionListener(this::onCopyPGN);

        JMenuItem copyFEN = new JMenuItem("Copy FEN");
        copyFEN.addActionListener(this::onCopyFEN);

        gameMenu.add(newGame);
        gameMenu.add(saveGame);
        gameMenu.add(loadGame);
        gameMenu.add(new JSeparator());
        gameMenu.add(joinLAN);
        gameMenu.add(new JSeparator());
        gameMenu.add(importPGN);
        gameMenu.add(copyPGN);
        gameMenu.add(copyFEN);
        return gameMenu;
    }

    private void onNewGame(ActionEvent e) {
        GameSettingsDialog dialog = new GameSettingsDialog();
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
                        "New Game", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void onSaveGame(ActionEvent e) {
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
                Game game = gameView.getGame();
                fileOut.writeObject(game);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null, "Failed to save game.\n" + ex.getMessage(),
                        "Game Save", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void onLoadGame(ActionEvent e) {
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
                var dialog = new GameSettingsDialog(game);
                dialog.show();
                game.setPlayer(Side.White, dialog.createPlayer(Side.White));
                game.setPlayer(Side.Black, dialog.createPlayer(Side.Black));
                newGame(game);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null, "Failed to load game.\n" + ex.getMessage(),
                        "Game Load", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onJoinLan(ActionEvent r) {
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

        ClientThread thread = new ClientThread(password.getText());
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

    private void onImportPGN(ActionEvent e) {
        try {
            String pgnStr = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
            PGN pgn = new PGN(pgnStr);
            Game game = new Game(pgn);
            newGame(game);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    null, "Cannot access clipboard",
                    "Import PGN", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(ex);
        } catch (IncorrectNotationException ex) {
            JOptionPane.showMessageDialog(
                    null, "Failed to import PGN:\n" + ex.getMessage(),
                    "Import PGN", JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedFlavorException ex) {
            JOptionPane.showMessageDialog(
                    null, "Clipboard did not contain a string",
                    "Import PGN", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCopyPGN(ActionEvent e) {
        PGN pgn = new PGN(gameView.getGame());
        var pgnStr = new StringSelection(pgn.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(pgnStr, pgnStr);
    }

    private void onCopyFEN(ActionEvent e) {
        FEN fen = new FEN(gameView.getGame());
        var fenStr = new StringSelection(fen.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(fenStr, fenStr);
    }

    private JMenu createEngineMenu() {
        JMenu engineMenu = new JMenu("Engines");
        JMenuItem installEngine = new JMenuItem("Install Engine");
        installEngine.addActionListener(this::onInstallEngine);

        JMenuItem manageEngines = new JMenuItem("Manage Engines");
        manageEngines.addActionListener(this::onManageEngines);

        engineMenu.add(installEngine);
        engineMenu.add(manageEngines);
        return engineMenu;
    }

    private void onInstallEngine(ActionEvent e) {
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

        boolean ok =
                optionPane.getValue() != null
                && (Integer) optionPane.getValue() == JOptionPane.OK_OPTION;

        if(ok) {
            File file = fileInput.getSelectedFile();
            try {
                String protocol = (String) protocolOp.getSelectedItem();
                String name = LiteChessGUI.engineManager.installEngine(file, protocol);
                JOptionPane.showMessageDialog(
                        null, name + " is successfully installed.",
                        "Installation successful", JOptionPane.INFORMATION_MESSAGE
                );
            } catch (EngineVerificationFailure ex) {
                JOptionPane.showMessageDialog(
                        null, ex.getMessage(),
                        "Installation failed", JOptionPane.ERROR_MESSAGE
                );
            } catch (EngineAlreadyInstalledException ex) {
                JOptionPane.showMessageDialog(
                        null, ex.getEngineName() + " is already installed",
                        "Installation cancelled", JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    private void onManageEngines(ActionEvent e) {
        if(LiteChessGUI.engineManager.getInstalledEngines().isEmpty()) {
            JOptionPane.showMessageDialog(
                    null, "No engines installed.",
                    "Edit Config", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Engine Config");
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);

        for(var name : LiteChessGUI.engineManager.getInstalledEngines()) {
            EngineConfig config =  LiteChessGUI.engineManager.getConfig(name);
            JPanel panel = new JPanel();
            var layout = new GridBagLayout();
            var constraints = new GridBagConstraints();
            panel.setLayout(layout);

            JPanel options = new JPanel(new GridLayout(config.options.size(), 3));
            for(var option : config.options.values()) {
                options.add(new JLabel(option.getName()));
                options.add(new JLabel(option.getType()));
                var provider = new ArgComponentProvider(option);
                options.add(provider.getRootComponent());
            }
            options.setBorder(new TitledBorder("Options"));
            options.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JScrollPane scroll = new JScrollPane(options);

            JPanel buttons = new JPanel();
            buttons.setLayout(new GridLayout(1, 2));
            buttons.setAlignmentX(JComponent.CENTER_ALIGNMENT);

            JButton reset = new JButton("Reset");
            reset.addActionListener(l -> {
                for(var op : config.options.values())
                    op.reset();
            });
            buttons.add(reset);

            JButton uninstal = new JButton("Uninstall");
            uninstal.addActionListener(l -> {
                tabs.remove(panel);
                LiteChessGUI.engineManager.uninstallEngine(name);
                if(tabs.getTabCount() == 0)
                    dialog.dispose();
            });
            buttons.add(uninstal);

            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1.0d;
            constraints.weighty = 1.0d;
            layout.setConstraints(scroll, constraints);
            panel.add(scroll);

            constraints.weighty = 0.0d;
            constraints.gridy = 1;
            layout.setConstraints(buttons, constraints);
            panel.add(buttons);

            panel.setBorder(new EmptyBorder(16, 16, 16, 16));

            tabs.addTab(name, null, panel);
        }

        dialog.add(tabs);
        dialog.pack();
        dialog.setVisible(true);
    }

    private JMenu createPreferencesMenu() {
        JMenu prefs = new JMenu("Preferences");

        JCheckBoxMenuItem engineLog = new JCheckBoxMenuItem("Log Engine Communication");
        engineLog.setState(LiteChessGUI.settings.get(EngineManager.ENGINE_LOG, false));
        engineLog.addActionListener((l) -> {
            LiteChessGUI.settings.set(EngineManager.ENGINE_LOG, engineLog.getState());
            gameView.getBoardView().createGUI();
        });

        JCheckBoxMenuItem autoDraw = new JCheckBoxMenuItem("Claim Draw Automatically");
        autoDraw.setState(LiteChessGUI.settings.get(HumanPlayer.AUTO_DRAW, false));
        autoDraw.addActionListener((l) -> {
            LiteChessGUI.settings.set(HumanPlayer.AUTO_DRAW, autoDraw.getState());
        });

        JCheckBoxMenuItem showCoords = new JCheckBoxMenuItem("Show Board Coordinates");
        showCoords.setState(LiteChessGUI.settings.get(BoardView.SHOW_COODDINATES, true));
        showCoords.addActionListener((l) -> {
            LiteChessGUI.settings.set(BoardView.SHOW_COODDINATES, showCoords.getState());
            gameView.getBoardView().createGUI();
        });

        JCheckBoxMenuItem showMoves = new JCheckBoxMenuItem("Show Possible Moves");
        showMoves.setState(LiteChessGUI.settings.get(BoardView.SHOW_POSSIBLE_MOVES, true));
        showMoves.addActionListener((l) -> {
            LiteChessGUI.settings.set(BoardView.SHOW_POSSIBLE_MOVES, showMoves.getState());
            gameView.getBoardView().updateStyle();
        });

        JCheckBoxMenuItem showInfo = new JCheckBoxMenuItem("Show Square Info");
        showInfo.setState(LiteChessGUI.settings.get(BoardView.SHOW_SQUARE_INFO, true));
        showInfo.addActionListener((l) -> {
            LiteChessGUI.settings.set(BoardView.SHOW_SQUARE_INFO, showInfo.getState());
            gameView.getBoardView().updateStyle();
        });

        JMenu styles = new JMenu("Select Style");
        try {
            for(String styleName : BoardStyle.getAvailableStyles()) {
                var item = new JMenuItem(styleName);
                item.addActionListener(new StyleLoader(styleName));
                styles.add(item);
            }
        } catch (StyleLoadingException ignore) {}

        prefs.add(engineLog);
        prefs.add(autoDraw);
        prefs.add(new JSeparator(JSeparator.HORIZONTAL));
        prefs.add(showCoords);
        prefs.add(showMoves);
        prefs.add(showInfo);
        prefs.add(styles);
        return prefs;
    }

    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");

        JMenuItem help = new JMenuItem("Help");
        help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        help.addActionListener(this::onHelp);

        JMenuItem laws = new JMenuItem("Laws of Chess");
        laws.addActionListener(this::onLawsOfChess);

        helpMenu.add(help);
        helpMenu.add(laws);
        return helpMenu;
    }

    private void onHelp(ActionEvent e) {
        if(docFrame != null) {
            docFrame.toFront();
            docFrame.requestFocus();
            return;
        }

        docFrame = new JFrame("Lite Chess GUI Documentation");
        docFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                docFrame = null;
            }
        });

        JEditorPane docPane = new JEditorPane();
        Consumer<URL> setPage = (URL url) -> {
            try {
                if(url.toString().startsWith("file:"))
                    docPane.setPage(url);
                else {
                    try {
                        Desktop.getDesktop().browse(url.toURI());
                    } catch (URISyntaxException ignored) {}
                }
            } catch (IOException ex) {
                docPane.setText("ERROR." + ex.getMessage());
            }
        };

        docPane.setEditable(false);
        docPane.setContentType("text/html");
        docPane.addHyperlinkListener(link -> {
            if(link.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                setPage.accept(link.getURL());
        });
        docPane.setBorder(new EmptyBorder(32, 64, 32, 64));

        File indexFile = new File("docs", "index.html");
        try {
            setPage.accept(indexFile.toURI().toURL());
        } catch (MalformedURLException ex) {
            docPane.setText("ERROR." + ex.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(docPane);
        scrollPane.setPreferredSize(new Dimension(640, 480));

        docFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                var size = docFrame.getSize();
                int delta = (size.width - 480) / 2;
                int margin = delta * 75 / 100;
                docPane.setBorder(new EmptyBorder(32, margin, 32, margin));
            }
        });

        docFrame.add(scrollPane);
        docFrame.setMinimumSize(minimumSize);
        docFrame.setIconImage(icon);
        docFrame.pack();
        docFrame.setVisible(true);
    }

    private void onLawsOfChess(ActionEvent e) {
        Desktop d = Desktop.getDesktop();
        URI FIDEpage = null;
        try {
            FIDEpage = new URI("https://www.fide.com/FIDE/handbook/LawsOfChess.pdf");
            d.browse(FIDEpage);
        } catch (URISyntaxException | IOException ignored) {}
    }

    private class StyleLoader implements ActionListener{
        private final String styleName;

        public StyleLoader(String styleName) {
            this.styleName = styleName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BoardStyle newStyle = new BoardStyle();
            try {
                newStyle.loadStyle(styleName);
                LiteChessGUI.style = newStyle;
                gameView.getBoardView().updateStyle();
                LiteChessGUI.settings.set(BoardStyle.STYLE, styleName);
            } catch (StyleLoadingException ex) {
                JOptionPane.showMessageDialog(
                        null, ex.getMessage(),
                        "Failed to load Style", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
