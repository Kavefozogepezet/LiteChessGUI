package me.lcgui.gui.factory;

import me.lcgui.gui.DrawableFactory;
import me.lcgui.gui.FactoryException;
import me.lcgui.lan.ServerThread;
import me.lcgui.game.player.LANPlayer;

import javax.swing.*;
import java.awt.*;

public class LANPlayerFactory implements DrawableFactory<LANPlayer> {
    private final JPanel panel = new JPanel();
    private final JTextField password = new JTextField("default", 16);

    public LANPlayerFactory() {
        createGUI();
    }

    @Override
    public LANPlayer instantiate() throws FactoryException {
        JOptionPane pane = new JOptionPane(
                "Waiting for a player to connect...",
                JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new String[] { "Cancel"}, "Cancel" );

        JDialog dialog = pane.createDialog("Creating game");

        ServerThread thread = new ServerThread(password.getText());
        thread.setConnectionCallback((s) -> {
            SwingUtilities.invokeLater(dialog::dispose);
        });

        LANPlayer player = new LANPlayer(thread, true);
        thread.start();

        dialog.pack();
        dialog.setVisible(true);

        thread.setConnectionCallback(null);
        dialog.dispose();

        String input = (String)pane.getInputValue();
        if(JOptionPane.UNINITIALIZED_VALUE.equals(input)) {
            if(!thread.isConnected()) {
                thread.interrupt();
                throw new FactoryException("Failed to connect to a client.");
            }
        } else {
            throw new FactoryException("Connection cancelled.");
        }
        return player;
    }

    @Override
    public Component createGUI() {
        panel.removeAll();
        panel.setLayout(new GridLayout(2, 1));

        panel.add(new JLabel("Password:"));
        panel.add(password);

        return panel;
    }

    @Override
    public Component getRootComponent() {
        return panel;
    }
}
