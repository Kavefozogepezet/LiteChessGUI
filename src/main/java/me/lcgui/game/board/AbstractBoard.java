package me.lcgui.game.board;

import me.lcgui.game.movegen.Move;
import me.lcgui.game.movegen.MoveGen;

import java.util.ArrayList;

/**
 * Sakktáblát ábrázoló abstrakt osztály, mely az általános funkcionalitást
 * (mint a bábuk mozgatása, lépések értelmezése, és sakkállás észlelése)
 * valósítja meg, de a tábla tárolásának implementálását a leszármazottakra hagyja.
 * Így a leszármazottak saját struktúrában tárolhatják a táblát,
 * miközben az AbstractBoard felé csak a lényeges információ látszik.
 */
public abstract class AbstractBoard {
    public static final int BOARD_SIZE = 8;

    private final Square[] kings = new Square[2];
    private final boolean[] checks = { false, false };

    /**
     * Törli a tábla összes bábuját.
     */
    public void clear() {
        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                removePiece(file, rank);
            }
        }
        kings[0] = null;
        kings[1] = null;
    }

    /**
     * A megadott mezőre kell helyeznie egy bábut.
     * Az implementáció nem teljes, mivel ez az osztály nem tárolja a táblát, az a leszármazottak feladata. A királyok mezőit frissíti.
     * Leszármazott osztályban felülírandó, de a felülírt függvény köteles meghívni a super-t.
     * @param square A mező, amin a bábu állni fog.
     * @param piece A bábu.
     */
    public void setPiece(Square square, Piece piece) {
        if(square.valid() && piece != null && piece.type == PieceType.King)
            kings[piece.side.ordinal()] = square;
    }

    /**
     * {@link AbstractBoard#setPiece(Square, Piece)}
     * @param file Az oszlop indexe, amin a bábu állni fog.
     * @param rank A sor indexe, amin a bábu állni fog.
     * @param piece A bábu.
     */
    public void setPiece(int file, int rank, Piece piece) {
        setPiece(new Square(file, rank), piece);
    }

    /**
     * A megadott mezőről el kell távolítania az ott álló bábut.
     * Az implementáció nem teljes, mivel ez az osztály nem tárolja a táblát, az a leszármazottak feladata. A királyok mezőit frissíti.
     * Leszármazott osztályban felülírandó, de a felülírt függvény köteles meghívni a super-t.
     * @param square A mező,
     */
    public void removePiece(Square square) {
        Piece piece = getPiece(square);
        if(piece != null && piece.type == PieceType.King)
            kings[piece.side.ordinal()] = null;
    }

    /**
     * {@link AbstractBoard#removePiece(Square)}
     * @param file A mező oszlopának indexe.
     * @param rank A mező sorának indexe.
     */
    public final void removePiece(int file, int rank) {
        removePiece(new Square(file, rank));
    }

    /**
     * Meg kell adnia a kért mezőn álló bábut. Ha nem áll ott egyik bábu sem, null-al térjen vissza.
     * @param square A mező.
     * @return A bábu, vagy null.
     */
    public abstract Piece getPiece(Square square);

    /**
     * {@link AbstractBoard#getPiece(Square)}
     * @param file A mező oszlopának indexe.
     * @param rank A mező sorának indexe.
     * @return A bábu, vagy null.
     */
    public final Piece getPiece(int file, int rank) {
        return getPiece(new Square(file, rank));
    }

    /**
     * Megadja egy adott oldal királyának pozícióját a táblán.
     * @param side Az oldal, aminek a királyát lekérjük.
     * @return A király mezője, vagy invalid mező ha nincs király a táblán.
     */
    public final Square getKing(Side side) {
        return kings[side.ordinal()];
    }

    /**
     * Végrehajtja a megadott lépést a sakktáblán.
     * @param move A végrehajtandó lépés.
     */
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

        updateChecks();
    }

    /**
     * Visszalépteti a bábukat.
     * Csak a legutoljára tett lépéssel hívható meg.
     * @param move A legutolsó lépés, amit a táblának adtunk.
     */
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

        updateChecks();
    }

    /**
     * Megadja, hogy hogy a táblán van-e sakkállás.
     * @param side A fél, aminek az állását le szeretnénk kérdezni.
     * @return igaz, ha az adott fél sakkban van.
     */
    public boolean isCheck(Side side) {
        return checks[side.ordinal()];
    }

    /**
     * Megadja, hogy egy fél támadja-e a megadott mezőt.
     * @param sq A mező, amiről információt kérünk.
     * @param attacker A fél, akit a támadónak tekintünk.
     * @return igaz, ha támadják a mezőt.
     */
    public boolean isSqAttacked(Square sq, Side attacker) {
        if(sq == null)
            return false;

        for(var delta : MoveGen.knightMoves) {
            Square to = sq.shift(delta[0], delta[1]);
            if(to.valid()) {
                Piece p = getPiece(to);
                if (p != null && p.type == PieceType.Knight && p.side == attacker)
                    return true;
            }
        }

        PieceType[] validAttacker = new PieceType[] { PieceType.Rook, PieceType.Bishop };
        for(int i = 0; i < MoveGen.directions.length; i++) {
            var dir = MoveGen.directions[i];
            for(Square to = sq.shift(dir[0], dir[1]); to.valid(); to = to.shift(dir[0], dir[1])) {
                Piece p = getPiece(to);
                int pIdx = i % 2;
                if(p != null && p.side == attacker && (p.type == PieceType.Queen || p.type == validAttacker[pIdx]))
                    return true;
                else if(p != null)
                    break;
            }
        }

        int dRank = attacker == Side.White ? -1 : 1;
        Square[] pawnCaptures = { sq.shift(1, dRank), sq.shift(-1, dRank) };
        for(var to : pawnCaptures) {
            if(to.valid()) {
                Piece p = getPiece(to);
                if (p != null && p.side == attacker && p.type == PieceType.Pawn)
                    return true;
            }
        }

        return false;
    }

    private void updateChecks() {
        checks[Side.White.ordinal()] = isSqAttacked(kings[Side.White.ordinal()], Side.Black);
        checks[Side.Black.ordinal()] = isSqAttacked(kings[Side.Black.ordinal()], Side.White);
    }
}
