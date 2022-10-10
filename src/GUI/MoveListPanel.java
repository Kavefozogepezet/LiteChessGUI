package GUI;

import engine.SearchInfo;
import game.Clock;
import game.Game;
import game.board.Side;
import game.event.GameListener;
import game.movegen.Move;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MoveListPanel implements GUICreator {
    private static final int PLY_COL = 0;
    private static final int WHITE_MOVE_COL = 1;
    private static final int BLACK_MOVE_COL = 2;
    private static final int WHITE_COMMENT_COL = 3;
    private static final int BLACK_COMMENT_COL = 4;

    private final DefaultTableModel tModel = new DefaultTableModel(new String[] { "", "white", "black", "white's comment", "black's comment" }, 0);
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
                return column == WHITE_COMMENT_COL || column == BLACK_COMMENT_COL;
            }
        };
        jTable.setFillsViewportHeight(true);
        jTable.getTableHeader().setReorderingAllowed(false);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int col = e.getColumn();
                if(col != WHITE_COMMENT_COL && col != BLACK_COMMENT_COL)
                    return;

                for(int row = e.getFirstRow(); row <= e.getLastRow(); row++) {
                    int ply = ((Integer) tModel.getValueAt(row, PLY_COL) - 1) * 2;
                    if(col == BLACK_COMMENT_COL)
                        ply++;
                    myGame.getMoveData(ply).comment = (String)tModel.getValueAt(row, col);
                }
            }
        });

        TableColumnModel tcModel = jTable.getColumnModel();
        int[] widths = { 32, 64, 64 };
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
                tModel.addRow(new Object[] { moveNum, SAN, "", "" });
            else
                tModel.setValueAt(SAN, moveNum - 1, 2);
        }

        @Override
        public void gameEnded(Game.Result result, Game.Termination termination) {}
        @Override
        public void timeTick(Clock clock) {}
    }

    private boolean isMoveAt(int col, int row) {
        return !tModel.getValueAt(row, col).equals("...");
    }
}
