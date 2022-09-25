package board.setup;

import board.Board;
import board.setup.BoardSetup;

public interface ChessNotation extends BoardSetup {
    String get(Board board);
}
