package me.lcgui.game.movegen;

import me.lcgui.game.Game;
import me.lcgui.game.board.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Legális lépések generálásához használható osztály.
 */
public class MoveGen implements Serializable {
    public static final int[][] directions = {
            { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 }
    };

    public static final int[][] knightMoves = {
            { 1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { -1, -2 }, { -2, -1 }, { -2, 1 }, { -1, 2 }
    };

    private Game game;
    private transient AbstractBoard board = null;
    private transient State state = null;

    private transient BitBoard
            checkBoard = new BitBoard(),
            attackBoard = new BitBoard(),
            uniPinBoard = new BitBoard();
    private transient BitBoard[] pinBoards = new BitBoard[8];

    private boolean check, doublecheck;

    private final LinkedList<Move> allMoves = new LinkedList<>();
    private final HashMap<Square, LinkedList<Move>> moves = new HashMap<>();
    private final HashMap<Square, LinkedList<Move>> movesToSq = new HashMap<>();

    /**
     * Létrehozza a lépés generálót a megadott partihoz.
     * @param game A parti, aminek a legális lépéseit generálni szeretnénk.
     */
    public MoveGen(Game game) {
        this.game = game;
        for(int i = 0; i < pinBoards.length; i++)
            pinBoards[i] = new BitBoard();
    }

    /**
     * Generálja az összes legális lépést a parti állása szerint.
     */
    public void generate() {
        this.board = this.game.getBoard();
        this.state = this.game.getState();
        moves.clear();
        movesToSq.clear();
        allMoves.clear();
        fillInfo();
        fillMoves();
    }

    /**
     * @return igaz, ha a legutóbbi generálás alkalmával sakk állás volt.
     */
    public boolean isCheck() {
        return check;
    }

    /**
     * Megadja azokat a lépéseket, amelyek egy bizonyos mezőről indulnak.
     * @param origin A lépések kezdő mezője.
     * @return Az ilyen lépések listája.
     */
    public LinkedList<Move> from(Square origin) {
        var list = moves.get(origin);
        return list == null ? new LinkedList<>() : list;
    }

    /**
     * Megadja azokat a lépéseket, amelyek egy bizonyos mezőre érkeznek.
     * @param sq A lépések cél mezője.
     * @return Az ilyen lépések listája.
     */
    public LinkedList<Move> to(Square sq) {
        var list = movesToSq.get(sq);
        return list == null ? new LinkedList<>() : list;
    }

    /**
     * @return Az összes legális lépés listája.
     */
    public LinkedList<Move> all() {
        return allMoves;
    }

    /**
     * @return igaz, ha nem talált legális lépést, mert patt, vagy matt helyzet állt elő.
     */
    public boolean isEmpty() {
        return allMoves.isEmpty();
    }

    // ---------- INFO ----------

    private void fillPawnInfo(Square origin) {
        Piece pawn = board.getPiece(origin);
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
        Piece sliding = board.getPiece(origin);
        int dIdx = sliding.type == PieceType.Queen ? 1 : 2;
        int startIdx = sliding.type == PieceType.Bishop ? 1 : 0;

        for(int i = startIdx; i < directions.length; i += dIdx) {
            var dir = directions[i];
            for(Square sq = origin.shift(dir[0], dir[1]); sq.valid(); sq = sq.shift(dir[0], dir[1])) {
                Piece capt = board.getPiece(sq);
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

        Side side = state.getTurn();
        Side attacker = side.other();
        Square king = null;

        // Attack game.board
        for(int rank = 0; rank < AbstractBoard.BOARD_SIZE; rank++) {
            for(int file = 0; file < AbstractBoard.BOARD_SIZE; file++) {
                Square origin = new Square(file, rank);
                Piece piece = board.getPiece(origin);

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
                Piece piece = board.getPiece(sq);
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
                Piece piece = board.getPiece(sq);
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
                Piece target = board.getPiece(sq);
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
        if(move.captured != null && move.captured.side == move.moving.side)
            return;

        int pDir = state.getTurn() == Side.White ? -1 : 1;
        Square
                epPawn = state.getEpTarget().shift(0, pDir),
                king = board.getKing(state.getTurn());

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
                    Piece tp = board.getPiece(target);
                    if(tp == null || target == move.from)
                        continue;
                    else if(tp.side == state.getTurn())
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

        LinkedList<Move> movelist = moves.computeIfAbsent(move.from, k -> new LinkedList<>());
        LinkedList<Move> moveToList = movesToSq.computeIfAbsent(move.to, k -> new LinkedList<>());

        movelist.add(move);
        moveToList.add(move);
        allMoves.add(move);
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
        Piece pawn = board.getPiece(origin);
        int dRank = pawn.isWhite() ? 1 : -1;
        int baseRank = pawn.isWhite() ? 1 : 6;

        Square push = origin.shift(0, dRank);
        if(board.getPiece(push) == null) {
            addWithPromotion(origin, push, pawn, null);

            Square dpush = push.shift(0, dRank);
            if(origin.rank == baseRank && board.getPiece(dpush) == null)
                addIfLegal(new Move(origin, dpush, pawn, null, Move.DOUBLE_PUSH));
        }

        Square[] captSq = { push.shift(1, 0), push.shift(-1, 0) };
        for(var sq : captSq) {
            if(!sq.valid())
                continue;

            Piece capt = board.getPiece(sq);
            if(capt != null && capt.side != pawn.side)
                addWithPromotion(origin, sq, pawn, capt);
            else if(state.getEpTarget().equals(sq)) {
                Piece epCapt = board.getPiece(Square.cross(sq, origin));
                addIfLegal(new Move(origin, sq, pawn, epCapt, Move.EN_PASSANT));
            }
        }
    }

    private void fillKnightMoves(Square origin) {
        Piece knight = board.getPiece(origin);
        for(var knightMove : knightMoves) {
            Square sq = origin.shift(knightMove[0], knightMove[1]);
            if(sq.valid()) {
                Piece cap = board.getPiece(sq);
                addIfLegal(new Move(origin, sq, knight, cap));
            }
        }
    }

    private boolean isQuietSq(Square sq) {
        return board.getPiece(sq) == null && !attackBoard.get(sq);
    }

    private void fillKingMoves(Square origin) {
        Piece king = board.getPiece(origin);
        for(var dir : directions) {
            Square to = origin.shift(dir[0], dir[1]);

            if(!to.valid())
                continue;

            Piece capt = board.getPiece(to);
            addIfLegal(new Move(origin, to, king, capt));
        }

        int castleMask = king.isWhite() ? State.CASTLE_W : State.CASTLE_B;
        if (state.canCastle(State.CASTLE_K & castleMask)) {
            if(
                    !check &&
                    isQuietSq(origin.shift(1, 0)) &&
                    isQuietSq(origin.shift(2, 0))
            ){
                addIfLegal(new Move(origin, origin.shift(2, 0), king, null, Move.CASTLE_K));
            }
        }
        if(state.canCastle(State.CASTLE_Q & castleMask)) {
            if(
                    !check &&
                    isQuietSq(origin.shift(-1, 0)) &&
                    isQuietSq(origin.shift(-2, 0)) &&
                    board.getPiece(origin.shift(-3, 0)) == null
            ) {
                addIfLegal(new Move(origin, origin.shift(-2, 0), king, null, Move.CASTLE_Q));
            }
        }
    }

    private void fillSlidingMoves(Square origin) {
        Piece sliding = board.getPiece(origin);
        int dIdx = sliding.type == PieceType.Queen ? 1 : 2;
        int startIdx = sliding.type == PieceType.Bishop ? 1 : 0;

        for(int i = startIdx; i < directions.length; i += dIdx) {
            var dir = directions[i];
            for(Square sq = origin.shift(dir[0], dir[1]); sq.valid(); sq = sq.shift(dir[0], dir[1])) {
                Piece capt = board.getPiece(sq);
                addIfLegal(new Move(origin, sq, sliding, capt));
                if(capt != null)
                    break;
            }
        }
    }

    private void fillMoves() {
        for(int rank = 0; rank < AbstractBoard.BOARD_SIZE; rank++) {
            for(int file = 0; file < AbstractBoard.BOARD_SIZE; file++) {
                Square sq = new Square(file, rank);
                Piece moving = board.getPiece(sq);

                if(moving == null || moving.side != state.getTurn())
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

    @Serial
    protected Object readResolve() {
        checkBoard = new BitBoard();
        attackBoard = new BitBoard();
        uniPinBoard = new BitBoard();
        pinBoards = new BitBoard[8];
        for(int i = 0; i < pinBoards.length; i++)
            pinBoards[i] = new BitBoard();
        return this;
    }
}
