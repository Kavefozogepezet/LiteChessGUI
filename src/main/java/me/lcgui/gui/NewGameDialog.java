package me.lcgui.gui;

import me.lcgui.app.LiteChessGUI;
import me.lcgui.game.Clock;
import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
import me.lcgui.game.setup.FEN;
import me.lcgui.game.setup.GameSetup;
import me.lcgui.game.setup.StartPos;
import me.lcgui.player.*;

import javax.sound.sampled.BooleanControl;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.text.NumberFormat;
import java.util.HashMap;

public class NewGameDialog {
    private static final String DEFAULT_POS = "Default start position";
    private static final String FEN_POS = "FEN position from clipboard";

    private final JOptionPane optionPane;
    private JDialog dialog = null;

    private final PlayerData[] players = { new PlayerData(Side.White), new PlayerData(Side.Black) };

    private final TimerData timerData = new TimerData();

    private final JComboBox<String> startpos = new JComboBox<>(new String[] { DEFAULT_POS, FEN_POS });

    public NewGameDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel posPanel = new JPanel();
        posPanel.add(startpos);
        posPanel.setBorder(new TitledBorder("Starting position"));
        posPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        panel.add(players[0].getRootComponent());
        panel.add(players[1].getRootComponent());
        panel.add(timerData.getRootComponent());
        panel.add(posPanel);

        optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    }

    public boolean show() {
        dialog = optionPane.createDialog("NewGame");
        dialog.setVisible(true);
        dialog.dispose();

        return optionPane.getValue() != null;
    }

    public Game createGame() throws Exception {
        GameSetup setup = new StartPos();
        if (FEN_POS.equals(startpos.getSelectedItem())) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            DataFlavor flavor = DataFlavor.stringFlavor;
            if(clipboard.isDataFlavorAvailable(flavor)) {
                try {
                    String fen = (String) clipboard.getData(flavor);
                    setup = new FEN(fen);
                } catch (Exception e) {
                    throw new RuntimeException("Couldn't read FEN from clipboard");
                }
            }
        }
        Clock.Format format = timerData.getFormat();

        if(format == null) {
            return new Game(setup);
        } else {
            return new Game(setup, format);
        }
    }

    public Player createPlayer(Side side) throws Exception {
        return players[side.ordinal()].create();
    }

    private class PlayerData implements GUICreator {
        private final Side mySide;

        private final JPanel playerPanel = new JPanel();

        private JComboBox<String> playerOp = new JComboBox<String>();
        private final JPanel playerArgs = new JPanel();
        private HashMap<String, DrawableFactory<? extends Player>> factories;

        public PlayerData(Side side) {
            this.mySide = side;
            createGUI();
        }

        public Player create() throws Exception {
            String selected = (String)playerOp.getSelectedItem();
            var factory = factories.get(selected);
            return factory.instantiate();
        }

        @Override
        public Component createGUI() {
            playerPanel.removeAll();

            factories = createPlayerArgs();
            String[] opStrs = factories.keySet().toArray(new String[0]);

            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.PAGE_AXIS));
            playerPanel.setBorder(new TitledBorder(mySide == Side.White ? "White" : "Black"));

            playerOp = new JComboBox<>(opStrs);
            playerArgs.add(factories.get(opStrs[0]).getRootComponent());

            playerOp.addActionListener((l) -> {
                playerArgs.removeAll();
                var factory = factories.get((String) playerOp.getSelectedItem());
                playerArgs.add(factory.getRootComponent());
                dialog.revalidate();
                dialog.pack();
            });

            playerPanel.add(playerOp);
            playerPanel.add(playerArgs);
            playerPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            return this.playerPanel;
        }

        @Override
        public Component getRootComponent() {
            return playerPanel;
        }

        private HashMap<String, DrawableFactory<? extends Player>> createPlayerArgs() {
            var map = new HashMap<String, DrawableFactory<? extends Player>>();
            for(var annot : LiteChessGUI.players) {
                try {
                    DrawableFactory<? extends Player> factory = annot.factoryClass().getDeclaredConstructor().newInstance();
                    map.put(annot.name(), factory);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            return map;
        }
    }

    private class TimerData implements GUICreator {
        private static final Clock.Format customTimeControl = new Clock.Format(1, "custom");
        private static final Clock.Format noTimeControl = new Clock.Format(1, "No time control");

        private final JPanel panel = new JPanel();
        private final JComboBox<Clock.Format> timeFormat = new JComboBox<>(new Clock.Format[] {
                noTimeControl,
                Clock.Format.Bullet, Clock.Format.Blitz, Clock.Format.Classical,
                Clock.Format.FIDE_Blitz, Clock.Format.FIDE_Rapid,
                customTimeControl
        });
        private final JPanel timePanel = new JPanel();
        private final JFormattedTextField[] timeFields = new JFormattedTextField[4];
        private final JComboBox<Clock.Format.IncType> incType = new JComboBox<>(new Clock.Format.IncType[]{
                Clock.Format.IncType.NONE, Clock.Format.IncType.FISCHER,
                Clock.Format.IncType.DELAY, Clock.Format.IncType.BRONSTEIN
        });

        public TimerData() {
            createGUI();
        }

        public Clock.Format getFormat() {
            Clock.Format format = (Clock.Format) timeFormat.getSelectedItem();
            if(format == noTimeControl) {
                return null;
            } else if(format == customTimeControl) {
                int
                        wtime = Integer.parseInt(timeFields[0].getText()),
                        winc = Integer.parseInt(timeFields[1].getText()),
                        btime = Integer.parseInt(timeFields[2].getText()),
                        binc = Integer.parseInt(timeFields[3].getText());
                var inc = (Clock.Format.IncType) incType.getSelectedItem();
                format = new Clock.Format(wtime, winc, btime, binc, inc, "custom");
            }
            return format;
        }

        @Override
        public Component createGUI() {
            panel.removeAll();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setBorder(new TitledBorder("Time Control"));

            JPanel customTimePanel = new JPanel();
            customTimePanel.setLayout(new BoxLayout(customTimePanel, BoxLayout.Y_AXIS));

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
            timePanel.add(incType);

            JLabel incTypeTxt = new JLabel("Increment type:");
            incTypeTxt.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            timePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            incType.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            customTimePanel.add(timePanel);
            customTimePanel.add(incTypeTxt);
            customTimePanel.add(incType);
            customTimePanel.setVisible(false);

            timeFormat.addActionListener((l) -> {
                boolean enable = customTimeControl.equals(timeFormat.getSelectedItem());
                customTimePanel.setVisible(enable);
                if(dialog != null)
                    dialog.pack();
            });

            timeFormat.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            customTimePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            panel.add(timeFormat);
            panel.add(customTimePanel);
            panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            return panel;
        }

        @Override
        public Component getRootComponent() {
            return panel;
        }
    }
}
