package me.lcgui.game.setup;

import me.lcgui.game.Game;
import me.lcgui.game.IncorrectNotationException;

public interface GameSetup {
    void set(Game game) throws IncorrectNotationException;
}

