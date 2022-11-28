package me.lcgui.game.movegen;

import me.lcgui.game.board.PieceType;
import me.lcgui.game.board.Square;

/**
 * Standard algebraic notation készítéséhez használandó osztály.
 */
public class SANBuilder {
    private final StringBuilder SAN = new StringBuilder();

    private boolean canCheck = true;

    /**
     * Ellátja a SANBUildert az alap informáiókkal, de a sakk/matt állásokat külső segítség nélkül nem ismeri fel.
     * @param move A lépés, amihez SAN-t generálunk.
     * @param movegen A {@link MoveGen} példány amivel a lépést generáltuk.
     *                Azért van erre szükség, mert a SAN csak úgy tehető egyértelmű jelöléssé,
     *                ha ehhez ismerjük a többi legális lépést is.
     */
    public SANBuilder(Move move, MoveGen movegen) {
        if(move.isCastle()) {
            canCheck = false;
            if (move.is(Move.CASTLE_K))
                SAN.append("O-O");
            else
                SAN.append("O-O-O");
            return;
        }

        SAN.append(move.to.toString());

        if(move.isCapture())
            SAN.insert(0, 'x');

        if(move.moving.type == PieceType.Pawn) {
            if(move.isCapture())
                SAN.insert(0, Square.file2char(move.from.file));
            if(move.isPromotion()) {
                char p = move.getPromotionPiece().type.toChar();
                p = Character.toUpperCase(p);
                SAN.append('=').append(p);
            }
        } else if(move.moving.type == PieceType.King) {
            canCheck = false;
            SAN.insert(0, PieceType.King.toString().toUpperCase());
            return;
        } else {
            String pieceStr = move.moving.toString().toUpperCase();
            boolean
                    useFileNotation = false,
                    useRankNotation = false;

            for(var sameTo : movegen.to(move.to)) {
                if(sameTo.moving != move.moving || sameTo.from.equals(move.from))
                    continue;
                else if(sameTo.from.file != move.from.file)
                    useFileNotation = true;
                else
                    useRankNotation = true;
            }

            if(useRankNotation)
                SAN.insert(0, Square.rank2char(move.from.rank));
            if(useFileNotation)
                SAN.insert(0, Square.file2char(move.from.file));
            SAN.insert(0, pieceStr);
        }
    }

    /**
     * A SAN-t úgy generálja majd, hogy az tükrözze hogy a lépés sakk állást eredményezett.
     */
    public void check() {
        if(canCheck)
            SAN.append('+');
    }

    /**
     * A SAN-t úgy generálja majd, hogy az tükrözze hogy a lépés matt állást eredményezett.
     */
    public void mate() {
        if(canCheck)
            SAN.append('#');
    }

    /**
     * @return Megadja a SAN jelölést.
     */
    @Override
    public String toString() {
        return SAN.toString();
    }
}
