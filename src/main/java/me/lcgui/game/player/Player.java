package me.lcgui.game.player;

import me.lcgui.game.Game;
import me.lcgui.game.board.Side;

public interface Player {
    void myTurn();
    void cancelTurn();
    void setGame(Game game, Side mySide);
    void gameEnd();
    void handleDrawClaim();
    String getName();
}
