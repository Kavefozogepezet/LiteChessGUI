package game.board;

public enum PieceType {
    King, Queen, Bishop, Knight, Rook, Pawn, Count;

    @Override
    public String toString() {
        final String[] pieceTable = {"k", "q", "b", "n", "r", "p"};
        return pieceTable[ordinal()];
    }
}
