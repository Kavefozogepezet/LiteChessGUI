package player;

import game.Game;

public interface Player {
    void myTurn();
    void cancelTurn();
    void setGame(Game game);

    String getName();
}
