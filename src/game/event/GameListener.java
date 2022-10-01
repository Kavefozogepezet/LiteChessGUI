package game.event;

import game.Clock;
import game.Game;
import game.board.Square;
import game.movegen.Move;

public interface GameListener {
    void movePlayed(Move move);
    void gameEnded(Game.Result result, Game.Termination termination);
    void timeTick(Clock clock);
}
