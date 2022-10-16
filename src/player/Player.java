package player;

import game.Game;
import game.board.Side;

public interface Player {
    void myTurn();
    void cancelTurn();
    void setGame(Game game, Side mySide);
    void gameEnd();

    String getName();
}
