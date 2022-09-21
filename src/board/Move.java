package board;

public class Move {
    // Flags
    public static final int DOUBLE_PUSH = 0b00000001;
    public static final int EN_PASSANT =  0b00000010;
    public static final int CASTLE_K =    0b00000100;
    public static final int CASTLE_Q =    0b00001000;
    public static final int PROMOTE_Q =   0b00010000;
    public static final int PROMOTE_R =   0b00100000;
    public static final int PROMOTE_N =   0b00110000;
    public static final int PROMOTE_B =   0b01000000;

    // Flag groups
    public static final int CASTLING =    0b00001100;
    public static final int PROMOTION =   0b11110000;

    public final Square from, to;
    public final Piece moving;
    public final Piece captured;
    private final int flags;

    public Move(Square from, Square to, Piece moving) {
        this.from = from;
        this.to = to;
        this.moving = moving;
        this.captured = null;
        this.flags = 0;
    }

    public Move(Square from, Square to, Piece moving, Piece captured, int flags) {
        this.from = from;
        this.to = to;
        this.moving = moving;
        this.captured = captured;
        this.flags = flags;
    }

    public boolean isCapture() {
        return captured != null;
    }

    public boolean isCastle() {
        return (flags & CASTLING) != 0;
    }

    public boolean isPromotion() {
        return (flags & PROMOTION) != 0;
    }

    public Piece getPromotionPiece() {
        final Piece promPieces[][] = {
            { Piece.WQueen, Piece.WRook, Piece.WKnight, Piece.WBishop },
            { Piece.BQueen, Piece.BRook, Piece.BKnight, Piece.BBishop }
        };

        int sideIdx = moving.side.ordinal();
        int pieceIdx = (flags & PROMOTION) >> 4 - 1;

        return promPieces[sideIdx][pieceIdx];
    }

    public boolean is(int flag) {
        return (flags & flag) != 0;
    }
}
