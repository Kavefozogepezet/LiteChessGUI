package GUI;

import app.LiteChessGUI;
import audio.AudioFX;
import extensions.ColorUtil;
import game.Clock;
import game.Game;
import game.board.*;
import game.event.GameListener;
import game.movegen.Move;
import game.setup.StartPos;
import player.HumanPlayer;

import javax.imageio.ImageIO;
import javax.naming.spi.ObjectFactoryBuilder;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ListIterator;

public class GameView implements GUICreator {
    private Font bigFont;

    private Game game;
    private MoveMaker gameListener = new MoveMaker();
    private BoardView boardView = new BoardView();
    private ListIterator<Game.MoveData> moveItr;
    private PlayerBar[] playersBars = { new PlayerBar(Side.White), new PlayerBar(Side.Black) };
    private JPanel GUIRoot = new JPanel();
    private boolean followGame = true;

    private JButton resignBth, drawBtn;

    public GameView(Game game) {
        setGame(game);
    }

    public GameView() {
        createGUI();
        /*Game defaultGame = new Game();
        defaultGame.setPlayer(Side.White, new HumanPlayer());
        defaultGame.setPlayer(Side.Black, new HumanPlayer());
        setGame(defaultGame);*/
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
        boardView.clearAllHighlight();

        if(game.getPlayer(Side.White) instanceof HumanPlayer whiteHuman)
            whiteHuman.setGameView(this);
        if(game.getPlayer(Side.Black) instanceof HumanPlayer blackHuman)
            blackHuman.setGameView(this);

        this.game = game;
        moveItr = game.getMoveList().listIterator(game.getMoveList().size());
        this.game.addListener(gameListener);
        boardView.copyBoard(game.getBoard());
        playersBars[Side.White.ordinal()].setPlayer(game.getPlayer(Side.White).getName());
        playersBars[Side.Black.ordinal()].setPlayer(game.getPlayer(Side.Black).getName());
        playersBars[Side.White.ordinal()].setTime(game.getClock());
        playersBars[Side.Black.ordinal()].setTime(game.getClock());
        setButtonAvailability();
        GUIRoot.repaint();
    }

    public boolean isFollowingGame() {
        return followGame;
    }

    public void nextMove() {
        if(!moveItr.hasNext()) // we are up-to-date
            return;

        Move move = moveItr.next().move;
        boardView.play(move);

        if(!moveItr.hasNext())
            followGame = true;
    }

    public void prevMove() {
        if(!moveItr.hasPrevious()) // we are at the start position
            return;
        Move move = moveItr.previous().move;
        boardView.unplay(move);
        followGame = false;
    }

    @Override
    public Component createGUI() {
        GUIRoot.removeAll();
        bigFont = GUIRoot.getFont().deriveFont(Font.BOLD, 24.0f);

        GUIRoot.setLayout(new BorderLayout());
        JPanel playerPanel = new JPanel();
        playerPanel.setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(BevelBorder.RAISED),
                new EmptyBorder(15, 10, 15, 10)
        ));
        playerPanel.setLayout(new GridLayout(1, 2));
        playerPanel.add(playersBars[Side.White.ordinal()].createGUI());
        playerPanel.add(playersBars[Side.Black.ordinal()].createGUI());


        GUIRoot.add(playerPanel, BorderLayout.PAGE_START);
        GUIRoot.add(boardView.getRootComponent(), BorderLayout.CENTER);
        GUIRoot.add(createToolBar(), BorderLayout.PAGE_END);

        return GUIRoot;
    }

    @Override
    public Component getRootComponent() {
        return GUIRoot;
    }

    @Override
    public void adjustGUI() {}

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setBorder(new EmptyBorder(5, 5, 5, 5));
        toolbar.setLayout(new FlowLayout());
        toolbar.setFloatable(false);

        ImageIcon
                step = new ImageIcon(),
                skip = null;

        JButton[] buttons = {
                new JButton("\u23EE"), new JButton("\u23F4"),
                new JButton("\u23F5"), new JButton("\u23ED"),
                new JButton("\uD83C\uDFF3"), new JButton("\u00BD")
        };

        buttons[1].addActionListener((l) -> { prevMove(); });
        buttons[2].addActionListener((l) -> { nextMove(); });

        buttons[0].addActionListener((l) -> {
            while(moveItr.hasPrevious())
                prevMove();
        });
        buttons[3].addActionListener((l) -> {
            while(moveItr.hasNext())
                nextMove();
        });
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

    private void setButtonAvailability() {
        boolean enabled = game.getPlayer(game.getState().getTurn()) instanceof HumanPlayer;
        resignBth.setEnabled(enabled);
        drawBtn.setEnabled(enabled);
    }

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

            name.setFont(bigFont);
            name.setHorizontalAlignment(SwingConstants.CENTER);
            name.setVerticalAlignment(SwingConstants.CENTER);

            time.setFont(bigFont);
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

            if(followGame)
                nextMove();
            setButtonAvailability();
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
