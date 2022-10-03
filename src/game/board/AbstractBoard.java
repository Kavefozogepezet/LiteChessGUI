package game.board;

import game.movegen.Move;

public abstract class AbstractBoard {
    public static final int BOARD_SIZE = 8;
    private final Square[] kings = new Square[2];

    public void clear() {
        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                removePiece(file, rank);
            }
        }
        kings[0] = null;
        kings[1] = null;
    }

    public void setPiece(Square square, Piece piece) {
        if(piece != null && piece.type == PieceType.King)
            kings[piece.side.ordinal()] = square;
    }

    public void setPiece(int file, int rank, Piece piece) {
        setPiece(new Square(file, rank), piece);
    }

    public void removePiece(Square square) {
        Piece piece = getPiece(square);
        if(piece != null && piece.type == PieceType.King)
            kings[piece.side.ordinal()] = null;
    }

    public final void removePiece(int file, int rank) {
        removePiece(new Square(file, rank));
    }

    public abstract Piece getPiece(Square square);

    public final Piece getPiece(int file, int rank) {
        return getPiece(new Square(file, rank));
    }

    public final Square getKing(Side side) {
        return kings[side.ordinal()];
    }

    // TODO check detection
    public void play(Move move) {
        removePiece(move.from);

        if(move.isPromotion())
            setPiece(move.to, move.getPromotionPiece());
        else
            setPiece(move.to, move.moving);

        if(move.isCastle()) {
            Square rookSq;
            if(move.moving.side == Side.White)
                rookSq = move.is(Move.CASTLE_K) ? Square.h1 : Square.a1;
            else
                rookSq = move.is(Move.CASTLE_K) ? Square.h8 : Square.a8;

            Piece rook = getPiece(rookSq);
            setPiece(Square.between(move.from, move.to), rook);
            removePiece(rookSq);
        }

        if(move.is(Move.EN_PASSANT))
            removePiece(Square.cross(move.to, move.from));
    }

    // TODO check detection
    public void unplay(Move move) {
        removePiece(move.to);
        setPiece(move.from, move.moving);

        if(move.isCastle()) {
            Square rookSq;
            if(move.moving.side == Side.White)
                rookSq = move.is(Move.CASTLE_K) ? Square.h1 : Square.a1;
            else
                rookSq = move.is(Move.CASTLE_K) ? Square.h8 : Square.a8;

            Piece rook = getPiece(Square.between(move.from, move.to));
            setPiece(rookSq, rook);
            removePiece(Square.between(move.from, move.to));
        }

        if(move.is(Move.EN_PASSANT))
            setPiece(Square.cross(move.to, move.from), move.captured);
        else if(move.isCapture())
            setPiece(move.to, move.captured);
    }
}
