package me.lcgui.gui;

import me.lcgui.app.LiteChessGUI;
import me.lcgui.game.board.Piece;
import me.lcgui.game.board.Square;
import me.lcgui.misc.ColorExt;
import me.lcgui.misc.Event;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class SquareButton extends JPanel {
    public enum SqInfoHL {
        None, Checked, Moved, Arrived,
    }

    public enum SqMoveHL {
        None, Selected, Move
    }

    public final Event<Square> clickEvent = new Event<>();
    public final Event<Square> hoverEvent = new Event<>();
    public final Event<Square> hoverOffEvent = new Event<>();

    private final Square mySquare;
    private Piece piece = null;

    private SqInfoHL infoState = SqInfoHL.None;
    private SqMoveHL moveState = SqMoveHL.None;

    public SquareButton(Square sq) {
        mySquare = sq;
        setColor();
        addMouseListener(new SquareMouseListener());
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
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

        if(moveState == SqMoveHL.None) {
            switch (infoState) {
                case None -> setBackground(baseColor);
                case Checked -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqihCheck));
                case Moved -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqihMoved));
                case Arrived -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqihArrived));
            }
        }else {
            switch(moveState) {
                case Move -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqmhMove));
                case Selected -> setBackground(ColorExt.overlay(baseColor, LiteChessGUI.style.sqmhSelected));
            }
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
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

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
