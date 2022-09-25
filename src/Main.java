import board.*;
import board.movegen.MoveGen;
import board.setup.Fen;
import board.setup.StartPos;
import board.types.Piece;
import board.types.Side;
import board.types.Square;
import com.formdev.flatlaf.FlatDarkLaf;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import player.HumanPlayer;
import javax.swing.UIManager.LookAndFeelInfo;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.plaf.synth.SynthLookAndFeel;
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
        window.setSize(new Dimension(1280, 720));

        var constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0d;
        constraints.weighty = 1.0f;
        constraints.gridwidth = 2;

        Board board = new Board();
        layout.setConstraints(board, constraints);
        board.newGame(new Fen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"));
/*
        JButton backB = new JButton("Back");
        constraints.gridy = 1;
        constraints.weighty = 0.0f;
        constraints.gridwidth = 1;
        layout.setConstraints(backB, constraints);
        backB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.unplay();
            }
        });

        JTextArea perftD = new JTextArea("depth");
        constraints.gridy = 2;
        constraints.gridx = 1;
        layout.setConstraints(perftD, constraints);

        JButton perftB = new JButton("Perft");
        constraints.gridx = 0;
        layout.setConstraints(perftB, constraints);
        perftB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MoveGen.Perft(Integer.parseInt(perftD.getText()), board);
            }
        });

        JTextArea fenText = new JTextArea("fen");
        constraints.gridy = 3;
        constraints.gridx = 1;
        layout.setConstraints(fenText, constraints);

        JButton fenB = new JButton("set fen");
        constraints.gridx = 0;
        layout.setConstraints(fenB, constraints);

        fenB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.newGame(new Fen(fenText.getText()));
            }
        });
*/
        window.add(board);
        /*window.add(perftB);
        window.add(backB);
        window.add(perftD);
        window.add(fenText);
        window.add(fenB);*/

        HumanPlayer player1 = new HumanPlayer(board);
        player1.myTurn();

        //window.pack();
        window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}