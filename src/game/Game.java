package game;

import game.board.*;
import game.movegen.MoveGen;
import game.movegen.Move;
import game.setup.GameSetup;
import game.setup.StartPos;
import org.jetbrains.annotations.NotNull;
import player.Player;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.LinkedList;

public class Game extends JLabel {
    Board board = new Board();
    Clock clock = null;

    JLabel[] players = new JLabel[2];
    JLabel[] times = new JLabel[2];

    private State state = new State();
    private final LinkedList<Move> moveList = new LinkedList<>();
    private final MoveGen movesNow = new MoveGen(this);

    private JPanel createPlayerPanel(String name, Side side) {
        int idx = side.ordinal();

        JPanel panel = new JPanel(new GridLayout(1, 2));
        players[idx] = new JLabel(name);

        String fontName = players[idx].getFont().getName();
        Font font = new Font(fontName, Font.BOLD, 24);

        players[idx].setHorizontalAlignment(SwingConstants.CENTER);
        players[idx].setFont(font);

        times[idx] = new JLabel("0.0");
        times[idx].setHorizontalAlignment(SwingConstants.CENTER);
        times[idx].setFont(font);

        panel.add(players[idx]);
        panel.add(times[idx]);

        panel.setBackground(panel.getBackground().darker());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    public Game() {
        JPanel
                header = createPlayerPanel("Black player", Side.Black),
                footer = createPlayerPanel("White player", Side.White);

        setLayout(new BorderLayout());
        add(header, BorderLayout.PAGE_START);
        add(footer, BorderLayout.PAGE_END);
        add(board, BorderLayout.CENTER);

        movesNow.generate();
    }

    public Game(Clock.Format timeControl) {
        this();
        clock = new Clock(times[0], times[1], state.getTurn(), timeControl);
    }

    public void newGame(GameSetup setup) {
        board.clear();
        moveList.clear();
        setup.set(this);
        board.repaint();

        clock.reset(state.getTurn());

        movesNow.generate();
    }

    public void newGame() {
        newGame(new StartPos());
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }

    public Board getBoard() { return board; }

    public LinkedList<Move> getMoves(Square sq) {
        return movesNow.getMoves(sq);
    }

    public void play(@NotNull Move move) {
        clearHighlights();

        board.removePiece(move.from);

        if(move.isPromotion())
            board.setPiece(move.to, move.getPromotionPiece());
        else
            board.setPiece(move.to, move.moving);

        if(move.isCastle()) {
            Square rookSq;
            if(move.moving.side == Side.White)
                rookSq = move.is(Move.CASTLE_K) ? Square.h1 : Square.a1;
            else
                rookSq = move.is(Move.CASTLE_K) ? Square.h8 : Square.a8;

            Piece rook = board.getPiece(rookSq);
            board.setPiece(Square.between(move.from, move.to), rook);
            board.removePiece(rookSq);
        }

        if(move.is(Move.EN_PASSANT))
            board.removePiece(Square.cross(move.to, move.from));

        state.movePlayed(move);
        moveList.add(move);

        if(clock != null) {
            if (!clock.isRunning())
                clock.start();
            clock.movePlayed();
        }

        board.repaint();
        movesNow.generate();

        updateHighlights();
    }

    public void unplay() {
        clearHighlights();
        Move move = moveList.pollLast();

        if(move == null)
            return;

        board.removePiece(move.to);
        board.setPiece(move.from, move.moving);

        if(move.isCastle()) {
            Square rookSq;
            if(move.moving.side == Side.White)
                rookSq = move.is(Move.CASTLE_K) ? Square.h1 : Square.a1;
            else
                rookSq = move.is(Move.CASTLE_K) ? Square.h8 : Square.a8;

            Piece rook = board.getPiece(Square.between(move.from, move.to));
            board.setPiece(rookSq, rook);
            board.removePiece(Square.between(move.from, move.to));
        }

        if(move.is(Move.EN_PASSANT))
            board.setPiece(Square.cross(move.to, move.from), move.captured);
        else if(move.isCapture())
            board.setPiece(move.to, move.captured);

        state.moveUnplayed();
        board.repaint();

        movesNow.generate();

        updateHighlights();
    }

    private void clearHighlights() {
        if(moveList.isEmpty())
            return;

        Move move = moveList.getLast();

        board.setSqHighlight(move.from, Board.SqInfoHL.None);
        board.setSqHighlight(move.to, Board.SqInfoHL.None);

        board.setSqHighlight(board.getKing(state.getTurn()), Board.SqInfoHL.None);
    }

    private void updateHighlights() {
        if(moveList.isEmpty())
            return;

        Move move = moveList.getLast();

        board.setSqHighlight(move.from, Board.SqInfoHL.Moved);
        board.setSqHighlight(move.to, Board.SqInfoHL.Arrived);

        if(movesNow.isCheck())
            board.setSqHighlight(board.getKing(state.getTurn()), Board.SqInfoHL.Checked);
    }
}
