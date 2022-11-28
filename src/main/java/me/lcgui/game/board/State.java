package me.lcgui.game.board;

import me.lcgui.game.movegen.Move;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Sakkparti állapotát tároló osztály.
 * Számon tartja a féllépések számát, a következő félt, sáncolási jogokat,
 * en passant cél mezőt, és az 50 lépés szabály számlálót.
 */
public class State implements Serializable {
    public static final int CASTLE_WK = 0b0001;
    public static final int CASTLE_WQ = 0b0010;
    public static final int CASTLE_BK = 0b0100;
    public static final int CASTLE_BQ = 0b1000;

    public static final int CASTLE_W =   CASTLE_WK | CASTLE_WQ;
    public static final int CASTLE_B =   CASTLE_BK | CASTLE_BQ;
    public static final int CASTLE_K =   CASTLE_WK | CASTLE_BK;
    public static final int CASTLE_Q =   CASTLE_WQ | CASTLE_BQ;
    public static final int CASTLE_ALL = CASTLE_W | CASTLE_B;

    private int castlingRights = 0;
    private Square epTarget = Square.invalid;
    private Side turn = Side.White;
    private int ply = 0;
    private int ply50 = 0;

    public State() {}

    public State(Side turn, int castling, Square ep, int ply, int ply50) {
        this.turn = turn;
        this.castlingRights = castling;
        this.epTarget = ep;
        this.ply = ply;
        this.ply50 = ply50;
    }

    /**
     * A lépés alapján frissíti a parti állapotát.
     * @param move A lépés, amit végrehajtott az egyik fél.
     */
    public void movePlayed(Move move) {
        epTarget = Square.invalid;

        if(move.moving == Piece.WKing)
            castlingRights &= ~CASTLE_W;
        else if(move.moving == Piece.BKing)
            castlingRights &= ~CASTLE_B;

        if (castlingRights != 0) {
            if (move.to.equals(Square.h1) || move.from.equals( Square.h1))
                castlingRights &= ~CASTLE_WK;
            else if (move.to.equals(Square.a1) || move.from.equals(Square.a1))
                castlingRights &= ~CASTLE_WQ;;

            if (move.to.equals(Square.h8) || move.from.equals(Square.h8))
                castlingRights &= ~CASTLE_BK;
            else if (move.to.equals(Square.a8) || move.from.equals(Square.a8))
                castlingRights &= ~CASTLE_BQ;
        }

        if(move.is(Move.DOUBLE_PUSH))
            epTarget = Square.between(move.from, move.to);

        turn = turn.other();
        ply++;
        ply50++;

        if(move.moving.type == PieceType.Pawn || move.isCapture())
            ply50 = 0;
    }

    /**
     * Megadja hogy a sáncolási jogok érvényesek-e. Ha csak az egyik sem érvényes, hamissal tér vissza.
     * @param flag A CLASTLE_** flagekből or-al összeállított flag.
     * @return igaz, ha a jogok érvényesek.
     */
    public boolean canCastle(int flag) {
        return (castlingRights & flag) == flag;
    }

    public int getCastleRights() {
        return castlingRights;
    }

    public Square getEpTarget() {
        return epTarget;
    }

    public Side getTurn() {
        return turn;
    }

    public int getPly() {
        return ply;
    }

    public int get50move() {
        return ply50;
    }
}
