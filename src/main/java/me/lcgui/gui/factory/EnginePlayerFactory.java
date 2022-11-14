package me.lcgui.gui.factory;

import jdk.jshell.spi.ExecutionControl;
import me.lcgui.app.LiteChessGUI;
import me.lcgui.engine.Engine;
import me.lcgui.engine.EngineVerificationFailure;
import me.lcgui.gui.DrawableFactory;
import me.lcgui.player.EnginePlayer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class EnginePlayerFactory implements DrawableFactory<EnginePlayer> {
    JPanel panel = new JPanel();
    private JComboBox<String> engineOptions;

    public EnginePlayerFactory() {
        createGUI();
    }

    @Override
    public EnginePlayer instantiate() throws EngineVerificationFailure {
        String engineName = (String) engineOptions.getSelectedItem();
        Engine engine = LiteChessGUI.engineManager.getInstance(engineName);
        return new EnginePlayer(engine);
    }

    @Override
    public Component createGUI() {
        panel.removeAll();
        panel.setLayout(new GridLayout(2, 1));

        String[] engineNames = LiteChessGUI.engineManager.getInstalledEngines().toArray(new String[0]);
        engineOptions = new JComboBox<>(engineNames);

        panel.add(new JLabel("Select an engine:"));
        panel.add(engineOptions);

        return panel;
    }

    @Override
    public Component getRootComponent() {
        return panel;
    }
}
