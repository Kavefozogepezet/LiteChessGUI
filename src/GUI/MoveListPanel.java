package GUI;

import engine.SearchInfo;
import game.Clock;
import game.Game;
import game.board.Side;
import game.event.GameListener;
import game.movegen.Move;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class MoveListPanel implements GUICreator {
    private final DefaultTableModel tModel = new DefaultTableModel(new String[] { "move", "white", "black", "comment" }, 0);
    private Game myGame;
    private GameListener myListener;
    private JScrollPane GUIRoot;

    public MoveListPanel() {
        createGUI();
    }

    public void followGame(Game game) {
        if(myGame != null)
            myGame.removeListener(myListener);

        tModel.setRowCount(0);

        myGame = game;
        myListener = new MoveListener();
        myGame.addListener(myListener);
    }

    @Override
    public Component createGUI() {
        JTable jTable = new JTable(tModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        jTable.setFillsViewportHeight(true);
        jTable.getTableHeader().setReorderingAllowed(false);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumnModel tcModel = jTable.getColumnModel();
        int[] widths = { 64, 64, 64 };
        for(int i = 0; i < Math.min(widths.length, tcModel.getColumnCount()); i++) {
            tcModel.getColumn(i).setMaxWidth(widths[i]);
            tcModel.getColumn(i).setMinWidth(widths[i]);
        }

        GUIRoot = new JScrollPane(jTable);
        return GUIRoot;
    }

    @Override
    public Component getRootComponent() {
        return GUIRoot;
    }

    @Override
    public void adjustGUI() {}

    private class MoveListener implements GameListener {
        @Override
        public void movePlayed(Move move, String SAN) {
            int ply = myGame.getState().getPly() - 1;
            int moveNum =  ply / 2 + 1;
            if(moveNum > tModel.getRowCount())
                tModel.addRow(new String[] { Integer.toString(moveNum), SAN, "", "" });
            else
                tModel.setValueAt(SAN, moveNum - 1, 2);
        }

        @Override
        public void gameEnded(Game.Result result, Game.Termination termination) {}
        @Override
        public void timeTick(Clock clock) {}
    }
}
