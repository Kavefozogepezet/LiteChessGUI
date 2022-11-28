package me.lcgui.gui;

import java.awt.*;

/**
 * Interface grafikusan megjeleníthető osztály létrehozásához.
 */
public interface GUICreator {
    /**
     * Elkészíti az osztály grafikus megjelenítését.
     * @return A megjelenítás alap komponense.
     */
    Component createGUI();

    /**
     * @return A megjelenítás alap komponense.
     *
     */
    Component getRootComponent();
}
