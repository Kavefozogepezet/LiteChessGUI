package board;

public enum Piece {
    WKing(PieceType.King, Side.White), WQueen(PieceType.Queen, Side.White), WBishop(PieceType.Bishop, Side.White), WKnight(PieceType.Knight, Side.White), WRook(PieceType.Rook, Side.White), WPawn(PieceType.Pawn, Side.White),
    BKing(PieceType.King, Side.Black), BQueen(PieceType.Queen, Side.Black), BBishop(PieceType.Bishop, Side.Black), BKnight(PieceType.Knight, Side.Black), BRook(PieceType.Rook, Side.Black), BPawn(PieceType.Pawn, Side.Black);



    public final PieceType type;
    public final Side side;

    Piece(PieceType t, Side s) {
        type = t;
        side = s;
    }
}