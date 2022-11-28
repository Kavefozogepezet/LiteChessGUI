package test.me.lcgui.game.setup;

import me.lcgui.game.Game;
import me.lcgui.game.IllegalMoveException;
import me.lcgui.game.IncorrectNotationException;
import me.lcgui.game.board.Side;
import me.lcgui.game.movegen.Move;
import me.lcgui.game.player.HumanPlayer;
import me.lcgui.game.setup.PGN;
import org.junit.Assert;
import org.junit.Test;

public class PNGTest {
    private static final String PGNmoves = "1. e4 e5 2. Nf3 Nc6 3. d4 1-0";
    private static final String PGNStr = "[White \"White\"]\n[Black \"Black\"]\n" + PGNmoves;

    @Test
    public void testSet() throws IncorrectNotationException {
        PGN pgn = new PGN(PGNStr);
        Game game = new Game(pgn);

        String[] moves = { "e2e4", "e7e5", "g1f3", "b8c6", "d2d4" };
        for(int i = 0; i < game.getState().getPly(); i++) {
            Move move = game.getMoveData(i).move;
            Assert.assertEquals(moves[i], move.toString());
        }
    }

    @Test
    public void testGet() throws IllegalMoveException, IncorrectNotationException {
        Game game = new Game();
        game.setPlayer(Side.White, new HumanPlayer("White"));
        game.setPlayer(Side.Black, new HumanPlayer("Black"));
        game.startGame();

        String[] moves = { "e2e4", "e7e5", "g1f3", "b8c6", "d2d4" };
        for(var moveStr : moves) {
            Move move = game.parseLAMove(moveStr);
            game.play(move);
        }
        game.resign();

        PGN pgn = new PGN(game);
        String[] str = pgn.toString().split("\n");
        Assert.assertEquals(PGNmoves, str[str.length - 1]);
    }
}
