package game.setup;

import GUI.BoardView;
import game.Game;
import game.board.*;

public class StartPos implements GameSetup {
    @Override
    public void set(Game game) {
        Board board = game.getBoard();

        board.setPiece(4, 0, Piece.WKing);
        board.setPiece(4, 7, Piece.BKing);

        board.setPiece(3, 0, Piece.WQueen);
        board.setPiece(3, 7, Piece.BQueen);

        board.setPiece(0, 0, Piece.WRook);
        board.setPiece(0, 7, Piece.BRook);
        board.setPiece(7, 0, Piece.WRook);
        board.setPiece(7, 7, Piece.BRook);

        board.setPiece(1, 0, Piece.WKnight);
        board.setPiece(1, 7, Piece.BKnight);
        board.setPiece(6, 0, Piece.WKnight);
        board.setPiece(6, 7, Piece.BKnight);

        board.setPiece(2, 0, Piece.WBishop);
        board.setPiece(2, 7, Piece.BBishop);
        board.setPiece(5, 0, Piece.WBishop);
        board.setPiece(5, 7, Piece.BBishop);

        for(int i = 0; i < BoardView.BOARD_SIZE; i++) {
            board.setPiece(i, 1, Piece.WPawn);
            board.setPiece(i, 6, Piece.BPawn);
        }

        game.setState(new State(Side.White, State.CASTLE_ALL, Square.invalid, 0));
    }
}
