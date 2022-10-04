package app;

import GUI.BoardStyle;
import GUI.GamePage;
import GUI.Page;
import audio.AudioFX;
import engine.*;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class LiteChessGUI {
    private static Page currentPage;
    private static JFrame window;
    public static final EngineManager engineManager = new EngineManager();
    public static final BoardStyle style = new BoardStyle();

    public static void main(String[] args) {
        AudioFX.innit();
        /*try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            Font dFont = (Font)UIManager.getLookAndFeelDefaults().get("defaultFont");
            //UIManager.getLookAndFeelDefaults().put("defaultFont", dFont.deriveFont(14.0f));
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }*/
        window = new JFrame("Lite Chess GUI");
        window.setSize(new Dimension(1280, 720));

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
}