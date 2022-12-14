package me.lcgui.gui;

import me.lcgui.app.LiteChessGUI;
import me.lcgui.app.AudioFX;
import me.lcgui.game.board.*;
import me.lcgui.misc.ColorExt;
import me.lcgui.game.movegen.Move;
import me.lcgui.misc.Event;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * SAkktábla grafigus megjelenítésére használatos osztály.
 */
public class BoardView extends AbstractBoard implements GUICreator {
    public static final String SHOW_COODDINATES = "show_coordinates";
    public static final String SHOW_POSSIBLE_MOVES = "show_possible_moves";
    public static final String SHOW_SQUARE_INFO = "show_square_info";

    /**
     * Az esemény, amikor a felhasználó rákattint az egyik mezőre.
     */
    public final Event<Square> clickEvent = new Event<>();

    private final SquareButton[][] squares = new SquareButton[BOARD_SIZE][BOARD_SIZE];
    private final JPanel GUIRoot = new JPanel();

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

    /**
     * Egy {@link Board} példály állását átmásolja.
     * @param board A tábla, amit másol.
     */
    public void copyBoard(Board board) {
        for(int rank = 0; rank < BOARD_SIZE; rank++) {
            for(int file = 0; file < BOARD_SIZE; file++) {
                setPiece(file, rank, board.getPiece(file, rank));
            }
        }
    }

    /**
     * {@link AbstractBoard#play(Move)}
     * Lejátszja a lépés hangeffektjéz.
     * @param move A végrehajtandó lépés.
     */
    @Override
    public void play(Move move) {
        super.play(move);

        clearAllHighlight();
        setSqHighlight(move.from, SqInfoHL.Moved);
        setSqHighlight(move.to, SqInfoHL.Arrived);

        if(move.isCapture())
            AudioFX.play(AudioFX.Name.CAPTURE);
        else
            AudioFX.play(AudioFX.Name.MOVE);

        Side[] sides = { Side.White, Side.Black };
        for(var side : sides) {
            if(isCheck(side))
                setSqHighlight(getKing(side), SqInfoHL.Checked);
        }
    }

    /**
     * {@link AbstractBoard#unplay(Move)}
     * Lejátszja a lépés hangeffektjéz.
     * @param move A végrehajtandó lépés.
     */
    @Override
    public void unplay(Move move) {
        super.unplay(move);

        clearAllHighlight();
        setSqHighlight(move.from, SqInfoHL.Moved);
        setSqHighlight(move.to, SqInfoHL.Arrived);

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

    /**
     * @return A mezők mérete a képernyőn.
     */
    public Dimension getSquareSize() {
        return squares[0][0].getSize();
    }

    /**
     * @param sq A mező aminek a helyét keressük.
     * @return A mező koordinátái a képernyőn.
     */
    public Point getSquareLocation(Square sq) {
        return squares[sq.rank][sq.file].getLocationOnScreen();
    }

    /**
     * Beállítja a mező színezését adott stílusúra.
     * @param square A mező.
     * @param hl A mező kiemelése.
     */
    public void setSqHighlight(Square square, SqInfoHL hl) {
        squares[square.rank][square.file].setInfoHL(hl);
    }

    /**
     * Beállítja a mező színezését adott stílusúra.
     * @param square A mező.
     * @param hl A mező kiemelése.
     */
    public void setSqHighlight(Square square, SqMoveHL hl) {
        squares[square.rank][square.file].setMoveHL(hl);
    }

    /**
     * Törli az összes kiamalést.
     */
    public void clearAllHighlight() {
        for(int rank = 0; rank < BOARD_SIZE; rank++) {
            for(int file = 0; file < BOARD_SIZE; file++) {
                Square sq = new Square(file, rank);
                setSqHighlight(sq, SqInfoHL.None);
                setSqHighlight(sq, SqMoveHL.None);
            }
        }
    }

    /**
     * Frissíti a kiemelések színét és a bábu textúrákat.
     */
    public void updateStyle() {
        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--)
            for (int file = 0; file < BOARD_SIZE; file++)
                squares[rank][file].setColor();
        GUIRoot.repaint();
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
        GUIRoot.removeAll();
        GUIRoot.setLayout(new GridBagLayout());

        int gridOffset = LiteChessGUI.settings.get(SHOW_COODDINATES, true) ? 1 : 0;

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

        // notation
        if(LiteChessGUI.settings.get(SHOW_COODDINATES, true)) {
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
        GUIRoot.revalidate();
        GUIRoot.repaint();
        return GUIRoot;
    }

    @Override
    public Component getRootComponent() {
        return GUIRoot;
    }

    /**
     * Egy mező általános kiemelésének értékeit tartalmazó enum.
     */
    public enum SqInfoHL {
        None, Checked, Moved, Arrived,
    }

    /**
     * Egy mező felhasználói interakcióra történő kiemelésének értékeit tartalmazó enum.
     */
    public enum SqMoveHL {
        None, Selected, Move
    }

    private class SquareButton extends JPanel {
        private final Square mySquare;
        public Piece piece = null;

        private SqInfoHL infoState = SqInfoHL.None;
        private SqMoveHL moveState = SqMoveHL.None;

        public SquareButton(Square sq) {
            mySquare = sq;
            setColor();
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

        public void setColor() {
            Color baseColor = mySquare.isLight() ?
                    LiteChessGUI.style.baseLight :
                    LiteChessGUI.style.baseDark;

            if(moveState != SqMoveHL.None
                    && LiteChessGUI.settings.get(SHOW_POSSIBLE_MOVES, true)) {
                switch(moveState) {
                    case Move -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqmhMove));
                    case Selected -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqmhSelected));
                }
            } else if(infoState != SqInfoHL.None
                    && LiteChessGUI.settings.get(SHOW_SQUARE_INFO, true)) {
                switch (infoState) {
                    case Checked -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqihCheck));
                    case Moved -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqihMoved));
                    case Arrived -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqihArrived));
                }
            } else {
                setBackground(baseColor);
            }
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            if(piece == null)
                return;

            Graphics2D g2D = (Graphics2D) g;
            var hints = g2D.getRenderingHints();

            g2D.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    LiteChessGUI.style.textureInterpolation);

            var size = getSize();
            Image img = LiteChessGUI.style.getPieceTexture(piece);
            g2D.drawImage(img, 0, 0, size.width, size.height, this);

            g2D.setRenderingHints(hints);
        }

        private class SquareMouseListener implements MouseListener {
            private boolean pressed = false;

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1)
                    pressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && pressed) {
                    pressed = false;
                    clickEvent.invoke(mySquare);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {
                pressed = false;
            }
        }
    }
}
