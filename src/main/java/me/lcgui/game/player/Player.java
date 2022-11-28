package me.lcgui.game.player;

import me.lcgui.game.Game;
import me.lcgui.game.board.Side;

/**
 * Egy játékost ábrázoló interface.
 */
public interface Player {
    /**
     *  Egy {@link Game} példány hívja meg, ha a játékos sorra kerül.
     */
    void myTurn();

    /**
     *  Egy {@link Game} példány hívja meg, ha a játékos köre valami okból félbe szakadt.
     */
    void cancelTurn();

    /**
     * BEállítja a partit, amit a játékos játszani fog.
     * @param game A parti.
     * @param mySide A játékos színe a partiban.
     */
    void setGame(Game game, Side mySide);

    /**
     * Egy {@link Game} példány hívja meg, hogy tájékoztassa a játékost a parti végéről.
     */
    void gameEnd();

    /**
     * Egy {@link Game} példány hívja meg, hogy tájékoztassa a játékost hogy joga van döntetlent igényelni.
     * Ekkor a játékos meghívhatja a {@link Game#draw()} függvényt.
     */
    void handleDrawClaim();

    /**
     * @return A játékos neve.
     */
    String getName();
}
