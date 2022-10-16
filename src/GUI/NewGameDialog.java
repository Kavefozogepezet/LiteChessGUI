package GUI;

import app.LiteChessGUI;
import engine.EngineVerificationFailure;
import game.Clock;
import game.board.Side;
import jdk.jshell.spi.ExecutionControl;
import lan.ServerThread;
import player.EnginePlayer;
import player.HumanPlayer;
import player.LANPlayer;
import player.Player;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.NumberFormat;

public class NewGameDialog {
    private static final Clock.Format customTimeControl = new Clock.Format(0, "custom");
    private static final Clock.Format noTimeControl = new Clock.Format(0, "No time control");
    
    private final JPanel panel;

    private final JComboBox<String>[] players = new JComboBox[2];

    private final JComboBox<Clock.Format> timeFormat = new JComboBox<>(new Clock.Format[] {
            noTimeControl,
            Clock.Format.Bullet, Clock.Format.Blitz, Clock.Format.Classical,
            Clock.Format.FIDE_Blitz, Clock.Format.FIDE_Rapid,
            customTimeControl
    });
    private final JPanel timePanel = new JPanel();
    private JFormattedTextField[] timeFields = new JFormattedTextField[4];

    public NewGameDialog() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        String[] options = getPlayerOptions();
        addPlayerOptions(Side.White, options);
        addPlayerOptions(Side.Black, options);

        panel.add(new JLabel("Time control"));
        panel.add(timeFormat);

        String[] timeNames = { "White time", "increment", "Black time", "increment" };
        timePanel.setLayout(new GridLayout(4, 2));
        for(int i = 0; i < 2; i++) {
            int idx = i * 2;
            timeFields[idx] = new JFormattedTextField(NumberFormat.getNumberInstance());
            timeFields[idx + 1] = new JFormattedTextField(NumberFormat.getNumberInstance());
            timePanel.add(new JLabel(timeNames[idx]));
            timePanel.add(new JLabel(timeNames[idx + 1]));
            timePanel.add(timeFields[idx]);
            timePanel.add(timeFields[idx + 1]);
        }
        //panel.add(timePanel);
    }

    public int show() {
        return JOptionPane.showConfirmDialog(
                null, panel, "New Game",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    public Player createPlayer(Side side) throws
            ExecutionControl.NotImplementedException,
            EngineVerificationFailure
    {
        String selected = (String)players[side.ordinal()].getSelectedItem();
        if(LiteChessGUI.engineManager.getInstalledEngines().contains(selected))
            return new EnginePlayer(selected);
        else if("Player over LAN".equals(selected))
            return createLANPlayer(side);
        else
            return new HumanPlayer(selected);
    }

    public boolean useTimeControl() {
        return timeFormat.getSelectedItem() != noTimeControl;
    }

    public Clock.Format getTimeControl() {
        // TODO custom
        return (Clock.Format)timeFormat.getSelectedItem();
    }

    private void addPlayerOptions(Side side, String[] options) {
        String titleStr = side == Side.White ? "White player" : "Black player";
        JLabel title = new JLabel(titleStr);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setHorizontalAlignment(JLabel.LEFT);

        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setEditable(true);
        comboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        players[side.ordinal()] = comboBox;


        panel.add(title);
        panel.add(comboBox);
    }

    private String[] getPlayerOptions() {
        String[] engines = LiteChessGUI.engineManager.getInstalledEngines().toArray(new String[0]);
        String[] options = new String[engines.length + 2];
        System.arraycopy(engines, 0, options, 1, engines.length);
        options[0] = "Human";
        options[1] = "Player over LAN";
        return options;
    }

    private LANPlayer createLANPlayer(Side side) {
        JOptionPane pane = new JOptionPane(
                "Waiting for a player to connect...",
                JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new String[] { "Cancel"}, "Cancel" );

        JDialog dialog = pane.createDialog("Creating game");

        ServerThread thread = new ServerThread(""); // TODO password
        thread.setConnectionCallback((s) -> {
            SwingUtilities.invokeLater(dialog::dispose);
        });

        LANPlayer player = new LANPlayer(thread, true);
        thread.start();

        dialog.pack();
        dialog.show();

        thread.setConnectionCallback(null);
        dialog.dispose();

        String input = (String)pane.getInputValue();
        if(JOptionPane.UNINITIALIZED_VALUE.equals(input)) {
            if(!thread.isConnected()) {
                thread.interrupt();
                throw new RuntimeException("Failed to connect");
            }
        } else {
            throw new RuntimeException("Connection cancelled");
        }

        return player;
    }
}
