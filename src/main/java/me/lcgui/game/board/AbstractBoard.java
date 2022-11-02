package me.lcgui.game.board;

import me.lcgui.game.movegen.Move;
import me.lcgui.game.movegen.MoveGen;

import java.util.ArrayList;

public abstract class AbstractBoard {
    public static final int BOARD_SIZE = 8;
    private final Square[] kings = new Square[2];
    private final boolean[] checks = { false, false };

    public void clear() {
        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                removePiece(file, rank);
            }
        }
        kings[0] = null;
        kings[1] = null;
    }

    public void setPiece(Square square, Piece piece) {
        if(piece != null && piece.type == PieceType.King)
            kings[piece.side.ordinal()] = square;
    }

    public void setPiece(int file, int rank, Piece piece) {
        setPiece(new Square(file, rank), piece);
    }

    public void removePiece(Square square) {
        Piece piece = getPiece(square);
        if(piece != null && piece.type == PieceType.King)
            kings[piece.side.ordinal()] = null;
    }

    public final void removePiece(int file, int rank) {
        removePiece(new Square(file, rank));
    }

    public abstract Piece getPiece(Square square);

    public final Piece getPiece(int file, int rank) {
        return getPiece(new Square(file, rank));
    }

    public final Square getKing(Side side) {
        return kings[side.ordinal()];
    }

    // TODO check detection
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

    // TODO check detection
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

    public boolean isCheck(Side side) {
        return checks[side.ordinal()];
    }

    private void updateChecks() {
        checks[Side.White.ordinal()] = isSqAttacked(kings[Side.White.ordinal()], Side.Black);
        checks[Side.Black.ordinal()] = isSqAttacked(kings[Side.Black.ordinal()], Side.White);
    }

    private boolean isSqAttacked(Square sq, Side attacker) {
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
}
