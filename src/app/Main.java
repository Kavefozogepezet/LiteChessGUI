package app;

import GUI.BoardStyle;
import GUI.GamePage;
import GUI.GameView;
import audio.AudioFX;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import engine.*;
import game.Clock;
import game.Game;
import com.formdev.flatlaf.FlatDarkLaf;
import jdk.jshell.spi.ExecutionControl;
import player.EnginePlayer;
import player.HumanPlayer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Main {
    private static GamePage gamePage;
    private static JFrame window;
    public static final EngineManager engineManager = new EngineManager();

    public static void main(String[] args) {
        AudioFX.innit();
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            Font dFont = (Font)UIManager.getLookAndFeelDefaults().get("defaultFont");
            //UIManager.getLookAndFeelDefaults().put("defaultFont", dFont.deriveFont(14.0f));
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        BoardStyle.loadStyle("default");

        gamePage = new GamePage();

        window = new JFrame("Lite Chess GUI");
        window.setSize(new Dimension(1280, 720));

        window.add(gamePage.getRootComponent());
        window.setJMenuBar(createMenuBar());

        window.setMinimumSize(new Dimension(640, 480));
        window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
        window.setVisible(true);

        gamePage.adjustGUI();
    }

    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Game
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New");
        newGame.addActionListener((e) -> {
            Object[] choices = engineManager.getInstalledEngines().toArray();
            Object[] withHuman = new Object[choices.length + 1];
            System.arraycopy(choices, 0, withHuman, 0, choices.length);
            withHuman[withHuman.length - 1] = "Human";
            String selected = (String)JOptionPane.showInputDialog(
                    window,
                    "Select a player",
                    "New Game",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    withHuman,
                    withHuman[0]
            );
            if(selected == null)
                return;
            gamePage.newGame(selected);
        });
        gameMenu.add(newGame);
        menuBar.add(gameMenu);

        // Tournament
        JMenu tournamentMenu = new JMenu("Tournament");
        menuBar.add(tournamentMenu);

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
                    String name = engineManager.installEngine(file, EngineConfig.Protocol.UCI);
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
        engineMenu.add(installUCI);
        menuBar.add(engineMenu);

        return menuBar;
    }
}