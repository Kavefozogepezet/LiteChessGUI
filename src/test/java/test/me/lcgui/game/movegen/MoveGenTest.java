package test.me.lcgui.game.movegen;

import me.lcgui.game.Game;
import me.lcgui.game.IncorrectNotationException;
import me.lcgui.game.movegen.Move;
import me.lcgui.game.movegen.MoveGen;
import me.lcgui.game.setup.FEN;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;

public class MoveGenTest {
    MoveGen gen;

    @Before
    public void setUp() throws IncorrectNotationException {
        Game game = new Game(new FEN("4k3/3r4/8/1B1n4/8/8/8/3K1R2 w - - 0 1"));
        gen = new MoveGen(game);
    }

    @Test
    public void testGenerate() {
        int idx = 0;
        while(true) {
            Path path = Paths.get("src", "test", "resources", "movegen", "pos" + idx++ + ".txt");
            File file = new File(path.toUri());
            if(!file.exists()) {
                idx--;
                break;
            }

            try (var in = new FileInputStream(file)) {
                Scanner scn = new Scanner(in);

                Game game = new Game(new FEN(scn.nextLine()));
                gen = new MoveGen(game);
                gen.generate();

                HashSet<String> moveSet = new HashSet<>();
                while(scn.hasNextLine())
                    moveSet.add(scn.nextLine());

                Assert.assertEquals(moveSet.size(), gen.all().size());
                for(Move move : gen.all())
                    Assert.assertTrue(moveSet.contains(move.toString()));
            } catch (IOException | IncorrectNotationException e) {
                throw new RuntimeException(e);
            }
        }
        Assert.assertTrue("Insufficient number of tests. Ass more test positions", idx > 0);
    }
}
