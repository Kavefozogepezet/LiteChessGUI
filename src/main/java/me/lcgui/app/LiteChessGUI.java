package me.lcgui.app;

import com.formdev.flatlaf.FlatDarkLaf;
import me.lcgui.engine.Engine;
import me.lcgui.engine.EngineManager;
import me.lcgui.engine.ProtocolImplementation;
import me.lcgui.gui.BoardStyle;
import me.lcgui.gui.BoardView;
import me.lcgui.gui.LCGUIWindow;
import me.lcgui.gui.StyleLoadingException;
import me.lcgui.game.player.HumanPlayer;
import me.lcgui.gui.SelectablePlayer;
import org.reflections.Reflections;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;

public class LiteChessGUI {
    /**
     * A player package-ben {@link SelectablePlayer} annotációk gyűjteménye.
     * Az alkalmazás automatikusan összegyűjti futás elején.
     */
    public static final Set<SelectablePlayer> players = new HashSet<>();

    /**
     * Az engine package-ben {@link ProtocolImplementation} annotációval ellátott osztályok gyűjteménye.
     * Az egyes osztályok kulcsa az általuk implementált protocol.
     * Az alkalmazás automatikusan összegyűjti futás elején.
     */
    public static final Map<String, Class<? extends Engine>> protocols = new HashMap<>();

    /**
     * Az alkalmazás összes beállítását tárolja.
     * Ha egy osztály saját beállításokat szeretne hozzáadni,
     * ajánlott a beállítás nevét public static String-ként tárolni az egyszerűség kedvéért.
     */
    public static Settings settings = Settings.withDefaults(
            Settings.setting(EngineManager.ENGINE_LOG, false),
            Settings.setting(HumanPlayer.AUTO_DRAW, false),
            Settings.setting(BoardView.SHOW_COODDINATES, true),
            Settings.setting(BoardView.SHOW_POSSIBLE_MOVES, true),
            Settings.setting(BoardView.SHOW_SQUARE_INFO, true),
            Settings.setting(BoardStyle.STYLE, BoardStyle.defaultStyle)
    );

    /**
     * Az alkalmazásban installált, és az éppen futó engine-eket tárolja.
     */
    public static EngineManager engineManager = new EngineManager();

    /**
     * Az éppen betöltött stílust tárolja.
     */
    public static BoardStyle style = new BoardStyle();

    private static LCGUIWindow window;

    public static void main(String[] args) {
        initPlayers();
        initProtocols();

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            style.loadStyle(BoardStyle.defaultStyle);
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Lite Chess GUI's default look and feel is unsupported on this platform.");
        } catch (StyleLoadingException e) {
            throw new RuntimeException(e);
        }

        loadSettings();

        window = new LCGUIWindow();
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveSettings();
            }
        });
    }

    public static LCGUIWindow getWindow() {
        return window;
    }

    /**
     * Betölti a beállításokat, stílust, és az installált engine-eket.
     */
    public static void loadSettings() {
        File settingsFile = new File("settings.ser");
        if(!settingsFile.exists())
            return;

        try(
                var stream = new ObjectInputStream(
                        new FileInputStream(
                                settingsFile))
        ) {
            settings = (Settings) stream.readObject();
            engineManager = (EngineManager) stream.readObject();

            style.loadStyle(settings.get(BoardStyle.STYLE, BoardStyle.defaultStyle));
        } catch (FileNotFoundException ignored) {}
        catch (IOException | ClassNotFoundException e) {
            settingsFile.delete();
        } catch (StyleLoadingException e) {
            JOptionPane.showMessageDialog(
                    null, e.getMessage(),
                    "Failed to load style", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elmenti a beállításokat, stílust, és az installált engine-eket.
     */
    public static void saveSettings() {
        try(
                var stream = new ObjectOutputStream(
                        new FileOutputStream(
                                new File("settings.ser")))
        ) {
            stream.writeObject(settings);
            stream.writeObject(engineManager);
        } catch (Exception ignore) {}
    }

    private static void initPlayers() {
        Reflections reflections = new Reflections("me.lcgui.game.player");
        var set = reflections.getTypesAnnotatedWith(SelectablePlayer.class);
        set.forEach((c) -> players.add(c.getAnnotation(SelectablePlayer.class)));
    }

    private static void initProtocols() {
        Reflections reflections = new Reflections("me.lcgui.engine");
        var set = reflections.getSubTypesOf(Engine.class);
        for(var clazz : set) {
            ProtocolImplementation annot = clazz.getAnnotation(ProtocolImplementation.class);
            if(annot != null)
                protocols.put(annot.name(), clazz);
        }
    }
}