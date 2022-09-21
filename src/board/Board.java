package board;

import extensions.ColorUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

public class Board extends JPanel {
    public static final int BOARD_SIZE = 8;

    private final SquareButton[][] squares = new SquareButton[BOARD_SIZE][BOARD_SIZE];
    private State state = new State();
    private LinkedList<Move> moveList = new LinkedList<>();

    private EventListenerList sqListeners = new EventListenerList();

    public Board() {
        setLayout(new GridBagLayout());
        JPanel container = new JPanel(new GridLayout(8, 8)) {
            @Override
            public @NotNull Dimension getPreferredSize() {
                Dimension parent = getParent().getSize();
                int size = Math.min(parent.width, parent.height);
                size = Math.max(128, size);
                return new Dimension(size, size);
            }
        };

        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--) {
            for(int file = 0; file < BOARD_SIZE; file++) {
                var sqb = new SquareButton(new Square(file, rank));
                squares[rank][file] = sqb;
                container.add(sqb);
            }
        }
        add(container);
    }

    public State getState() {
        return state;
    }

    public void addSquareListener(SquareListener listener) {
        sqListeners.add(SquareListener.class, listener);
    }

    public void removeSquareListener(SquareListener listener) {
        sqListeners.remove(SquareListener.class, listener);
    }

    public void play(@NotNull Move move) {
        removePiece(move.from);

        if(move.isPromotion())
            setPiece(move.to, move.getPromotionPiece());
        else
            setPiece(move.to, move.moving);

        if(move.isCastle()) {
            Square rookSq;
            if(move.moving.side == Side.White)
                rookSq = move.is(Move.CASTLE_K) ? Square.h1 : Square.a1;
            else
                rookSq = move.is(Move.CASTLE_K) ? Square.h8 : Square.a8;

            Piece rook = getPiece(rookSq);
            setPiece(Square.between(move.from, move.to), rook);
            removePiece(rookSq);
        }

        if(move.is(Move.EN_PASSANT))
            removePiece(Square.cross(move.to, move.from));

        state.movePlayed(move);
        moveList.add(move);
        repaint();
    }

    public void unplay() {
        Move move = moveList.pop();

        removePiece(move.to);
        setPiece(move.from, move.moving);

        if(move.isCastle()) {
            Square rookSq;
            if(move.moving.side == Side.White)
                rookSq = move.is(Move.CASTLE_K) ? Square.h1 : Square.a1;
            else
                rookSq = move.is(Move.CASTLE_K) ? Square.h8 : Square.a8;

            Piece rook = getPiece(rookSq);
            setPiece(rookSq, rook);
            removePiece(Square.between(move.from, move.to));
        }

        if(move.is(Move.EN_PASSANT))
            setPiece(Square.cross(move.to, move.from), move.captured);
        else if(move.isCapture())
            setPiece(move.to, move.captured);

        state.moveUnplayed();
        repaint();
    }

    public void setPiece(@NotNull Square square, Piece piece) {
        squares[square.rank][square.file].piece = piece;
    }

    public Piece getPiece(@NotNull Square square) {
        return squares[square.rank][square.file].piece;
    }

    public void removePiece(@NotNull Square square) {
        squares[square.rank][square.file].piece = null;
    }

    public static class State {
        public static final int CASTLE_WK = 0b0001;
        public static final int CASTLE_WQ = 0b0010;
        public static final int CASTLE_BK = 0b0100;
        public static final int CASTLE_BQ = 0b1000;

        public static final int CASTLE_W =  0b0011;
        public static final int CASTLE_B =  0b1100;

        private int castlingRights = 0;
        private Square epTarget = Square.invalid;
        private Side turn = Side.White;

        private static class Record {
            public int c;
            public Square ep;
            Record(int c, Square ep) {
                this.c = c;
                this.ep = ep;
            }
        }

        LinkedList<Record> prevStates = new LinkedList<>();

        public State() {}

        public State(int castling, Square ep) {
            castlingRights = castling;
            epTarget = ep;
        }

        public void movePlayed(@NotNull Move move) {
            prevStates.add(new Record(castlingRights, epTarget));

            epTarget = Square.invalid;

            if(move.moving == Piece.WKing)
                castlingRights &= ~CASTLE_W;
            else if(move.moving == Piece.BKing)
                castlingRights &= ~CASTLE_B;
            else if(move.moving == Piece.WRook) {
                if(move.from.equals(Square.a1))
                    castlingRights &= ~CASTLE_WQ;
                else if(move.from.equals(Square.h1))
                    castlingRights &= ~CASTLE_WK;
            }
            else if(move.moving == Piece.BRook) {
                if(move.from.equals(Square.a8))
                    castlingRights &= ~CASTLE_BQ;
                else if(move.from.equals(Square.h8))
                    castlingRights &= ~CASTLE_BK;
            }

            if(move.is(Move.DOUBLE_PUSH))
                epTarget = Square.between(move.from, move.to);

            turn = turn.other();
        }

        public void moveUnplayed() {
            Record rec = prevStates.pop();
            castlingRights = rec.c;
            epTarget = rec.ep;
            turn = turn.other();
        }

        public boolean canCastle(int flag) {
            return (castlingRights & flag) != 0;
        }

        public Square getEpTarget() {
            return epTarget;
        }

        public Side getTurn() {
            return turn;
        }
    }

    class SquareButton extends JPanel {
        private class SquareMouseListener implements MouseListener {
            private boolean pressed = false;

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                var listeners = sqListeners.getListeners(SquareListener.class);
                for(var listener : listeners)
                    listener.squarePressed(mySquare, e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                var listeners = sqListeners.getListeners(SquareListener.class);
                for(var listener : listeners) {
                    listener.squareReleased(mySquare, e);
                    if(pressed)
                        listener.squareClicked(mySquare, e);
                }
                pressed = false;
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {
                pressed = false;
            }
        }
        private final Color baseColor;
        private final Square mySquare;
        public Piece piece = null;

        static Color hColor = Color.green;

        public SquareButton(Square sq) {
            mySquare = sq;
            baseColor = (sq.file + sq.rank) % 2 == 0 ?
                    new Color(222, 189, 144) :
                    new Color(189, 122, 67);

            setBackground(baseColor);
            addMouseListener(new SquareMouseListener());
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            if(piece == null)
                return;

            var size = getSize();
            var pieceImg =
                    BoardStyle.getPieceTexture(piece)
                            .getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
            g.drawImage(pieceImg, 0, 0, this);
        }

        public void setHighlight(boolean highlight) {
            Color newColor = highlight ?
                    ColorUtil.blend(baseColor, hColor) :
                    baseColor;
            setBackground(newColor);
        }
    }
}
