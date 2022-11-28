package me.lcgui.gui;

import me.lcgui.app.LiteChessGUI;
import me.lcgui.game.board.Piece;
import me.lcgui.game.board.PieceType;
import me.lcgui.game.board.Side;
import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.Move;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Felugró ablak, amiben a felhasználó kiválaszthatja, milyen tiszté szeretné előléptetni a gyalogot.
 */
public class PromotionDialog {
    private final LinkedList<Move> promotions = new LinkedList<>();
    private final Side side;
    private final Dimension dimension;
    private final Point position;
    private PieceType selected = null;

    /**
     * @param moves Az összes legális lépés a célmezőre.
     * @param from A kezdő mező, ahonnan a gyalog ellépett.
     * @param to A célmező, ahova a tisztet helyezni kell.
     * @param dimension Egy mező mérete a képernyőn.
     * @param position A cél mező bal felső sarka a képernyőn.
     */
    public PromotionDialog(Collection<Move> moves, Square from, Square to, Dimension dimension, Point position) {
        for(var move : moves)
            if(move.isPromotion() && move.from.equals(from) && move.to.equals(to))
                promotions.add(move);

        this.side = promotions.getFirst().moving.side;
        this.dimension = dimension;
        this.position = side == Side.White
                ? position
                : new Point(position.x, position.y - dimension.height * 3);
    }

    /**
     * Megjeleníti a felugró ablakot.
     */
    public void show() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        PieceType[] types = { PieceType.Queen, PieceType.Rook, PieceType.Knight, PieceType.Bishop };
        PromotionButton[] buttons = new PromotionButton[types.length];

        for(int i = 0; i < types.length; i++) {
            buttons[i] = new PromotionButton(side, types[i], dimension);
            panel.add(buttons[i]);
        }

        JDialog d = new JDialog();
        d.setLocation(position);
        d.setResizable(false);
        d.setUndecorated(true);
        d.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        d.add(panel);

        for(int i = 0; i < types.length; i++) {
            int idx = i;
            buttons[i].addActionListener(l -> {
                d.dispose();
                selected = types[idx];
            });
        }

        d.pack();
        d.setVisible(true);
    }

    /**
     * @return A felhasználó által választott előléptetés lekérése.
     */
    public Move getSelectedMove() {
        for(var move : promotions)
            if(move.getPromotionPiece().type == selected)
                return move;
        throw new RuntimeException("Illegal promotion");
    }

    private class PromotionButton extends JButton {
        private final Side side;
        private final PieceType type;

        public PromotionButton(Side side, PieceType type, Dimension dimension) {
            this.type = type;
            this.side = side;

            setMinimumSize(dimension);
            setPreferredSize(dimension);
            setMaximumSize(dimension);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            Graphics2D g2D = (Graphics2D) g;
            var hints = g2D.getRenderingHints();

            g2D.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            var size = getSize();
            Image img = LiteChessGUI.style.getPieceTexture(side, type);
            g2D.drawImage(img, 0, 0, size.width, size.height, this);

            g2D.setRenderingHints(hints);
        }
    }
}
