package me.lcgui.engine;

/**
 * Engine és GUI közötti komunikációt ábrázoló osztály.
 */
public class ComData {
    /**
     * igaz, ha az üzenetet az engine küldte, hamis ha a GUI za engine felé.
     */
    public final boolean isInput;

    /**
     * A szöveg, amit küldtek.
     */
    public final String line;

    public ComData(boolean isInput, String line) {
        this.isInput = isInput;
        this.line = line;
    }
}
