package me.lcgui.app;

import com.formdev.flatlaf.FlatDarkLaf;
import me.lcgui.audio.AudioFX;
import me.lcgui.engine.Engine;
import me.lcgui.engine.EngineManager;
import me.lcgui.engine.ProtocolImplementation;
import me.lcgui.gui.BoardStyle;
import me.lcgui.gui.LCGUIWindow;
import me.lcgui.player.SelectablePlayer;
import org.reflections.Reflections;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LiteChessGUI {
    public static final Set<SelectablePlayer> players = new HashSet<>();

    public static final Map<String, Class<? extends Engine>> protocols = new HashMap<>();

    public static Settings settings = new Settings();
    public static EngineManager engineManager = new EngineManager();
    public static BoardStyle style = new BoardStyle();

    private static LCGUIWindow window;

    public static void main(String[] args) {
        AudioFX.innit();
        initPlayers();
        initProtocols();

        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
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

    private static void loadSettings() {
        try(
                var stream = new ObjectInputStream(
                        new FileInputStream(
                                new File("settings.ser")))
        ) {
            settings = (Settings) stream.readObject();
            engineManager = (EngineManager) stream.readObject();

            style.loadStyle(settings.styleName);
        } catch (Exception ignore) {}
    }

    private static void saveSettings() {
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
        Reflections reflections = new Reflections("me.lcgui.player");
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