package me.lcgui.gui;

import me.lcgui.game.Game;
import me.lcgui.misc.Event;
import me.lcgui.misc.MathExt;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.LinkedList;

public class MoveListPanel implements GUICreator {
    private final MoveModel tModel = new MoveModel();
    private Game myGame;
    private JScrollPane GUIRoot;

    private int deltaPly = 0;

    public MoveListPanel() {
        createGUI();
    }

    public void followGame(Game game) {
        if(myGame != null)
            myGame.moveEvent.removeListener(onMovePlayed);

        tModel.clear();
        deltaPly = 0;
        myGame = game;
        myGame.moveEvent.addListener(onMovePlayed);

        for(var moveData : myGame.getMoveList())
            tModel.addMove(moveData);
    }

    @Override
    public Component createGUI() {
        JTable jTable = new JTable(tModel);
        jTable.setFillsViewportHeight(true);
        jTable.getTableHeader().setReorderingAllowed(false);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

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

    private final Event.Listener<Game.MoveData> onMovePlayed = (Game.MoveData moveData) -> {
        SwingUtilities.invokeLater(() -> {
            tModel.addMove(moveData);
            //addMove(moveData);
        });
    };

    private boolean isMoveAt(int col, int row) {
        return !tModel.getValueAt(row, col).equals("...");
    }

    private static class MoveModel extends AbstractTableModel {
        private static final int
                SERIAL_COLUMN = 0,
                WHITE_MOVE_COLUMN = 1,
                BLACK_MOVE_COLUMN = 2,
                WHITE_COMMENT_COLUMN = 3,
                BLACK_COMMENT_COLUMN = 4,

                COLUMN_COUNT = 5;

        private static final String[] columnNames = new String[] { "", "white", "black", "white's comment", "black's comment" };

        LinkedList<Round> moveList = new LinkedList<>();

        public void addMove(Game.MoveData moveData) {
            if(moveData.move.moving.isWhite()) {
                int serial = moveData.ply / 2 + 1;
                Round r = new Round(serial);
                r.moves[0] = moveData;
                moveList.add(r);
                fireTableRowsUpdated(moveList.size() - 1, moveList.size() - 1);
            } else {
                moveList.getLast().moves[1] = moveData;
                fireTableRowsInserted(moveList.size() - 1, moveList.size() - 1);
            }
        }

        public void clear() {
            if(!moveList.isEmpty()) {
                int last = moveList.size() - 1;
                moveList.clear();
                fireTableRowsDeleted(0, last);
            }
        }

        @Override
        public int getRowCount() {
            return moveList.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        @Override
        public String getColumnName(int column) {
            int idx = MathExt.clamp(column, 0, columnNames.length);
            return columnNames[idx];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Round r = moveList.get(rowIndex);
            return switch (columnIndex) {
                case SERIAL_COLUMN -> Integer.toString(r.serial) + ".";
                case WHITE_MOVE_COLUMN -> r.moves[0] == null ? "" : r.moves[0].SAN;
                case BLACK_MOVE_COLUMN -> r.moves[1] == null ? "" : r.moves[1].SAN;
                case WHITE_COMMENT_COLUMN -> r.moves[0] == null ? "" : r.moves[0].comment;
                default -> r.moves[1] == null ? "" : r.moves[1].comment;
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Round r = moveList.get(rowIndex);
            if(columnIndex == WHITE_COMMENT_COLUMN)
                r.moves[0].comment = (String) aValue;
            else if(columnIndex == BLACK_COMMENT_COLUMN)
                r.moves[1].comment = (String) aValue;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == WHITE_COMMENT_COLUMN || columnIndex == BLACK_COMMENT_COLUMN;
        }

        private static class Round {
            public final int serial;
            public final Game.MoveData[] moves = { null, null };

            private Round(int serial) {
                this.serial = serial;
            }
        }
    }
}
