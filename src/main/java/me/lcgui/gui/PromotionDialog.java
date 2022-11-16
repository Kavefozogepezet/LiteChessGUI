package me.lcgui.gui;

import me.lcgui.app.LiteChessGUI;
import me.lcgui.game.board.PieceType;
import me.lcgui.game.board.Side;
import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.Move;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

public class PromotionDialog {
    private final LinkedList<Move> promotions = new LinkedList<>();
    private final Side side;
    private final Dimension dimension;
    private final Point position;
    private PieceType selected = null;

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

    public void show() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        PieceType[] types = { PieceType.Queen, PieceType.Rook, PieceType.Knight, PieceType.Bishop };
        JButton[] buttons = new JButton[types.length];

        for(int i = 0; i < types.length; i++) {
            buttons[i] = new JButton(
                    new ImageIcon(
                            LiteChessGUI.style.getResizedPieceTexture(
                                    side, types[i], dimension)));
            buttons[i].setMinimumSize(dimension);
            buttons[i].setPreferredSize(dimension);
            buttons[i].setMaximumSize(dimension);
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

    public Move getSelectedMove() {
        for(var move : promotions)
            if(move.getPromotionPiece().type == selected)
                return move;
        throw new RuntimeException("Illegal promotion");
    }
}
