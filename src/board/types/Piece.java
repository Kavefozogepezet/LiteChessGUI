package board.types;

import java.util.HashMap;

public enum Piece {
    WKing(PieceType.King, Side.White), WQueen(PieceType.Queen, Side.White), WBishop(PieceType.Bishop, Side.White), WKnight(PieceType.Knight, Side.White), WRook(PieceType.Rook, Side.White), WPawn(PieceType.Pawn, Side.White),
    BKing(PieceType.King, Side.Black), BQueen(PieceType.Queen, Side.Black), BBishop(PieceType.Bishop, Side.Black), BKnight(PieceType.Knight, Side.Black), BRook(PieceType.Rook, Side.Black), BPawn(PieceType.Pawn, Side.Black);



    public final PieceType type;
    public final Side side;

    Piece(PieceType t, Side s) {
        type = t;
        side = s;
    }

    public boolean isWhite() {
        return side == Side.White;
    }
    public static Piece fromChar(char ch) {
        final Piece[][] pieceTable = {
                { WKing, WQueen, WBishop, WKnight, WRook, WPawn },
                { BKing, BQueen, BBishop, BKnight, BRook, BPawn }
        };
        int sIdx = Character.isUpperCase(ch) ? 0 : 1;
        int pIdx = 0;
        switch (Character.toLowerCase(ch)) {
            case 'q' -> pIdx = 1;
            case 'b' -> pIdx = 2;
            case 'n' -> pIdx = 3;
            case 'r' -> pIdx = 4;
            case 'p' -> pIdx = 5;
        }
        return pieceTable[sIdx][pIdx];
    }

    @Override
    public String toString() {
        final String[] pieceTable = { "k", "q", "b", "n", "r", "p" };

        String pieceStr = pieceTable[type.ordinal()];
        if(isWhite())
            pieceStr = pieceStr.toUpperCase();

        return pieceStr;
    }
}