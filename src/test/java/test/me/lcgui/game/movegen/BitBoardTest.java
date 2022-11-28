package test.me.lcgui.game.movegen;

import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.BitBoard;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BitBoardTest {
    private BitBoard bb;

    @Before
    public void setUp() {
        bb = new BitBoard();
    }

    @Test
    public void testSetGet() {
        bb.set(Square.e8);
        BitBoard other = new BitBoard();
        other.set(Square.f3);

        Assert.assertTrue(bb.get(Square.e8));
        Assert.assertFalse(bb.get(Square.f3));
        bb.set(other);
        Assert.assertTrue(bb.get(Square.f3));

        bb.clear(Square.e8);
        Assert.assertFalse(bb.get(Square.e8));

        bb.clear(bb);
        Assert.assertEquals(0, bb.toLong());
    }
}
