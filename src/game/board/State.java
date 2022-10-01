package game.board;

import game.movegen.Move;

import java.util.LinkedList;

public class State {
    public static final int CASTLE_WK = 0b0001;
    public static final int CASTLE_WQ = 0b0010;
    public static final int CASTLE_BK = 0b0100;
    public static final int CASTLE_BQ = 0b1000;

    public static final int CASTLE_W =  CASTLE_WK | CASTLE_WQ;
    public static final int CASTLE_B =  CASTLE_BK | CASTLE_BQ;
    public static final int CASTLE_K =  CASTLE_WK | CASTLE_BK;
    public static final int CASTLE_Q =  CASTLE_WQ | CASTLE_BQ;
    public static final int CASTLE_ALL =CASTLE_W | CASTLE_B;

    private int castlingRights = 0;
    private Square epTarget = Square.invalid;
    private Side turn = Side.White;
    private int ply = 0;

    private static class Record {
        public int c;
        public Square ep;
        Record(int c, Square ep) {
            this.c = c;
            this.ep = ep;
        }
    }

    LinkedList<Record> prevStates = new LinkedList<>();

    public State() {}

    public State(Side turn, int castling, Square ep, int ply) {
        this.turn = turn;
        castlingRights = castling;
        epTarget = ep;
        ply = 0;
    }

    public void movePlayed(Move move) {
        prevStates.add(new Record(castlingRights, epTarget));

        epTarget = Square.invalid;

        if(move.moving == Piece.WKing)
            castlingRights &= ~CASTLE_W;
        else if(move.moving == Piece.BKing)
            castlingRights &= ~CASTLE_B;

        if (castlingRights != 0) {
            if (move.to.equals(Square.h1) || move.from.equals( Square.h1))
                castlingRights &= ~CASTLE_WK;
            else if (move.to.equals(Square.a1) || move.from.equals(Square.a1))
                castlingRights &= ~CASTLE_WQ;;

            if (move.to.equals(Square.h8) || move.from.equals(Square.h8))
                castlingRights &= ~CASTLE_BK;
            else if (move.to.equals(Square.a8) || move.from.equals(Square.a8))
                castlingRights &= ~CASTLE_BQ;
        }

        if(move.is(Move.DOUBLE_PUSH))
            epTarget = Square.between(move.from, move.to);

        turn = turn.other();
        ply++;
    }

    public void moveUnplayed() {
        Record rec = prevStates.pollLast();
        castlingRights = rec.c;
        epTarget = rec.ep;
        turn = turn.other();
        ply--;
    }

    public boolean canCastle(int flag) {
        return (castlingRights & flag) != 0;
    }

    public int getCastleRights() {
        return castlingRights;
    }

    public Square getEpTarget() {
        return epTarget;
    }

    public Side getTurn() {
        return turn;
    }

    public int getPly() { return ply; }

    public int get50move() {
        //TODO implement 50 move counting
        return 0;
    }
}
