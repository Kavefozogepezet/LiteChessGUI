package test.me.lcgui.game.board;

import me.lcgui.game.board.Piece;
import me.lcgui.game.board.PieceType;
import me.lcgui.game.board.Square;
import org.junit.Assert;
import org.junit.Test;

public class EnumTest {
    @Test
    public void testPieceTypeConversion() {
        Assert.assertEquals(PieceType.Knight, PieceType.fromChar('n'));
        Assert.assertEquals(PieceType.King, PieceType.fromChar('K'));
        Assert.assertEquals(PieceType.Queen, PieceType.fromChar('q'));
        Assert.assertEquals(PieceType.Pawn, PieceType.fromChar('P'));
        Assert.assertEquals(PieceType.Rook, PieceType.fromChar('r'));
        Assert.assertEquals(PieceType.Bishop, PieceType.fromChar('B'));

        Assert.assertEquals('k', PieceType.King.toChar());
        Assert.assertEquals('n', PieceType.Knight.toChar());
        Assert.assertEquals('q', PieceType.Queen.toChar());
        Assert.assertEquals('p', PieceType.Pawn.toChar());
        Assert.assertEquals('r', PieceType.Rook.toChar());
        Assert.assertEquals('b', PieceType.Bishop.toChar());
    }

    @Test
    public void testPieceConversion() {
        Assert.assertEquals(Piece.BKnight, Piece.fromChar('n'));
        Assert.assertEquals(Piece.WKing, Piece.fromChar('K'));
        Assert.assertEquals(Piece.BQueen, Piece.fromChar('q'));
        Assert.assertEquals(Piece.WPawn, Piece.fromChar('P'));
        Assert.assertEquals(Piece.BRook, Piece.fromChar('r'));
        Assert.assertEquals(Piece.WBishop, Piece.fromChar('B'));

        Assert.assertEquals('k', Piece.BKing.toChar());
        Assert.assertEquals('N', Piece.WKnight.toChar());
        Assert.assertEquals('Q', Piece.WQueen.toChar());
        Assert.assertEquals('p', Piece.BPawn.toChar());
        Assert.assertEquals('R', Piece.WRook.toChar());
        Assert.assertEquals('b', Piece.BBishop.toChar());
    }

    @Test
    public void testSquare() {
        Assert.assertTrue(Square.a3.equals(new Square(0, 2)));
        Assert.assertEquals(new Square('e', 4), Square.e4);

        Assert.assertTrue(Square.c2.valid());
        Assert.assertFalse(Square.invalid.valid());

        Assert.assertEquals(Square.f3, Square.e2.shift(1, 1));
        Assert.assertEquals(Square.f3, Square.between(Square.e2, Square.g4));
        Assert.assertEquals(Square.f3, Square.cross(Square.f1, Square.e3));

        Assert.assertEquals("d8", Square.d8.toString());
    }
}
