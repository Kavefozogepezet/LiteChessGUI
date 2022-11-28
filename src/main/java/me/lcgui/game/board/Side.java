package me.lcgui.game.board;

/**
 * Világos és sötét feleket jelölő enum.
 */
public enum Side {
    White, Black, Count;

    public Side other() {
        return this == White ? Black : White;
    }
}
