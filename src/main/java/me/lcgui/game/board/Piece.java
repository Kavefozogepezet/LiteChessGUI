package me.lcgui.game.board;

/**
 * A sakkbábukat ábrázoló enum, külön tárolja a bábu típusára és színére vonatkozó információt.
 * A bábu színét az elnevezés elején a W/B betű adja, a típusát pedig a bábu angol neve.
 */
public enum Piece {
    WKing(PieceType.King, Side.White), WQueen(PieceType.Queen, Side.White), WBishop(PieceType.Bishop, Side.White), WKnight(PieceType.Knight, Side.White), WRook(PieceType.Rook, Side.White), WPawn(PieceType.Pawn, Side.White),
    BKing(PieceType.King, Side.Black), BQueen(PieceType.Queen, Side.Black), BBishop(PieceType.Bishop, Side.Black), BKnight(PieceType.Knight, Side.Black), BRook(PieceType.Rook, Side.Black), BPawn(PieceType.Pawn, Side.Black);



    public final PieceType type;
    public final Side side;

    Piece(PieceType t, Side s) {
        type = t;
        side = s;
    }

    /**
     * @return igaz, ha a bábu a világoshoz tartozik.
     */
    public boolean isWhite() {
        return side == Side.White;
    }

    /**
     * Megadja a bábut, amit a karakter jelöl. A FEN string-ben használttal azonos jelölést konvertál.
     * k -> Király; q -> Királynő, b -> futó; n -> huszár, r -> bástya, p -> gyalog
     * A nagybetű jelöli a világos, a kisbető a sötét oldalt.
     * (pl.: K -> WKing; k -> BKing)
     * @param ch A konvertálandó karakter.
     * @return A konvertált bábu. Null, ha érvénytelen volt a karakter.
     */
    public static Piece fromChar(char ch) {
        final Piece[][] pieceTable = {
                { WKing, WQueen, WBishop, WKnight, WRook, WPawn, null },
                { BKing, BQueen, BBishop, BKnight, BRook, BPawn, null }
        };
        int sIdx = Character.isUpperCase(ch) ? 0 : 1;
        int pIdx = 6;
        switch (Character.toLowerCase(ch)) {
            case 'k' -> pIdx = 0;
            case 'q' -> pIdx = 1;
            case 'b' -> pIdx = 2;
            case 'n' -> pIdx = 3;
            case 'r' -> pIdx = 4;
            case 'p' -> pIdx = 5;
        }
        return pieceTable[sIdx][pIdx];
    }

    /**
     * @return {@link Piece#toChar()} eredménye string-ként.
     */
    @Override
    public String toString() {
        return "" + toChar();
    }

    /**
     * Karakterré alakítja a bábut.
     * Király -> k; Királynő -> q, futó -> b; huszár -> n, bástya -> r, gyalog -> p
     * A nagybetű jelöli a világos, a kisbető a sötét oldalt.
     * (pl.: WKing -> K; BKing -> k)
     * @return A konvertált karakter.
     */
    public char toChar() {
        char pieceChar = type.toChar();
        if(isWhite())
            pieceChar = Character.toUpperCase(pieceChar);

        return pieceChar;
    }
}