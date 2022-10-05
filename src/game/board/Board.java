package game.board;

import java.io.Serializable;

public class Board extends AbstractBoard implements Serializable {
    private final Piece[][] squares = new Piece[BOARD_SIZE][BOARD_SIZE];

    @Override
    public void setPiece(Square square, Piece piece) {
        super.setPiece(square, piece);
        squares[square.rank][square.file] = piece;
    }

    @Override
    public void removePiece(Square square) {
        super.removePiece(square);
        squares[square.rank][square.file] = null;
    }

    @Override
    public Piece getPiece(Square square) {
        return squares[square.rank][square.file];
    }
}
