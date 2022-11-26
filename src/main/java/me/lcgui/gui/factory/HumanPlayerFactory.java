package me.lcgui.gui.factory;

import me.lcgui.app.LiteChessGUI;
import me.lcgui.gui.DrawableFactory;
import me.lcgui.game.player.HumanPlayer;

import javax.swing.*;
import java.awt.*;

public class HumanPlayerFactory implements DrawableFactory<HumanPlayer> {
    private final JPanel panel = new JPanel();
    private final JTextField name = new JTextField("Human", 16);

    public HumanPlayerFactory() {
        createGUI();
    }

    @Override
    public HumanPlayer instantiate() {
        HumanPlayer player = new HumanPlayer(name.getText());
        player.setMoveSupplier(LiteChessGUI.getWindow().getGameView());
        return player;
    }

    @Override
    public Component createGUI() {
        panel.removeAll();
        panel.setLayout(new GridLayout(2, 1));

        panel.add(new JLabel("Your name:"));
        panel.add(name);

        return panel;
    }

    @Override
    public Component getRootComponent() {
        return panel;
    }
}
