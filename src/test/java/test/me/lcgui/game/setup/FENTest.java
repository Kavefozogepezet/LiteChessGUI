package test.me.lcgui.game.setup;

import me.lcgui.game.Game;
import me.lcgui.game.IncorrectNotationException;
import me.lcgui.game.board.Board;
import me.lcgui.game.board.Piece;
import me.lcgui.game.board.Square;
import me.lcgui.game.board.State;
import me.lcgui.game.setup.FEN;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static me.lcgui.game.board.AbstractBoard.BOARD_SIZE;

public class FENTest {
    @Test
    public void testSet() throws IncorrectNotationException {
        FEN fen = new FEN("4k2r/8/1b6/8/2Q1P3/7N/8/2K5 w k - 5 14");
        Game game = new Game(fen);

        Board b = game.getBoard();

        Map<Square, Piece> pieces = Map.ofEntries(
                Map.entry(Square.e8, Piece.BKing),
                Map.entry(Square.h8, Piece.BRook),
                Map.entry(Square.h3, Piece.WKnight),
                Map.entry(Square.e4, Piece.WPawn),
                Map.entry(Square.c1, Piece.WKing),
                Map.entry(Square.c4, Piece.WQueen),
                Map.entry(Square.b6, Piece.BBishop)
        );

        for(int rank = BOARD_SIZE - 1; rank >= 0; rank--) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                Square sq = new Square(file, rank);
                if(pieces.containsKey(sq))
                    Assert.assertEquals(pieces.get(sq), b.getPiece(sq));
                else
                    Assert.assertNull(b.getPiece(sq));
            }
        }

        State s = game.getState();
        Assert.assertEquals(State.CASTLE_BK, s.getCastleRights());
        Assert.assertEquals(Square.invalid, s.getEpTarget());
        Assert.assertEquals(26, s.getPly());
        Assert.assertEquals(5, s.get50move());
    }

    @Test
    public void testGet() throws IncorrectNotationException {
        FEN fen = new FEN("4k2r/8/1b6/8/2Q1P3/7N/8/2K5 w k - 5 14");
        Game game = new Game(fen);

        FEN newFen = new FEN(game);
        Assert.assertEquals(fen, newFen);
        Assert.assertEquals(fen, "4k2r/8/1b6/8/2Q1P3/7N/8/2K5 w k - 5 14");
    }
}
