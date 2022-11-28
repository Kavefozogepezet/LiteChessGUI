package test.me.lcgui.game.board;

import me.lcgui.game.board.Piece;
import me.lcgui.game.board.Side;
import me.lcgui.game.board.Square;
import me.lcgui.game.board.State;
import me.lcgui.game.movegen.Move;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StateTest {
    State state;

    @Before
    public void setUp() {
        state = new State(
                Side.White,
                State.CASTLE_BQ | State.CASTLE_WK,
                Square.g6,
                6, 0
                );
    }

    @Test
    public void testCastling() {
        Assert.assertEquals(State.CASTLE_BQ | State.CASTLE_WK, state.getCastleRights());
        Assert.assertTrue(state.canCastle(State.CASTLE_BQ));
        Assert.assertTrue(state.canCastle(State.CASTLE_WK));

        Move move = new Move(Square.h1, Square.h2, Piece.WRook);
        state.movePlayed(move);
        Assert.assertFalse(state.canCastle(State.CASTLE_W));
    }

    @Test
    public void testMove() {
        Move move = new Move(Square.h1, Square.h2, Piece.WRook);
        state.movePlayed(move);
        Assert.assertEquals(Square.invalid, state.getEpTarget());
        Assert.assertEquals(7, state.getPly());
        Assert.assertEquals(1, state.get50move());
        Assert.assertEquals(Side.Black, state.getTurn());

        Move doubleush = new Move(Square.b7, Square.b5, Piece.BPawn, null, Move.DOUBLE_PUSH);
        state.movePlayed(doubleush);
        Assert.assertEquals(Square.b6, state.getEpTarget());
    }
}
