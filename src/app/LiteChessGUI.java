package app;

import GUI.BoardStyle;
import GUI.GamePage;
import GUI.Page;
import audio.AudioFX;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatPropertiesLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import engine.*;
import com.formdev.flatlaf.FlatDarkLaf;
import game.Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.*;

public class LiteChessGUI {
    private static Page currentPage;
    private static JFrame window;
    public static EngineManager engineManager = new EngineManager();
    public static BoardStyle style = new BoardStyle();

    public static void main(String[] args) {
        AudioFX.innit();
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            Font dFont = (Font)UIManager.getLookAndFeelDefaults().get("defaultFont");
            //UIManager.getLookAndFeelDefaults().put("defaultFont", dFont.deriveFont(14.0f));
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        loadSettings();

        window = new JFrame("Lite Chess GUI");
        window.setSize(new Dimension(1280, 720));
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                saveSettings();
            }
        });

        try {
            window.setIconImage(ImageIO.read(new File("lite_chess_gui.png")));
        } catch (IOException ignore) {}

        loadPage(new GamePage());

        window.setMinimumSize(new Dimension(640, 480));
        window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
        window.setVisible(true);

        currentPage.adjustGUI();
    }

    public static void loadPage(Page page) {
        if(currentPage != null)
            window.remove(currentPage.getRootComponent());
        window.add(page.getRootComponent());
        window.setJMenuBar(page.getMenuBar());
        currentPage = page;
    }

    public static JFrame getWindow() {
        return window;
    }

    public static Page getCurrentPage() {
        return currentPage;
    }

    private static void loadSettings() {
        try(
                var stream = new ObjectInputStream(
                        new FileInputStream(
                                new File("settings.ser")))
        ) {
            engineManager = (EngineManager) stream.readObject();
            Game game = (Game) stream.readObject();
        } catch (Exception ignore) {}
    }

    private static void saveSettings() {
        try(
                var stream = new ObjectOutputStream(
                        new FileOutputStream(
                                new File("settings.ser")))
        ) {
            stream.writeObject(engineManager);
        } catch (Exception ignore) {}
    }
}