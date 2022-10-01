package player;

import game.Game;
import game.movegen.Move;

public interface Player {
    void myTurn();
    void cancelTurn();
    void bind(Game game);
}
