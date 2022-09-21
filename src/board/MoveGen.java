package board;

import java.util.LinkedList;
import java.util.Map;

public class MoveGen {
    private static final int[][] directions = {
            { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 }
    };
    private static final int[][] knight = {
            { 1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { -1, -2 }, { -2, -1 }, { -2, 1 }, { -1, 2 }
    };

    Board board;

    Map<Square, LinkedList<Move>> moves;

    public MoveGen(Board board) {
        this.board = board;
        fillAllMoves();
    }

    private void fillPawnMoves(Square origin) {

    }

    private void fillKnightMoves(Square origin) {

    }

    private void fillKingMoves(Square origin) {

    }

    private void fillSlidingMoves(Square origin) {

    }

    private void fillMoves(Square sq) {
        Piece moving = board.getPiece(sq);

        switch(moving.type) {
            case King -> fillKingMoves(sq);
            case Knight -> fillKnightMoves(sq);
            case Pawn -> fillPawnMoves(sq);
            case Queen, Bishop, Rook -> fillSlidingMoves(sq);
        }
    }

    private void fillAllMoves() {
        for(int rank = 0; rank < Board.BOARD_SIZE; rank++) {
            for(int file = 0; file < Board.BOARD_SIZE; file++) {
                Square sq = new Square(file, rank);
                Piece moving = board.getPiece(sq);

                if(moving == null)
                    return;

                fillMoves(sq);
            }
        }
    }

    public LinkedList<Move> getMoves(Square origin) {
        if(!moves.containsKey(origin))
            return null;
        return moves.get(origin);
    }
}
