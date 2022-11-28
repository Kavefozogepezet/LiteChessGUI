package test.me.lcgui.game.movegen;

import me.lcgui.game.board.Piece;
import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.Move;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MoveTest {
    Move casual, capture, promote, castle;

    @Before
    public void setUp() {
        casual = new Move(Square.e2, Square.h2, Piece.WRook);
        capture = new Move(Square.e2, Square.h2, Piece.WRook, Piece.BPawn);
        promote = new Move(Square.e7, Square.d8, Piece.WPawn, Piece.BKnight, Move.PROMOTE_N);
        castle = new Move(Square.e1, Square.c1, Piece.WKing, null, Move.CASTLE_K);
    }

    @Test
    public void testCasualIsFuncs() {
        Assert.assertFalse(casual.isCapture());
        Assert.assertFalse(casual.isCastle());
        Assert.assertFalse(casual.isPromotion());
    }

    @Test
    public void testCaptureIsFuncs() {
        Assert.assertTrue(capture.isCapture());
        Assert.assertFalse(capture.isCastle());
        Assert.assertFalse(capture.isPromotion());
    }

    @Test
    public void testPromoteIsFuncs() {
        Assert.assertTrue(promote.isCapture());
        Assert.assertFalse(promote.isCastle());
        Assert.assertTrue(promote.isPromotion());
        Assert.assertEquals(Piece.WKnight, promote.getPromotionPiece());
    }

    @Test
    public void testCastleIsFuncs() {
        Assert.assertFalse(castle.isCapture());
        Assert.assertTrue(castle.isCastle());
        Assert.assertFalse(castle.isPromotion());
        Assert.assertTrue((castle.is(Move.CASTLE_K)));
    }

    @Test
    public void testEquals() {
        Move move = new Move(Square.e7, Square.d8, Piece.WPawn, Piece.BKnight, Move.PROMOTE_N);
        Assert.assertEquals(move, promote);
        Assert.assertNotEquals(move, casual);
        Assert.assertNotEquals(move, capture);
        Assert.assertNotEquals(move, castle);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("e2h2", casual.toString());
        Assert.assertEquals("e7d8n", promote.toString());
        Assert.assertEquals("e1c1", castle.toString());
    }
}
