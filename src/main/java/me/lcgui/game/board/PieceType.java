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
}
