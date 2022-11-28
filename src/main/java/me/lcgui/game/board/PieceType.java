package me.lcgui.game.board;

/**
 * Bábu típust leíró enum.
 */
public enum PieceType {
    King, Queen, Bishop, Knight, Rook, Pawn, Count;

    /**
     * {@link PieceType#toChar()} eredménye string-ként.
     * @return
     */
    @Override
    public String toString() {
        return "" + toChar();
    }

    /**
     * Karakterré alakítja a bábu típust.
     * Király -> k; Királynő -> q, futó -> b; huszár -> n, bástya -> r, gyalog -> p
     * @return A konvertált karakter.
     */
    public char toChar() {
        final char[] pieceTable = { 'k', 'q', 'b', 'n', 'r', 'p' };
        return pieceTable[ordinal()];
    }

    /**
     * Megadja a bábu típust, amit a karakter jelöl. A FEN string-ben használttal azonos jelölést konvertál.
     * k -> Király; q -> Királynő, b -> futó; n -> huszár, r -> bástya, p -> gyalog
     * @param ch A konvertálandó karakter.
     * @return A konvertált bábu típus. Null, ha érvénytelen volt a karakter.
     */
    public static PieceType fromChar(char ch) {
        ch = Character.toLowerCase(ch);
        PieceType pt = null;
        switch (Character.toLowerCase(ch)) {
            case 'k' -> pt = PieceType.King;
            case 'q' -> pt = PieceType.Queen;
            case 'b' -> pt = PieceType.Bishop;
            case 'n' -> pt = PieceType.Knight;
            case 'r' -> pt = PieceType.Rook;
            case 'p' -> pt = PieceType.Pawn;
        }
        return pt;
    }
}
