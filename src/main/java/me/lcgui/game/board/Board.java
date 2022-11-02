package me.lcgui.game.board;

import java.io.Serializable;
import java.util.Arrays;

public class Board extends AbstractBoard implements Serializable {
    private final Piece[][] squares = new Piece[BOARD_SIZE][BOARD_SIZE];
    private final int[][] material = new int[Side.Count.ordinal()][PieceType.Count.ordinal()];
    private final int[] bishops = new int[2];
    private final char[] charArray = new char[64];

    @Override
    public void setPiece(Square square, Piece piece) {
        super.setPiece(square, piece);
        squares[square.rank][square.file] = piece;
        charArray[square.linearIdx()] = piece.toChar();

        material[piece.side.ordinal()][piece.type.ordinal()]++;
        if(piece.type == PieceType.Bishop)
            bishops[square.isLight() ? 0 : 1]++;
    }

    @Override
    public void removePiece(Square square) {
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
        return squares[square.rank][square.file];
    }

    public int getMaterial(Side side, PieceType pieceType) {
        return material[side.ordinal()][pieceType.ordinal()];
    }

    public int getMaterial(PieceType pieceType) {
        return getMaterial(Side.White, pieceType)
                + getMaterial(Side.Black, pieceType);
    }

    public int getLightBishops() {
        return bishops[0];
    }

    public int getDarkBishops() {
        return bishops[1];
    }

    @Override
    public String toString() {
        return String.valueOf(charArray);
    }
}
