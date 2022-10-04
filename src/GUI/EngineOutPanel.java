package GUI;

import engine.Engine;
import engine.EngineListener;
import engine.SearchInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;

public class EngineOutPanel implements GUICreator {
    private final DefaultTableModel tModel = new DefaultTableModel(SearchInfo.InfoNames, 0);

    private Engine myEngine = null;
    private EngineListener myListener = null;

    private JScrollPane GUIRoot;

    EngineOutPanel() {
        createGUI();
    }

    public void listenToEngine(Engine engine) {
        if(myEngine != null)
            myEngine.removeListener(myListener);

        tModel.setRowCount(0);

        myEngine = engine;
        myListener = new SearchInfoListener();
        myEngine.addListener(myListener);
    }

    @Override
    public Component createGUI() {
        JTable jTable = new JTable(tModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable.setFillsViewportHeight(true);
        jTable.getTableHeader().setReorderingAllowed(false);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumnModel tcModel = jTable.getColumnModel();
        int[] widths = { 64, 64, 64, 64, 64 };
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

    private class SearchInfoListener implements EngineListener {
        private boolean setToClear = false;

        @Override
        public void info(SearchInfo info) {
            if(setToClear) {
                tModel.setRowCount(0);
                setToClear = false;
            }
            tModel.insertRow(0, info.array);
        }

        @Override
        public void bestmove(String s) {
            setToClear = true;
        }
    }
}
