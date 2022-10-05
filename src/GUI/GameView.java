package GUI;

import audio.AudioFX;
import extensions.ColorUtil;
import game.Clock;
import game.Game;
import game.board.*;
import game.event.GameListener;
import game.movegen.Move;
import game.setup.StartPos;
import player.HumanPlayer;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.util.ListIterator;

public class GameView implements GUICreator {
    Game game;
    MoveMaker gameListener = new MoveMaker();
    BoardView boardView = new BoardView();
    ListIterator<Move> moveItr;
    Move lastMove = null;
    PlayerBar[] playersBars = { new PlayerBar(Side.White), new PlayerBar(Side.Black) };
    JPanel GUIRoot = new JPanel();
    boolean followGame = true;

    public GameView(Game game) {
        setGame(game);
        createGUI();
    }

    public GameView() {
        HumanPlayer h1 = new HumanPlayer(), h2 = new HumanPlayer();
        setGame(new Game(h1, h2));
        createGUI();
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        if(this.game != null) {
            this.game.removeListener(gameListener);
            this.game.resign();
        }
        clearHighlights();

        if(game.getPlayer(Side.White) instanceof HumanPlayer whiteHuman)
            whiteHuman.setGameView(this);
        if(game.getPlayer(Side.Black) instanceof HumanPlayer blackHuman)
            blackHuman.setGameView(this);

        this.game = game;
        moveItr = game.getMoveList().listIterator(game.getMoveList().size());
        game.addListener(gameListener);
        boardView.copyBoard(game.getBoard());
        playersBars[Side.White.ordinal()].setPlayer(game.getPlayer(Side.White).getName());
        playersBars[Side.Black.ordinal()].setPlayer(game.getPlayer(Side.Black).getName());
        playersBars[Side.White.ordinal()].setTime(game.getClock());
        playersBars[Side.Black.ordinal()].setTime(game.getClock());
        GUIRoot.repaint();
    }

    public void nextMove() {
        if(!moveItr.hasNext()) // we are up-to-date
            return;

        clearHighlights();
        Move move = moveItr.next();
        boardView.play(move);
        updateHighlights(move);

        if(!moveItr.hasNext())
            followGame = true;
    }

    public void prevMove() {
        if(!moveItr.hasPrevious()) // we are at the start position
            return;
        clearHighlights();
        Move move = moveItr.previous();
        boardView.play(move);
        updateHighlights(move);
        followGame = false;
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

    @Override
    public void adjustGUI() {}

    private class PlayerBar implements GUICreator {
        private static final String INFINITY_SYMBOL = "\u221e";

        private final JPanel frame = new JPanel();
        private Side side;
        private final JLabel
                name = new JLabel(),
                time = new JLabel();

        public PlayerBar(Side side) {
            this.side = side;
            name.setText(side == Side.White ? "White" : "Black");
            time.setText(INFINITY_SYMBOL);
            createGUI();
        }

        public void setPlayer(String player) {
            name.setText(player);
        }

        public void setTime(Clock clock) {
            String timeStr = clock == null
                    ? INFINITY_SYMBOL
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

        @Override
        public void adjustGUI() {}
    }

    private class MoveMaker implements GameListener {

        @Override
        public void movePlayed(Move move, String SAN) {
            System.out.println(game.getState().getPly() + " " + SAN);
            playersBars[game.getState().getTurn().other().ordinal()].setTime(game.getClock());

            int idx = moveItr.nextIndex();
            moveItr = game.getMoveList().listIterator(idx); // list changed, updating the iterator

            if(followGame) {
                clearHighlights();
                lastMove = moveItr.next();
                boardView.play(lastMove);
                updateHighlights(move);
            }
        }

        @Override
        public void gameEnded(Game.Result result, Game.Termination termination) {
            AudioFX.play(AudioFX.Name.GAME_END);
        }

        @Override
        public void timeTick(Clock clock) {
            playersBars[game.getState().getTurn().ordinal()].setTime(game.getClock());
        }
    }
}
