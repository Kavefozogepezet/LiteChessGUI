package board.setup;

import board.Board;
import board.types.Piece;
import board.types.Side;
import board.types.Square;

public class StartPos implements BoardSetup {
    @Override
    public void set(Board board) {
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

        for(int i = 0; i < Board.BOARD_SIZE; i++) {
            board.setPiece(i, 1, Piece.WPawn);
            board.setPiece(i, 6, Piece.BPawn);
        }

        board.setState(new Board.State(Side.White, Board.State.CASTLE_ALL, Square.invalid, 0));
    }
}
