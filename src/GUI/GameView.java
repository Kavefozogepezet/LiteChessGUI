package GUI;

import extensions.ColorUtil;
import game.Clock;
import game.Game;
import game.board.*;
import game.movegen.Move;
import game.setup.StartPos;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.util.ListIterator;

public class GameView implements GUICreator {
    private static final String infinity = "\u221e";

    Game game;
    BoardView boardView = new BoardView();
    ListIterator<Move> moveItr;
    Move lastMove = null;
    PlayerBar[] playersBars = new PlayerBar[2];
    JPanel GUIRoot = new JPanel();

    public GameView(Game game) {
        playersBars[Side.White.ordinal()] = new PlayerBar(Side.White);
        playersBars[Side.Black.ordinal()] = new PlayerBar(Side.Black);
        setGame(game);
        createGUI();
    }

    public GameView() {
        this(new Game(new StartPos()));
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        clearHighlights();
        this.game = game;
        boardView.copyBoard(game.getBoard());
        //playersBars[Side.White.ordinal()].setPlayer();
        //playersBars[Side.Black.ordinal()].setPlayer();
        playersBars[Side.White.ordinal()].setTime(game.getClock());
        playersBars[Side.Black.ordinal()].setTime(game.getClock());
        GUIRoot.repaint();
    }

    public void play(@NotNull Move move) {
        clearHighlights();

        boolean atLastMove = !moveItr.hasNext();

        game.play(move);
        int idx = moveItr.nextIndex();
        moveItr = game.getMoveList().listIterator(idx);

        if(atLastMove) {// we are following the match on the board view
            lastMove = moveItr.next();
            boardView.play(lastMove);
        }
        updateHighlights(move);
    }

    public void nextMove() {
        if(!moveItr.hasNext()) // we are up-to-date
            return;

        clearHighlights();
        Move move = moveItr.next();
        boardView.play(move);
        updateHighlights(move);
    }

    public void prevMove() {
        if(!moveItr.hasPrevious()) // we are at the start position
            return;
        clearHighlights();
        Move move = moveItr.previous();
        boardView.play(move);
        updateHighlights(move);
    }

    private void clearHighlights() {
        if(lastMove == null)
            return;

        boardView.setSqHighlight(lastMove.from, BoardView.SqInfoHL.None);
        boardView.setSqHighlight(lastMove.to, BoardView.SqInfoHL.None);
    }

    private void updateHighlights(Move move) {
        boardView.setSqHighlight(move.from, BoardView.SqInfoHL.Moved);
        boardView.setSqHighlight(move.to, BoardView.SqInfoHL.Arrived);
    }

    @Override
    public Component createGUI() {
        GUIRoot.removeAll();

        GUIRoot.setLayout(new BorderLayout());
        JPanel playerPanel = new JPanel();
        playerPanel.setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(BevelBorder.RAISED),
                new EmptyBorder(15, 10, 15, 10)
        ));
        playerPanel.setLayout(new GridLayout(1, 2));
        //playerPanel.setBackground(playerPanel.getBackground().darker());

        playerPanel.add(playersBars[Side.White.ordinal()].getRootComponent());
        playerPanel.add(playersBars[Side.Black.ordinal()].getRootComponent());

        GUIRoot.add(playerPanel, BorderLayout.PAGE_START);
        GUIRoot.add(boardView.getRootComponent(), BorderLayout.CENTER);

        return GUIRoot;
    }

    @Override
    public Component getRootComponent() {
        return GUIRoot;
    }

    private class PlayerBar implements GUICreator {
        private final JPanel frame = new JPanel();
        private Side side;
        private final JLabel
                name = new JLabel(),
                time = new JLabel();

        public PlayerBar(Side side) {
            this.side = side;
            name.setText(side == Side.White ? "White" : "Black");
            time.setText(GameView.infinity);
            createGUI();
        }

        public void setPlayer(String player) {
            name.setText(player);
        }

        public void setTime(Clock clock) {
            String timeStr = clock == null
                    ? GameView.infinity
                    : clock.getTimeStr(side);
            time.setText(timeStr);
        }

        @Override
        public Component createGUI() {
            frame.removeAll();
            frame.setLayout(new GridLayout(1, 1));
            Font font = name.getFont().deriveFont(Font.BOLD, 20);

            name.setFont(font);
            name.setHorizontalAlignment(SwingConstants.CENTER);
            name.setVerticalAlignment(SwingConstants.CENTER);

            time.setFont(font);
            time.setHorizontalAlignment(SwingConstants.CENTER);
            time.setVerticalAlignment(SwingConstants.CENTER);

            boolean isWhite = side == Side.White;
            frame.add(isWhite ? name : time);
            frame.add(isWhite ? time : name);

            //frame.setBackground(frame.getBackground().darker());

            return frame;
        }

        @Override
        public Component getRootComponent() {
            return frame;
        }
    }
}