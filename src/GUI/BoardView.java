package GUI;

import app.LiteChessGUI;
import audio.AudioFX;
import extensions.ColorUtil;
import game.board.*;
import game.event.SquareListener;
import game.movegen.Move;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class BoardView extends AbstractBoard implements GUICreator {
    private final SquareButton[][] squares = new SquareButton[BOARD_SIZE][BOARD_SIZE];
    private final LinkedList<SquareListener> sqListeners = new LinkedList<>();
    private final JPanel GUIRoot = new JPanel();

    private Timer quietTimer = null;
    private boolean quietPaint = true;

    public BoardView() {
        createGUI();
    }

    @Override
    public void clear() {
        super.clear();
        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                var sqb = squares[rank][file];
                sqb.setInfoHL(SqInfoHL.None);
                sqb.setMoveHL(SqMoveHL.None);
            }
        }
        GUIRoot.repaint();
    }

    public void copyBoard(Board board) {
        for(int rank = 0; rank < BOARD_SIZE; rank++) {
            for(int file = 0; file < BOARD_SIZE; file++) {
                setPiece(file, rank, board.getPiece(file, rank));
            }
        }
    }

    public void addSquareListener(SquareListener listener) {
        sqListeners.add(listener);
    }

    public void removeSquareListener(SquareListener listener) {
        sqListeners.remove(listener);
    }

    @Override
    public void play(Move move) {
        super.play(move);
        if(move.isCapture())
            AudioFX.play(AudioFX.Name.CAPTURE);
        else
            AudioFX.play(AudioFX.Name.MOVE);
    }

    @Override
    public void unplay(Move move) {
        super.unplay(move);
        if(move.isCapture())
            AudioFX.play(AudioFX.Name.CAPTURE);
        else
            AudioFX.play(AudioFX.Name.MOVE);
    }

    @Override
    public void setPiece(Square square, Piece piece) {
        super.setPiece(square, piece);
        var sqb = squares[square.rank][square.file];
        sqb.piece = piece;
        sqb.repaint();
    }

    @Override
    public void removePiece(Square square) {
        super.removePiece(square);
        var sqb = squares[square.rank][square.file];
        sqb.piece = null;
        sqb.repaint();
    }

    @Override
    public Piece getPiece(Square square) {
        return squares[square.rank][square.file].piece;
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

    @Override
    public Component createGUI() {
        GUIRoot.removeAll(); // if being recreated, remove previous GUI
        GUIRoot.setLayout(new GridBagLayout());

        int gridOffset = LiteChessGUI.style.showCoordinates ? 1 : 0;

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
            private Dimension lastDim = null;

            @Override
            public Dimension getPreferredSize() {
                Dimension parent = getParent().getSize();
                int size = Math.min(parent.width, parent.height);
                size = Math.max(128, size);
                return new Dimension(size, size);
            }
        };
        container.addComponentListener(new ComponentAdapter() {
            private void setQuiet() {
                quietPaint = false;

                if(quietTimer != null)
                    quietTimer.cancel();

                quietTimer = new Timer(true);
                quietTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        quietPaint = true;
                        container.repaint();
                    }
                }, 100L);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                setQuiet();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                super.componentMoved(e);
                setQuiet();
            }
        });

        // notation
        if(LiteChessGUI.style.showCoordinates) {
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
        GUIRoot.add(container);
        return GUIRoot;
    }

    @Override
    public Component getRootComponent() {
        return GUIRoot;
    }

    @Override
    public void adjustGUI() {}

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
                for(var listener : sqListeners)
                    listener.squarePressed(mySquare, e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                for(var listener : sqListeners) {
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
                    LiteChessGUI.style.baseWhite :
                    LiteChessGUI.style.baseBlack;

            setBackground(baseColor);
            addMouseListener(new SquareMouseListener());
        }

        public void setInfoHL(SqInfoHL state) {
            infoState = state;
            setColor();
        }

        public void setMoveHL(SqMoveHL state) {
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
                    case Checked -> setBackground(ColorUtil.blend(baseColor, LiteChessGUI.style.sqihCheck));
                    case Moved -> setBackground(ColorUtil.blend(baseColor, LiteChessGUI.style.sqihMoved));
                    case Arrived -> setBackground(ColorUtil.blend(baseColor, LiteChessGUI.style.sqihArrived));
                }
            }else {
                switch(moveState) {
                    case Move -> setBackground(ColorUtil.blend(baseColor, LiteChessGUI.style.sqmhMove));
                    case Selected -> setBackground(ColorUtil.blend(baseColor, LiteChessGUI.style.sqmhSelected));
                }
            }
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            if(piece == null)
                return;

            var size = getSize();
            if(quietPaint) {
                var pieceImg = LiteChessGUI.style.getPieceTexResized(piece, size);
                g.drawImage(pieceImg, 0, 0, this);
            } else {
                g.drawImage(LiteChessGUI.style.getPieceTexture(piece), 0, 0, size.width, size.height, this);
            }
        }
    }
}
