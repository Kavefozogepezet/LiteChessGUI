package test.me.lcgui.game.board;

import me.lcgui.game.board.*;
import me.lcgui.game.movegen.Move;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BoardTest {
    private Board board;

    private Move move;

    @Before
    public void setUp() {
        board = new Board();
        board.setPiece(Square.a8, Piece.BBishop);
        board.setPiece(Square.a2, Piece.WPawn);
        board.setPiece(Square.e1, Piece.WKing);
        board.setPiece(Square.e8, Piece.BKing);
        board.setPiece(Square.b3, Piece.BPawn);
        move = new Move(Square.a2, Square.b3, Piece.WPawn, Piece.BPawn);
    }

    @Test
    public void testSetGet() {
        Assert.assertEquals(Piece.BBishop, board.getPiece(Square.char2file('a'), 7));
        Assert.assertEquals(Piece.WPawn, board.getPiece(Square.char2file('a'), 1));
        Assert.assertEquals(Piece.WKing, board.getPiece(Square.e1));
        Assert.assertEquals(Piece.BKing, board.getPiece(Square.e8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSquare() {
        board.getPiece(Square.invalid);
    }

    @Test
    public void testRemovePiece() {
        int file = Square.char2file('a');
        board.removePiece(file, 7);
        Assert.assertNull(board.getPiece(file, 7));

        board.removePiece(Square.a2);
        Assert.assertNull(board.getPiece(Square.a2));
    }

    @Test
    public void testClear() {
        board.clear();
        Assert.assertNull(board.getPiece(Square.e2));
        Assert.assertNull(board.getPiece(Square.a8));
        Assert.assertNull(board.getPiece(Square.e1));
        Assert.assertNull(board.getPiece(Square.e8));
    }

    @Test
    public void testGetKing() {
        Assert.assertEquals(Square.e1, board.getKing(Side.White));
        Assert.assertEquals(Square.e8, board.getKing(Side.Black));
    }

    @Test
    public void testPlay() {
        board.play(move);
        Assert.assertNull(board.getPiece(move.from));
        Assert.assertEquals(board.getPiece(move.to), move.moving);
    }

    @Test
    public void testUnplay() {
        board.play(move);
        board.unplay(move);
        Assert.assertEquals(move.captured, board.getPiece(move.to));
        Assert.assertEquals(move.moving, board.getPiece(move.from));
    }

    @Test
    public void testIsSqAttacked() {
        Assert.assertFalse(board.isCheck(Side.White));

        board.setPiece(Square.h4, Piece.BRook);
        Assert.assertTrue(board.isSqAttacked(Square.h2, Side.Black));
        Assert.assertFalse(board.isSqAttacked(Square.h2, Side.White));

        board.setPiece(Square.h3, Piece.WPawn); // blocking the rook
        Assert.assertFalse(board.isSqAttacked(Square.h2, Side.Black));

        board.setPiece(Square.g4, Piece.BKnight);
        Assert.assertTrue(board.isSqAttacked(Square.h2, Side.Black));

        board.setPiece(Square.h6, Piece.WPawn);
        Assert.assertTrue(board.isSqAttacked(Square.g7, Side.White));
    }

    @Test
    public void testGetMaterial() {
        Assert.assertEquals(1, board.getMaterial(Side.White, PieceType.Pawn));
        Assert.assertEquals(2, board.getMaterial(PieceType.King));

        board.setPiece(Square.c7, Piece.BPawn);
        Assert.assertEquals(3, board.getMaterial(PieceType.Pawn));
    }

    @Test
    public void testGetBishops() {
        Assert.assertEquals(1, board.getDarkBishops());
        Assert.assertEquals(0, board.getLightBishops());
    }
}