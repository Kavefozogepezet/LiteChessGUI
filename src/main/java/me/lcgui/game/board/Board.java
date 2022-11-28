package me.lcgui.game.board;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Egy parti aktuális állását tároló sakktábla.
 */
public class Board extends AbstractBoard implements Serializable {
    private final Piece[][] squares = new Piece[BOARD_SIZE][BOARD_SIZE];
    private final int[][] material = new int[Side.Count.ordinal()][PieceType.Count.ordinal()];
    private final int[] bishops = new int[2];
    private final char[] charArray = new char[64];

    /**
     * Beállítja a bábut, e mellett frissíti a tábla karakteres reprezentációját,
     * material count-ot és a világos/sötét mezőn álló futók számát.
     * @param square A mező, amin a bábu állni fog.
     * @param piece A bábu.
     */
    @Override
    public void setPiece(Square square, Piece piece) {
        if(!square.valid())
            throw new IllegalArgumentException("Square was invalid");

        super.setPiece(square, piece);
        squares[square.rank][square.file] = piece;
        charArray[square.linearIdx()] = piece.toChar();

        material[piece.side.ordinal()][piece.type.ordinal()]++;
        if(piece.type == PieceType.Bishop)
            bishops[square.isLight() ? 0 : 1]++;
    }

    /**
     * Eltávolítja a mezőről az ott álló bábut, e mellett frissíti a tábla karakteres reprezentációját,
     * material count-ot és a világos/sötét mezőn álló futók számát.
     * @param square A mező,
     */
    @Override
    public void removePiece(Square square) {
        if(!square.valid())
            throw new IllegalArgumentException("Square was invalid.");

        super.removePiece(square);
        Piece piece = squares[square.rank][square.file];
        squares[square.rank][square.file] = null;
        charArray[square.linearIdx()] = '\0';

        if(piece != null) {
            material[piece.side.ordinal()][piece.type.ordinal()]--;
            if (piece.type == PieceType.Bishop)
                bishops[square.isLight() ? 0 : 1]--;
        }
    }

    @Override
    public Piece getPiece(Square square) {
        if(!square.valid())
            throw new IllegalArgumentException("Square was invalid.");
        return squares[square.rank][square.file];
    }

    /**
     * Megadja, hogy az egyik félnek hány darab megadott típusú bábuja van a táblán.
     * @param side A fél, akinek a bábuit lekérdezzük.
     * @param pieceType A bábu típusa.
     * @return A kért bábuk száma a táblán.
     */
    public int getMaterial(Side side, PieceType pieceType) {
        return material[side.ordinal()][pieceType.ordinal()];
    }

    public int getMaterial(PieceType pieceType) {
        return getMaterial(Side.White, pieceType)
                + getMaterial(Side.Black, pieceType);
    }

    /**
     * A világos mezőn álló futók számát adja meg, függetlenül a futó színétől.
     * @return A futók száma.
     */
    public int getLightBishops() {
        return bishops[0];
    }

    /**
     * A sötét mezőn álló futók számát adja meg, függetlenül a futó színétől.
     * @return A futók száma.
     */
    public int getDarkBishops() {
        return bishops[1];
    }

    /**
     * Megadja a számontartott szöveges reprezentációt.
     * Önmagában nincs értelme, a pozíciók összehasonlítására használandó.
     * @return A szöveges reprezentáció.
     */
    @Override
    public String toString() {
        return String.valueOf(charArray);
    }
}
