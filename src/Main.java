import game.Clock;
import game.Game;
import game.board.*;
import game.setup.Fen;
import com.formdev.flatlaf.FlatDarkLaf;
import player.HumanPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        BoardStyle.loadStyle("default");

        var window = new JFrame("Lite Chess GUI");
        var layout = new GridBagLayout();
        window.setLayout(layout);
        window.setSize(new Dimension(640, 720));

        var constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0d;
        constraints.weighty = 1.0f;
        constraints.gridwidth = 2;

        Game game = new Game(new Clock.Format(70, 70));
        layout.setConstraints(game, constraints);
        game.newGame();

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New");
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.newGame();
            }
        });
        gameMenu.add(newGame);
        menuBar.add(gameMenu);

        JMenu tournamentMenu = new JMenu("Tournament");
        menuBar.add(gameMenu);

        window.setJMenuBar(menuBar);
        window.add(game);

        HumanPlayer player1 = new HumanPlayer(game);
        player1.myTurn();

        //window.pack();
        window.setMinimumSize(new Dimension(640, 480));
        window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}