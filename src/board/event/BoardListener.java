package board.event;

import board.Board;

public interface BoardListener {
    void newGameStarted(Board board);
    void movePlayed(Board board);
    void moveUnplayed(Board board);
    void gameEnded(Board board);
}
