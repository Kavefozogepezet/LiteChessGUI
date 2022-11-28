package me.lcgui.game.movegen;

import me.lcgui.game.Game;
import me.lcgui.game.IllegalMoveException;
import me.lcgui.game.IncorrectNotationException;
import me.lcgui.game.board.Piece;
import me.lcgui.game.board.Square;

import java.io.Serializable;
import java.util.Objects;

/**
 * Egy lépést ábrázoló osztály.
 * A különleges lépéseket flag-ek segítségével tárolja.
 * Ilyenek a gyalog kettős lépése, en passant, előreléptetés, sáncolás.
 * A PROMOTE_* flagek a * helyén a tiszt karakterét kódolják.
 * A CASTLE_* flagek a * helyén a királynő, és a király oldali sáncolást kódolják.
 */
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

    /**
     * Átlagos lépést készít, amely nem vesz le bábut.
     * @param from A lépés kezdő mezője.
     * @param to A lépés cél mezője.
     * @param moving A lépő bábu.
     */
    public Move(Square from, Square to, Piece moving) {
        this.from = from;
        this.to = to;
        this.moving = moving;
        this.captured = null;
        this.flags = 0;
    }

    /**
     * Átlagos lépést készít, amely levesz egy bábut.
     * @param from A lépés kezdő mezője.
     * @param to A lépés cél mezője.
     * @param moving A lépő bábu.
     * @param captured A levett bábu.
     */
    public Move(Square from, Square to, Piece moving, Piece captured) {
        this.from = from;
        this.to = to;
        this.moving = moving;
        this.captured = captured;
        this.flags = 0;
    }

    /**
     * Különleges lépést készít.
     * @param from A lépés kezdő mezője.
     * @param to A lépés cél mezője.
     * @param moving A lépő bábu.
     * @param captured A levett bábu. Ha nem vesz le bábut, legyen null.
     * @param flags A különleges lépést leíró flagek.
     */
    public Move(Square from, Square to, Piece moving, Piece captured, int flags) {
        this.from = from;
        this.to = to;
        this.moving = moving;
        this.captured = captured;
        this.flags = flags;
    }

    /**
     * @return igaz, ha a lépés levesz egy bábut.
     */
    public boolean isCapture() {
        return captured != null;
    }

    /**
     * @return igaz, ha a lépés rendelkezik sáncolás flag-el.
     */
    public boolean isCastle() {
        return (flags & CASTLING) != 0;
    }

    /**
     * @return igaz, ha a lépés rendelkezik előreléptetés flag-el.
     */
    public boolean isPromotion() {
        return (flags & PROMOTION) != 0;
    }

    /**
     * Megadja a lépő bábu színe alapján, hogy milyen tiszt kerüljön a lépés célmezőjére.
     * @return A tiszt.
     */
    public Piece getPromotionPiece() {
        final Piece[][] promPieces = {
            { Piece.WQueen, Piece.WRook, Piece.WKnight, Piece.WBishop },
            { Piece.BQueen, Piece.BRook, Piece.BKnight, Piece.BBishop }
        };

        int sideIdx = moving.side.ordinal();
        int pieceIdx = ((flags & PROMOTION) >> 4) - 1;

        return promPieces[sideIdx][pieceIdx];
    }

    /**
     * @param flag A viszgálni kívánt flagek.
     * @return Igaz, ha a megadott flagek mindegyike be van állítva.
     */
    public boolean is(int flag) {
        return (flags & flag) == flag;
    }

    /**
     * @return Lépés string-je long algebraic notation szerint.
     */
    @Override
    public String toString() {
        String moveStr = from.toString() + to.toString();
        if(isPromotion())
            moveStr += getPromotionPiece().type.toString();
        return moveStr;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Move other) {
            return from.equals(other.from) && to.equals(other.to)
                    && moving.equals(other.moving) && Objects.equals(captured, other.captured)
                    && flags == other.flags;
        }
        return false;
    }
}
