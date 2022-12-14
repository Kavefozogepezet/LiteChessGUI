package me.lcgui.gui;

import me.lcgui.engine.Engine;
import me.lcgui.engine.SearchInfo;
import me.lcgui.game.movegen.Move;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * Engine keresési kimenetét listázó grafikusan megjeleníthető osztály.
 */
public class EngineOutPanel implements GUICreator {
    private final DefaultTableModel tModel = new DefaultTableModel(SearchInfo.InfoNames, 0);

    private Engine myEngine = null;

    private JScrollPane GUIRoot;

    private boolean setToClear = false;

    EngineOutPanel() {
        createGUI();
    }

    /**
     * @param engine Ennek az enginnek a kimenetét fogja listázni az objektum.
     */
    public void listenToEngine(Engine engine) {
        if(myEngine != null) {
            myEngine.removeMoveListener(onBestEvent);
            myEngine.getInfoEvent().removeListener(onInfoEvent);
        }

        tModel.setRowCount(0);

        myEngine = engine;
        myEngine.addMoveListener(onBestEvent);
        myEngine.getInfoEvent().addListener(onInfoEvent);
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

    private final Event.Listener<SearchInfo> onInfoEvent = (SearchInfo info) -> {
        SwingUtilities.invokeLater(() -> {
            if(setToClear) {
                tModel.setRowCount(0);
                setToClear = false;
            }
            tModel.insertRow(0, info.array);
        });
    };

    private final Event.Listener<Consumable<Move>> onBestEvent = (Consumable<Move> c) -> {
        setToClear = true;
    };
}
