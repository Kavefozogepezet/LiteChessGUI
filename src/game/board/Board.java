package game.board;

import extensions.ColorUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Board extends JPanel {
    public static final int BOARD_SIZE = 8;

    private final SquareButton[][] squares = new SquareButton[BOARD_SIZE][BOARD_SIZE];
    private final Square[] kings = new Square[2];
    private final EventListenerList sqListeners = new EventListenerList();

    public Board() {
        createGUI();
    }

    public void clear() {
        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                var sqb = squares[rank][file];
                sqb.piece = null;
                sqb.setInfoHL(SqInfoHL.None);
                sqb.setMoveHL(SqMoveHL.None);
            }
        }
        kings[0] = null;
        kings[1] = null;
        repaint();
    }

    public void addSquareListener(SquareListener listener) {
        sqListeners.add(SquareListener.class, listener);
    }

    public void removeSquareListener(SquareListener listener) {
        sqListeners.remove(SquareListener.class, listener);
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

    public Square getKing(Side side) {
        return kings[side.ordinal()];
    }

    public void setSqHighlight(Square square, SqInfoHL hl) {
        squares[square.rank][square.file].setInfoHL(hl);
    }

    public void setSqHighlight(Square square, SqMoveHL hl) {
        squares[square.rank][square.file].setMoveHL(hl);
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
