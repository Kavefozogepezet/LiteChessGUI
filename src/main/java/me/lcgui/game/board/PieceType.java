package me.lcgui.game.board;

public enum PieceType {
    King, Queen, Bishop, Knight, Rook, Pawn, Count;

    @Override
    public String toString() {
        return "" + toChar();
    }

    public char toChar() {
        final char[] pieceTable = { 'k', 'q', 'b', 'n', 'r', 'p' };
        return pieceTable[ordinal()];
    }

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
