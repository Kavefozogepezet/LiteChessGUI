package me.lcgui.gui;

import me.lcgui.app.AudioFX;
import me.lcgui.game.Clock;
import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.Move;
import me.lcgui.game.player.MoveSupplier;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;
import me.lcgui.game.player.Player;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Játszma grafikus megjelenítésére használatos osztály.
 */
public class GameView implements GUICreator, MoveSupplier {
    private static final String SKIP_TO_START_SYMBOL = "\u23EE";
    private static final String BACKWARD_SYMBOL = "\u23F4";
    private static final String FORWARD_SYMBOL = "\u23F5";
    private static final String SKIP_TO_END_SYMBOL = "\u23ED";
    private static final String RESIGN_SYMBOL = "\uD83C\uDFF3";
    private static final String TIMEOUT_SYMBOL = "\u231B";
    private static final String DRAW_SYMBOL = "\u00BD";
    private static final String ABANDONED_SYMBOL = "?";
    private static final String CHECKMATE_SYMBOL = "#";
    private static final String WIN_SYMBOL = "\uD83D\uDC51";
    private static final String INFINITY_SYMBOL = "\u221e";

    private Font bigFont;

    private Game game;
    private boolean followGame = true;
    private final BoardView boardView = new BoardView();
    private ListIterator<Game.MoveData> moveItr;
    private final PlayerBar[] playersBars = { new PlayerBar(Side.White), new PlayerBar(Side.Black) };
    private final JPanel GUIRoot = new JPanel();
    private JButton resignBth, drawBtn;

    // Move selection
    private final Event<Consumable<Move>> moveListeners = new Event<>();

    public GameView(Game game) {
        setGame(game);
    }

    public GameView() {
        createGUI();
        boardView.clickEvent.addListener(new MoveInputHandler());
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public Game getGame() {
        return game;
    }

    /**
     * Átállítja az aktuálisan játszott partit.
     * Felállítja a táblát.
     * @param game Az új parti.
     */
    public void setGame(Game game) {
        if(this.game != null) {
            this.game.moveEvent.removeListener(onMovePlayed);
            this.game.endEvent.removeListener(onGameEnd);
            this.game.tickEvent.removeListener(onTick);
            this.game.resign();
        }
        boardView.clearAllHighlight();
        this.game = game;
        moveItr = game.getMoveList().listIterator(game.getMoveList().size());
        followGame = true;

        this.game.moveEvent.addListener(onMovePlayed);
        this.game.endEvent.addListener(onGameEnd);
        this.game.tickEvent.addListener(onTick);

        boardView.copyBoard(game.getBoard());
        playersBars[Side.White.ordinal()].setPlayer(game.getPlayer(Side.White).getName());
        playersBars[Side.Black.ordinal()].setPlayer(game.getPlayer(Side.Black).getName());
        playersBars[Side.White.ordinal()].setTime(game.getClock());
        playersBars[Side.Black.ordinal()].setTime(game.getClock());
        if(game.hasEnded()) {
            playersBars[Side.White.ordinal()].setResult(game.getResult(), game.getTermination());
            playersBars[Side.Black.ordinal()].setResult(game.getResult(), game.getTermination());
        }
        setButtonAvailability();
        GUIRoot.repaint();
    }

    /**
     * @return igaz, ha a grafikus felület a játszma aktuális állását mutatja.
     */
    public boolean isFollowingGame() {
        return followGame;
    }

    /**
     * A grafikus felületet egy lépéssel előre lépteti.
     */
    public void nextMove() {
        if(!moveItr.hasNext()) // we are up-to-date
            return;

        Move move = moveItr.next().move;
        boardView.play(move);

        if(!moveItr.hasNext())
            followGame = true;
    }

    /**
     * A grafikus felületet egy lépéssel hátrébb lépteti.
     */
    public void prevMove() {
        if(!moveItr.hasPrevious()) // we are at the start position
            return;
        Move move = moveItr.previous().move;
        boardView.unplay(move);
        followGame = false;
    }

    @Override
    public void addMoveListener(Event.Listener<Consumable<Move>> listener) {
        moveListeners.addListener(listener);
    }

    @Override
    public void removeMoveListener(Event.Listener<Consumable<Move>> listener) {
        moveListeners.removeListener(listener);
    }

    @Override
    public Component createGUI() {
        GUIRoot.removeAll();
        bigFont = GUIRoot.getFont().deriveFont(Font.BOLD, 24.0f);
        boardView.createGUI();

        GUIRoot.setLayout(new BorderLayout());
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new GridLayout(1, 2));
        playerPanel.add(playersBars[Side.White.ordinal()].createGUI());
        playerPanel.add(playersBars[Side.Black.ordinal()].createGUI());
        playerPanel.setBorder(new SoftBevelBorder(BevelBorder.RAISED));


        GUIRoot.add(playerPanel, BorderLayout.PAGE_START);
        GUIRoot.add(boardView.getRootComponent(), BorderLayout.CENTER);
        GUIRoot.add(createToolBar(), BorderLayout.PAGE_END);

        return GUIRoot;
    }

    @Override
    public Component getRootComponent() {
        return GUIRoot;
    }

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setBorder(new EmptyBorder(5, 5, 5, 5));
        toolbar.setLayout(new FlowLayout());
        toolbar.setFloatable(false);

        ImageIcon
                step = new ImageIcon(),
                skip = null;

        JButton[] buttons = {
                new JButton(SKIP_TO_START_SYMBOL), new JButton(BACKWARD_SYMBOL),
                new JButton(FORWARD_SYMBOL), new JButton(SKIP_TO_END_SYMBOL),
                new JButton(RESIGN_SYMBOL), new JButton(DRAW_SYMBOL)
        };

        Action CtrlLeft = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prevMove();
            }
        };
        setKeyStrokeButton(buttons[1], CtrlLeft,  KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK);

        Action CtrlRight = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextMove();
            }
        };
        setKeyStrokeButton(buttons[2], CtrlRight, KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK);

        Action AltLeft = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                while(moveItr.hasPrevious())
                    prevMove();
            }
        };
        setKeyStrokeButton(buttons[0], AltLeft, KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK);

        Action AltRight = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                while(moveItr.hasNext())
                    nextMove();
            }
        };
        setKeyStrokeButton(buttons[3], AltRight, KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK);

        buttons[4].addActionListener((l) -> { game.resign(); });
        buttons[5].addActionListener((l) -> { game.draw(); });

        resignBth = buttons[4];
        drawBtn = buttons[5];

        for(var b : buttons) {
            b.setFont(bigFont);
            b.setBackground(b.getBackground().darker());
            toolbar.add(b);
        }

        return toolbar;
    }

    private void setKeyStrokeButton(JButton button, Action action, int key, int mask) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(key, mask);
        button.getActionMap().put(keyStroke.toString(), action);
        button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStroke.toString());
        button.addActionListener(action);
    }

    private void setButtonAvailability() {
        SelectablePlayer p = game.getPlayer(game.getState().getTurn()).getClass().getAnnotation(SelectablePlayer.class);
        boolean guiUser = p != null && p.canUseGUI();
        boolean enabled = !game.hasEnded() && guiUser;
        resignBth.setEnabled(enabled);
        drawBtn.setEnabled(enabled && game.canClaimDraw());
    }

    private final Event.Listener<Game.MoveData> onMovePlayed = (Game.MoveData moveData) -> {
        SwingUtilities.invokeLater(() -> {
            playersBars[0].setTime(game.getClock());
            playersBars[1].setTime(game.getClock());

            int idx = moveItr.nextIndex();
            moveItr = game.getMoveList().listIterator(idx); // list changed, updating the iterator

            if (followGame)
                nextMove();

            setButtonAvailability();
        });
    };

    private final Event.Listener<Game.ResultData> onGameEnd = (Game.ResultData resultData) -> {
        AudioFX.play(AudioFX.Name.GAME_END);
        SwingUtilities.invokeLater(() -> {
            setButtonAvailability();
            playersBars[0].setResult(resultData.result, resultData.termination);
            playersBars[1].setResult(resultData.result, resultData.termination);
        });
    };

    private final Event.Listener<Clock> onTick = (Clock clock) -> {
        SwingUtilities.invokeLater(() -> {
            playersBars[0].setTime(clock);
            playersBars[1].setTime(clock);
        });
    };

    private class PlayerBar implements GUICreator {
        private final JPanel frame = new JPanel();
        private final Side side;
        private final JLabel
                name = new JLabel(),
                time = new JLabel();

        public PlayerBar(Side side) {
            this.side = side;
            time.setText(INFINITY_SYMBOL);
            createGUI();
        }

        public void setPlayer(String playerName) {
            name.setText(playerName);
        }

        public void setResult(Game.Result result, Game.Termination termination) {
            String resStr = "";

            if(result == Game.Result.DRAW) {
                 resStr = DRAW_SYMBOL;
            } else if(result.winner == side) {
                resStr = WIN_SYMBOL;
            } else {
                switch (termination) {
                    case NORMAL -> resStr = CHECKMATE_SYMBOL;
                    case FORFEIT -> resStr = RESIGN_SYMBOL;
                    case TIME_FORFEIT -> resStr = TIMEOUT_SYMBOL;
                    case ABANDONED -> resStr = ABANDONED_SYMBOL;
                }
            }

            name.setText(name.getText() + " " + resStr);
        }

        public void setTime(Clock clock) {
            String timeStr = clock == null
                    ? INFINITY_SYMBOL
                    : clock.getTimeStr(side);
            time.setText(timeStr);
        }

        @Override
        public Component createGUI() {
            boolean isWhite = side == Side.White;

            var constraints = new GridBagConstraints();
            var layout = new GridBagLayout();

            frame.removeAll();
            frame.setLayout(layout);

            name.setFont(bigFont);
            name.setHorizontalAlignment(SwingConstants.CENTER);
            name.setVerticalAlignment(SwingConstants.CENTER);
            constraints.gridx = isWhite ? 0 : 1;
            constraints.weightx = 1.0f;
            layout.setConstraints(name, constraints);

            time.setFont(bigFont);
            time.setHorizontalAlignment(SwingConstants.CENTER);
            time.setVerticalAlignment(SwingConstants.CENTER);
            time.setBackground(isWhite ? Color.WHITE : Color.BLACK);
            time.setForeground(isWhite ? Color.BLACK : Color.WHITE);
            time.setOpaque(true);
            time.setBorder(new EmptyBorder(10, 15, 10, 15));
            constraints.gridx = isWhite ? 1 : 0;
            constraints.weightx = 0.0f;
            layout.setConstraints(time, constraints);

            frame.add(time);
            frame.add(name);
            //frame.setBackground(frame.getBackground().darker());

            return frame;
        }

        @Override
        public Component getRootComponent() {
            return frame;
        }
    }

    private class MoveInputHandler implements Event.Listener<Square> {
        private transient LinkedList<Move> selectedMoves = null;
        private transient Square selected = null;

        @Override
        public void invoked(Square sq) {
            if(!isFollowingGame() || game.hasEnded())
                return;

            Player p = game.getPlayer(game.getState().getTurn());
            SelectablePlayer annot = p.getClass().getAnnotation(SelectablePlayer.class);
            if(annot == null || !annot.canUseGUI())
                return;

            clearHighlights();
            selected = null;

            boolean moveSelected = false;
            if(selectedMoves != null) {
                for (var move : selectedMoves) {
                    if (move.to.equals(sq)) {
                        Move moveToPlay = move;
                        if(move.isPromotion()) {
                            var d = new PromotionDialog(
                                    selectedMoves,
                                    move.from,
                                    move.to,
                                    boardView.getSquareSize(),
                                    boardView.getSquareLocation(move.to));
                            d.show();
                            moveToPlay = d.getSelectedMove();
                        } else {
                            moveToPlay = move;
                        }
                        moveSelected = true;
                        moveListeners.invoke(Consumable.create(moveToPlay));
                        break;
                    }
                }
            }
            if (!moveSelected && boardView.getPiece(sq) != null) {
                selected = sq;
                selectedMoves = game.getPossibleMoves().from(sq);
            }
            else {
                selectedMoves = null;
            }
            setHighlights();
        }

        private void clearHighlights() {
            if(selectedMoves != null)
                for (var move : selectedMoves)
                    boardView.setSqHighlight(move.to, BoardView.SqMoveHL.None);

            if(selected != null)
                boardView.setSqHighlight(selected, BoardView.SqMoveHL.None);
        }

        private void setHighlights() {
            if(selectedMoves != null)
                for (var move : selectedMoves)
                    boardView.setSqHighlight(move.to, BoardView.SqMoveHL.Move);

            if(selected != null)
                boardView.setSqHighlight(selected, BoardView.SqMoveHL.Selected);
        }

        public void boardChanged() {
            clearHighlights();
            selected = null;
            selectedMoves = null;
        }
    }
}
