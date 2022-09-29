package game.board;

import org.jetbrains.annotations.NotNull;

public class Board extends AbstractBoard {
    private final Piece[][] squares = new Piece[BOARD_SIZE][BOARD_SIZE];

    @Override
    public void setPiece(@NotNull Square square, Piece piece) {
        super.setPiece(square, piece);
        squares[square.rank][square.file] = piece;
    }

    @Override
    public void removePiece(@NotNull Square square) {
        super.removePiece(square);
        squares[square.rank][square.file] = null;
    }

    @Override
    public Piece getPiece(@NotNull Square square) {
        return squares[square.rank][square.file];
    }
}
