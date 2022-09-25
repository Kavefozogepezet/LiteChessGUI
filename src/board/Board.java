package board;

import board.event.SquareListener;
import board.movegen.MoveGen;
import board.setup.BoardSetup;
import board.setup.StartPos;
import board.types.*;
import extensions.ColorUtil;
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
    private final Square[] kings = new Square[2];
    private State state = new State();
    private final LinkedList<Move> moveList = new LinkedList<>();
    private final MoveGen movesNow = new MoveGen(this);

    private final EventListenerList sqListeners = new EventListenerList();

    public Board() {
        createGUI();
        movesNow.generate();
    }

    public void newGame(BoardSetup setup) {
        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                var sqb = squares[rank][file];
                sqb.piece = null;
                sqb.setInfoHL(SqInfoHL.None);
                sqb.setMoveHL(SqMoveHL.None);
            }
        }
        moveList.clear();
        state = new State();
        setup.set(this);
        movesNow.generate();
        //foo();
        repaint();
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

        movesNow.generate();
        //foo();

        repaint();
    }

    public void unplay() {
        Move move = moveList.pollLast();

        if(move == null)
            return;

        removePiece(move.to);
        setPiece(move.from, move.moving);

        if(move.isCastle()) {
            Square rookSq;
            if(move.moving.side == Side.White)
                rookSq = move.is(Move.CASTLE_K) ? Square.h1 : Square.a1;
            else
                rookSq = move.is(Move.CASTLE_K) ? Square.h8 : Square.a8;

            Piece rook = getPiece(Square.between(move.from, move.to));
            setPiece(rookSq, rook);
            removePiece(Square.between(move.from, move.to));
        }

        if(move.is(Move.EN_PASSANT))
            setPiece(Square.cross(move.to, move.from), move.captured);
        else if(move.isCapture())
            setPiece(move.to, move.captured);

        state.moveUnplayed();
        repaint();

        movesNow.generate();
        //foo();
    }

    private void foo() {
        for (int rank = 0; rank < Board.BOARD_SIZE; rank++) {
            for (int file = 0; file < Board.BOARD_SIZE; file++) {
                if(movesNow.attackBoard.get(new Square(file, rank)))
                    squares[rank][file].setInfoHL(SqInfoHL.Checked);
                else
                    squares[rank][file].setInfoHL(SqInfoHL.None);
            }
        }
    }

    public void setPiece(@NotNull Square square, Piece piece) {
        squares[square.rank][square.file].piece = piece;
        if(piece.type == PieceType.King)
            kings[piece.side.ordinal()] = square;
    }

    public void setPiece(int file, int rank, Piece piece) {
        setPiece(new Square(file, rank), piece);
    }

    public void removePiece(@NotNull Square square) {
        Piece piece = squares[square.rank][square.file].piece;
        squares[square.rank][square.file].piece = null;
        if(piece.type == PieceType.King)
            kings[piece.side.ordinal()] = null;
    }

    public Piece getPiece(@NotNull Square square) {
        return squares[square.rank][square.file].piece;
    }

    public Piece getPiece(int file, int rank) {
        return squares[rank][file].piece;
    }

    public LinkedList<Move> getMoves(@NotNull Square square) {
        return movesNow.getMoves(square);
    }

    public Square getKing(Side side) {
        return kings[side.ordinal()];
    }

    public void setSqHighlight(Square square, SqInfoHL hl) {
        squares[square.rank][square.file].setInfoHL(hl);
    }

    public void setSqHighlight(Square square, SqMoveHL hl) {
        squares[square.rank][square.file].setMoveHL(hl);
    }

    public static class State {
        public static final int CASTLE_WK = 0b0001;
        public static final int CASTLE_WQ = 0b0010;
        public static final int CASTLE_BK = 0b0100;
        public static final int CASTLE_BQ = 0b1000;

        public static final int CASTLE_W =  CASTLE_WK | CASTLE_WQ;
        public static final int CASTLE_B =  CASTLE_BK | CASTLE_BQ;
        public static final int CASTLE_K =  CASTLE_WK | CASTLE_BK;
        public static final int CASTLE_Q =  CASTLE_WQ | CASTLE_BQ;
        public static final int CASTLE_ALL =CASTLE_W | CASTLE_B;

        private int castlingRights = 0;
        private Square epTarget = Square.invalid;
        private Side turn = Side.White;
        private int ply = 0;

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

        public State(Side turn, int castling, Square ep, int ply) {
            this.turn = turn;
            castlingRights = castling;
            epTarget = ep;
            ply = 0;
        }

        public void movePlayed(@NotNull Move move) {
            prevStates.add(new Record(castlingRights, epTarget));

            epTarget = Square.invalid;

            if(move.moving == Piece.WKing)
                castlingRights &= ~CASTLE_W;
            else if(move.moving == Piece.BKing)
                castlingRights &= ~CASTLE_B;

            if (castlingRights != 0) {
                if (move.to.equals(Square.h1) || move.from.equals( Square.h1))
                    castlingRights &= ~CASTLE_WK;
                else if (move.to.equals(Square.a1) || move.from.equals(Square.a1))
                    castlingRights &= ~CASTLE_WQ;;

                if (move.to.equals(Square.h8) || move.from.equals(Square.h8))
                    castlingRights &= ~CASTLE_BK;
                else if (move.to.equals(Square.a8) || move.from.equals(Square.a8))
                    castlingRights &= ~CASTLE_BQ;
            }

            if(move.is(Move.DOUBLE_PUSH))
                epTarget = Square.between(move.from, move.to);

            turn = turn.other();
            ply++;
        }

        public void moveUnplayed() {
            Record rec = prevStates.pollLast();
            castlingRights = rec.c;
            epTarget = rec.ep;
            turn = turn.other();
            ply--;
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

        public int getPly() { return ply; }
    }

    private JLabel createCoordPanel(int value, boolean file) {
        String content = file ?
                String.valueOf(Square.file2char(value)) :
                String.valueOf(Square.rank2char(value));

        JLabel label = new JLabel(content);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    public void createGUI() {
        removeAll(); // if being recreated, remove previous GUI
        setLayout(new GridBagLayout());

        int gridOffset = BoardStyle.showCoordinates ? 1 : 0;

        GridBagConstraints cell = new GridBagConstraints();
        cell.weightx = 1.0f;
        cell.weighty = 1.0f;
        cell.fill = GridBagConstraints.BOTH;

        GridBagConstraints coord = new GridBagConstraints();
        coord.weightx = 0.3f;
        coord.weighty = 0.3f;
        coord.fill = GridBagConstraints.BOTH;

        GridBagLayout layout = new GridBagLayout();

        JPanel container = new JPanel(layout) {
            @Override
            public @NotNull Dimension getPreferredSize() {
                Dimension parent = getParent().getSize();
                int size = Math.min(parent.width, parent.height);
                size = Math.max(128, size);
                return new Dimension(size, size);
            }
        };

        // notation
        if(BoardStyle.showCoordinates) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                coord.gridy = 0;
                coord.gridx = i + 1;
                JLabel label1 = createCoordPanel(i, true);
                layout.setConstraints(label1, coord);
                container.add(label1);

                coord.gridy = BOARD_SIZE + 1;
                coord.gridx = i + 1;
                JLabel label2 = createCoordPanel(i, true);
                layout.setConstraints(label2, coord);
                container.add(label2);
            }

            for (int rank = 0; rank < BOARD_SIZE; rank++) {
                cell.gridy = BOARD_SIZE - 1 - rank + gridOffset;
                coord.gridy = cell.gridy;

                coord.gridx = 0;
                JLabel label1 = createCoordPanel(rank, false);
                layout.setConstraints(label1, coord);
                container.add(label1);

                coord.gridx = BOARD_SIZE + 1;
                JLabel label2 = createCoordPanel(rank, false);
                layout.setConstraints(label2, coord);
                container.add(label2);
            }
        }

        // squares
        for(int rank = 0; rank < BOARD_SIZE; rank++) {
            cell.gridy = BOARD_SIZE - 1 - rank + gridOffset;
            coord.gridy = cell.gridy;

            for(int file = 0; file < BOARD_SIZE; file++) {
                cell.gridx = file + gridOffset;
                SquareButton sqb = new SquareButton(new Square(file, rank));
                sqb.copyContent(squares[rank][file]);
                squares[rank][file] = sqb;
                layout.setConstraints(sqb, cell);
                container.add(sqb);
            }
        }
        add(container);
    }

    public enum SqInfoHL {
        None, Checked, Moved, Arrived,
    }

    public enum SqMoveHL {
        None, Selected, Move
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

        private SqInfoHL infoState = SqInfoHL.None;
        private SqMoveHL moveState = SqMoveHL.None;

        public SquareButton(Square sq) {
            mySquare = sq;
            baseColor = (sq.file + sq.rank) % 2 == 0 ?
                    BoardStyle.baseWhite :
                    BoardStyle.baseBlack;

            setBackground(baseColor);
            addMouseListener(new SquareMouseListener());
        }

        public void setInfoHL(@NotNull SqInfoHL state) {
            infoState = state;
            setColor();
        }

        public void setMoveHL(@NotNull SqMoveHL state) {
            moveState = state;
            setColor();
        }

        public void copyContent(SquareButton other) {
            if(other == null)
                return;

            piece = other.piece;
            moveState = other.moveState;
            infoState = other.infoState;
        }

        private void setColor() {
            if(moveState == SqMoveHL.None) {
                switch (infoState) {
                    case None -> setBackground(baseColor);
                    case Checked -> setBackground(ColorUtil.blend(baseColor, BoardStyle.sqihCheck));
                    case Moved -> setBackground(ColorUtil.blend(baseColor, BoardStyle.sqihMoved));
                    case Arrived -> setBackground(ColorUtil.blend(baseColor, BoardStyle.sqihArrived));
                }
            }else {
                switch(moveState) {
                    case Move -> setBackground(ColorUtil.blend(baseColor, BoardStyle.sqmhMove));
                    case Selected -> setBackground(ColorUtil.blend(baseColor, BoardStyle.sqmhSelected));
                }
            }
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
    }
}
