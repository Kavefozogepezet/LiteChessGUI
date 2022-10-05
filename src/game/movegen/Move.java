package game.movegen;

import game.board.*;

import java.io.Serializable;

public class Move implements Serializable {
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
    public static final int CASTLING =    CASTLE_K | CASTLE_Q;
    public static final int PROMOTION =   PROMOTE_Q | PROMOTE_R | PROMOTE_N | PROMOTE_B;

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

    public Move(Square from, Square to, Piece moving, Piece captured) {
        this.from = from;
        this.to = to;
        this.moving = moving;
        this.captured = captured;
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
        final Piece[][] promPieces = {
            { Piece.WQueen, Piece.WRook, Piece.WKnight, Piece.WBishop },
            { Piece.BQueen, Piece.BRook, Piece.BKnight, Piece.BBishop }
        };

        int sideIdx = moving.side.ordinal();
        int pieceIdx = ((flags & PROMOTION) >> 4) - 1;

        return promPieces[sideIdx][pieceIdx];
    }

    public boolean is(int flag) {
        return (flags & flag) != 0;
    }

    @Override
    public String toString() {
        String moveStr = from.toString() + to.toString();
        if(isPromotion())
            moveStr += getPromotionPiece().type.toString();
        return moveStr;
    }
}
