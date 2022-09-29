import GUI.GameView;
import engine.EngineConfig;
import engine.UCIEngine;
import game.Clock;
import game.Game;
import game.board.*;
import game.setup.Fen;
import com.formdev.flatlaf.FlatDarkLaf;
import game.setup.StartPos;
import player.HumanPlayer;
import player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Main {
    private static Game game = new Game(new StartPos());
    private static GameView gameView;
    private static Player player;

    public static void tryengine() {
        File stockfishPath = new File(System.getProperty("user.dir"));

        String[] path = { "engines", "stockfish_15_win_x64_avx2", "stockfish_15_x64_avx2.exe"};
        for(String str : path) {
            stockfishPath = new File(stockfishPath, str);
        }

        System.out.println("jeee");

        var config = new EngineConfig(
                EngineConfig.Protocol.UCI,
                stockfishPath
        );

        var engine = new UCIEngine(config);
        Thread engineThread = new Thread(engine);
        engineThread.setDaemon(true);
        engineThread.start();

        try {
            Thread.sleep(1000);
            engine.go();
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        engineThread.interrupt();
    }

    public static void main(String[] args) {
        tryengine();
        /*
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        BoardStyle.loadStyle("default");

        gameView = new GameView(game);
        player = new HumanPlayer(gameView);
        player.myTurn();

        var window = new JFrame("Lite Chess GUI");
        var layout = new GridBagLayout();
        window.setLayout(layout);
        window.setSize(new Dimension(1280, 720));

        var constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0d;
        constraints.weighty = 1.0f;
        constraints.gridwidth = 2;

        layout.setConstraints(gameView.getRootComponent(), constraints);
        window.add(gameView.getRootComponent());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New");
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game = new Game(new StartPos());
                gameView.setGame(game);
                player = new HumanPlayer(gameView);
                player.myTurn();
            }
        });
        gameMenu.add(newGame);
        menuBar.add(gameMenu);

        JMenu tournamentMenu = new JMenu("Tournament");
        menuBar.add(gameMenu);

        window.setJMenuBar(menuBar);

        //window.pack();
        window.setMinimumSize(new Dimension(640, 480));
        window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
        window.setVisible(true);*/
    }
}