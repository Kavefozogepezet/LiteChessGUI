package me.lcgui.game.setup;

import me.lcgui.game.Game;
import me.lcgui.game.IncorrectNotationException;

/**
 * Interface játék kezdőpozíciójának beállítására használt osztály készítésére.
 */
public interface GameSetup {
    /**
     * BEállítja a megadott parti kezdőpozícióját az implementált jelölés szerint.
     * @param game A parti.
     * @throws IncorrectNotationException A jelölés helytelen volt.
     */
    void set(Game game) throws IncorrectNotationException;
}

