package game.movegen;

import game.Game;
import game.board.*;

import java.util.HashMap;
import java.util.LinkedList;

public class MoveGen {
    private static final int[][] directions = {
            { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 }
    };

    private static final int[][] knightMoves = {
            { 1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { -1, -2 }, { -2, -1 }, { -2, 1 }, { -1, 2 }
    };

    private final Game game;

    private final BitBoard
            checkBoard = new BitBoard(),
            attackBoard = new BitBoard(),
            uniPinBoard = new BitBoard();
    private final BitBoard[] pinBoards = new BitBoard[8];

    private boolean check, doublecheck;

    HashMap<Square, LinkedList<Move>> moves = new HashMap<>();

    public MoveGen(Game game) {
        this.game = game;

        for(int i = 0; i < pinBoards.length; i++)
            pinBoards[i] = new BitBoard();
    }

    public void generate() {
        moves.clear();
        fillInfo();
        fillMoves();
    }

    public boolean isCheck() {
        return check;
    }

    // ---------- INFO ----------

    private void fillPawnInfo(Square origin) {
        Piece pawn = game.getBoard().getPiece(origin);
        int dRank = pawn.isWhite() ? 1 : -1;

        Square[] captSq = { origin.shift(1, dRank), origin.shift(-1, dRank) };
        for(var sq : captSq) {
            if(sq.valid())
                attackBoard.set(sq);
        }
    }

    private void fillKingInfo(Square origin) {
        for(var dir : directions) {
            Square to = origin.shift(dir[0], dir[1]);
            if(to.valid())
                attackBoard.set(to);
        }
    }

    private  void fillKnightInfo(Square origin) {
        for(var knightMove : knightMoves) {
            Square sq = origin.shift(knightMove[0], knightMove[1]);
            if(sq.valid())
                attackBoard.set(sq);
        }
    }

    private void fillSlidingInfo(Square origin) {
        Piece sliding = game.getBoard().getPiece(origin);
        int dIdx = sliding.type == PieceType.Queen ? 1 : 2;
        int startIdx = sliding.type == PieceType.Bishop ? 1 : 0;

        for(int i = startIdx; i < directions.length; i += dIdx) {
            var dir = directions[i];
            for(Square sq = origin.shift(dir[0], dir[1]); sq.valid(); sq = sq.shift(dir[0], dir[1])) {
                Piece capt = game.getBoard().getPiece(sq);
                attackBoard.set(sq);
                if(capt != null && !(capt.type == PieceType.King && capt.side != sliding.side))
                    break;
            }
        }
    }

    private void fillInfo() {
        attackBoard.bits = 0;
        checkBoard.bits = 0;
        for(var pb : pinBoards)
            pb.bits = 0;
        uniPinBoard.bits = 0;
        check = false;
        doublecheck = false;

        Side side = game.getState().getTurn();
        Side attacker = side.other();
        Square king = null;

        // Attack game.game.getBoard()
        for(int rank = 0; rank < Board.BOARD_SIZE; rank++) {
            for(int file = 0; file < Board.BOARD_SIZE; file++) {
                Square origin = new Square(file, rank);
                Piece piece = game.getBoard().getPiece(origin);

                if(piece == null)
                    continue;
                else if(piece.side == side) {
                    if(piece.type == PieceType.King)
                        king = origin;
                    continue;
                }

                switch (piece.type) {
                    case King -> fillKingInfo(origin);
                    case Pawn -> fillPawnInfo(origin);
                    case Knight -> fillKnightInfo(origin);
                    case Queen, Bishop, Rook -> fillSlidingInfo(origin);
                }
            }
        }

        if(king == null)
            return;

        // check && pin
        int checkCount = 0;

        for(var knightMove : knightMoves) {
            Square sq = king.shift(knightMove[0], knightMove[1]);
            if(sq.valid()) {
                Piece piece = game.getBoard().getPiece(sq);
                if(piece != null && piece.type == PieceType.Knight && piece.side == attacker) {
                    checkBoard.set(sq);
                    checkCount++;
                }
            }
        }

        int dRank = side == Side.White ? 1 : -1;
        Square[] captSq = { king.shift(1, dRank), king.shift(-1, dRank) };
        for(var sq : captSq) {
            if (sq.valid()) {
                Piece piece = game.getBoard().getPiece(sq);
                if(piece != null && piece.type == PieceType.Pawn && piece.side == attacker)
                    checkBoard.set(sq);
            }
        }

        int myPieceCount = 0;
        BitBoard current = new BitBoard();

        for(int i = 0; i < directions.length; i++) {
            var dir = directions[i];
            boolean diag = i % 2 == 1;

            for(Square sq = king.shift(dir[0], dir[1]); sq.valid(); sq = sq.shift(dir[0], dir[1])) {
                Piece target = game.getBoard().getPiece(sq);
                current.set(sq);

                if(target != null) {
                    if(target.side == side)
                        myPieceCount++;
                    else {
                        if(
                                target.type == PieceType.Queen ||
                                (target.type == PieceType.Bishop && diag) ||
                                (target.type == PieceType.Rook && !diag)
                        ) {
                            if(myPieceCount == 0) {
                                checkBoard.set(current);
                                checkCount++;
                            } else if(myPieceCount == 1)
                                pinBoards[i].set(current);
                        }
                        break;
                    }
                }
                if(myPieceCount > 1)
                    break;
            }
            myPieceCount = 0;
            current.bits = 0;
        }

        for(var pb : pinBoards)
            uniPinBoard.set(pb);

        if(checkBoard.bits != 0)
            check = true;

        if(checkCount > 1)
            doublecheck = true;
    }

    // ---------- MOVE ----------

    private void addIfLegal(Move move) {
        LinkedList<Move> movelist;

        if(!moves.containsKey(move.from)) {
            movelist = new LinkedList<>();
            moves.put(move.from, movelist);
        } else {
            movelist = moves.get(move.from);
        }

        if(move.captured != null && move.captured.side == move.moving.side)
            return;

        // TODO check testing
        int pDir = game.getState().getTurn() == Side.White ? -1 : 1;
        Square
                epPawn = game.getState().getEpTarget().shift(0, pDir),
                king = game.getBoard().getKing(game.getState().getTurn());

        boolean kingIsMoving = move.moving.type == PieceType.King;

        if(doublecheck && !kingIsMoving)
            return;

        if(kingIsMoving && attackBoard.get(move.to))
            return;

        if(move.is(Move.EN_PASSANT)) {
            int
                    absDR = Math.abs(epPawn.rank - king.rank),
                    absDF = Math.abs(epPawn.file - king.file);
            boolean diag = absDR == absDF;

            if(diag || absDR == 0) {
                int[] dir = { (epPawn.file - king.file) / absDF, (epPawn.rank - king.rank) / absDR };
                for(Square target = king.shift(dir[0], dir[1]); target.valid(); target = target.shift(dir[0], dir[1])) {
                    Piece tp = game.getBoard().getPiece(target);
                    if(tp == null || target == move.from)
                        continue;
                    else if(tp.side == game.getState().getTurn())
                        break;
                    else {
                        if(
                                tp.type == PieceType.Queen ||
                                (tp.type == PieceType.Bishop && diag) ||
                                (tp.type == PieceType.Rook && !diag)
                        ) {
                            return;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        if(check && !kingIsMoving) {
            if(!checkBoard.get(move.to) &&
                !(move.is(Move.EN_PASSANT) && checkBoard.get(epPawn)) // the move captures en passant the checking pawn
            ) {
                return;
            }
        }

        if(uniPinBoard.get(move.from) && !kingIsMoving) {
            for(var pb : pinBoards) {
                if(!pb.get(move.from))
                    continue;

                if(!pb.get(move.to))
                    return;
                else
                    break;
            }
        }

        movelist.add(move);
    }

    private void addWithPromotion(Square from, Square to, Piece pawn, Piece captured) {
        int dstRank = pawn.isWhite() ? 7 : 0;
        if(to.rank == dstRank) {
            addIfLegal(new Move(from, to, pawn, captured, Move.PROMOTE_Q));
            addIfLegal(new Move(from, to, pawn, captured, Move.PROMOTE_R));
            addIfLegal(new Move(from, to, pawn, captured, Move.PROMOTE_N));
            addIfLegal(new Move(from, to, pawn, captured, Move.PROMOTE_B));
        } else {
            addIfLegal(new Move(from, to, pawn, captured));
        }
    }

    private void fillPawnMoves(Square origin) {
        Piece pawn = game.getBoard().getPiece(origin);
        int dRank = pawn.isWhite() ? 1 : -1;
        int baseRank = pawn.isWhite() ? 1 : 6;

        Square push = origin.shift(0, dRank);
        if(game.getBoard().getPiece(push) == null) {
            addWithPromotion(origin, push, pawn, null);

            Square dpush = push.shift(0, dRank);
            if(origin.rank == baseRank && game.getBoard().getPiece(dpush) == null)
                addIfLegal(new Move(origin, dpush, pawn, null, Move.DOUBLE_PUSH));
        }

        Square[] captSq = { push.shift(1, 0), push.shift(-1, 0) };
        for(var sq : captSq) {
            if(!sq.valid())
                continue;

            Piece capt = game.getBoard().getPiece(sq);
            if(capt != null && capt.side != pawn.side)
                addWithPromotion(origin, sq, pawn, capt);
            else if(game.getState().getEpTarget().equals(sq)) {
                Piece epCapt = game.getBoard().getPiece(Square.cross(sq, origin));
                addIfLegal(new Move(origin, sq, pawn, epCapt, Move.EN_PASSANT));
            }
        }
    }

    private void fillKnightMoves(Square origin) {
        Piece knight = game.getBoard().getPiece(origin);
        for(var knightMove : knightMoves) {
            Square sq = origin.shift(knightMove[0], knightMove[1]);
            if(sq.valid()) {
                Piece cap = game.getBoard().getPiece(sq);
                addIfLegal(new Move(origin, sq, knight, cap));
            }
        }
    }

    private boolean isQuietSq(Square sq) {
        return game.getBoard().getPiece(sq) == null && !attackBoard.get(sq);
    }

    private void fillKingMoves(Square origin) {
        Piece king = game.getBoard().getPiece(origin);
        for(var dir : directions) {
            Square to = origin.shift(dir[0], dir[1]);

            if(!to.valid())
                continue;

            Piece capt = game.getBoard().getPiece(to);
            addIfLegal(new Move(origin, to, king, capt));
        }

        int castleMask = king.isWhite() ? State.CASTLE_W : State.CASTLE_B;
        if (game.getState().canCastle(State.CASTLE_K & castleMask)) {
            if(
                    !check &&
                    isQuietSq(origin.shift(1, 0)) &&
                    isQuietSq(origin.shift(2, 0))
            ){
                addIfLegal(new Move(origin, origin.shift(2, 0), king, null, Move.CASTLE_K));
            }
        }
        if(game.getState().canCastle(State.CASTLE_Q & castleMask)) {
            if(
                    !check &&
                    isQuietSq(origin.shift(-1, 0)) &&
                    isQuietSq(origin.shift(-2, 0)) &&
                    game.getBoard().getPiece(origin.shift(-3, 0)) == null
            ) {
                addIfLegal(new Move(origin, origin.shift(-2, 0), king, null, Move.CASTLE_Q));
            }
        }
    }

    private void fillSlidingMoves(Square origin) {
        Piece sliding = game.getBoard().getPiece(origin);
        int dIdx = sliding.type == PieceType.Queen ? 1 : 2;
        int startIdx = sliding.type == PieceType.Bishop ? 1 : 0;

        for(int i = startIdx; i < directions.length; i += dIdx) {
            var dir = directions[i];
            for(Square sq = origin.shift(dir[0], dir[1]); sq.valid(); sq = sq.shift(dir[0], dir[1])) {
                Piece capt = game.getBoard().getPiece(sq);
                addIfLegal(new Move(origin, sq, sliding, capt));
                if(capt != null)
                    break;
            }
        }
    }

    private void fillMoves() {
        for(int rank = 0; rank < Board.BOARD_SIZE; rank++) {
            for(int file = 0; file < Board.BOARD_SIZE; file++) {
                Square sq = new Square(file, rank);
                Piece moving = game.getBoard().getPiece(sq);

                if(moving == null || moving.side != game.getState().getTurn())
                    continue;

                switch(moving.type) {
                    case King -> fillKingMoves(sq);
                    case Knight -> fillKnightMoves(sq);
                    case Pawn -> fillPawnMoves(sq);
                    case Queen, Bishop, Rook -> fillSlidingMoves(sq);
                }
            }
        }
    }

    public LinkedList<Move> getMoves(Square origin) {
        if(!moves.containsKey(origin))
            return null;
        return moves.get(origin);
    }

    private static long PerftHelper(int depth, Game game) {
        if (depth == 0)
            return 1L;

        var gen = new MoveGen(game);
        long pos = 0;

        for(var mlist : gen.moves.values()) {
            for(var move : mlist) {
                game.play(move);
                pos += PerftHelper(depth - 1, game);
                game.unplay();
            }
        }
        return pos;
    }

    public static void Perft(int depth, Game game) {
        if (depth == 0)
            System.out.println("min depth == 1");

        var gen = new MoveGen(game);
        long pos = 0;

        for(var mlist : gen.moves.values()) {
            for(var move : mlist) {
                game.play(move);
                long subpos = PerftHelper(depth - 1, game);
                pos += subpos;
                System.out.println(move + ": " + subpos);
                game.unplay();
            }
        }
        System.out.println("Perft result: " + pos);
    }
}
