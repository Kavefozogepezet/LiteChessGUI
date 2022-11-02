package me.lcgui.game.setup;

import jdk.jshell.spi.ExecutionControl;
import me.lcgui.game.Game;
import me.lcgui.game.board.Side;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class PGN implements GameSetup {
    String pgn;

    public PGN(Game game) {
        StringBuilder pgnb = new StringBuilder();
        pgnb.append(tagLineMaker("Event", "Casual Match"));
        pgnb.append(tagLineMaker("Side", "Lite Chess GUI"));

        var formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        pgnb.append(tagLineMaker("Date", formatter.format(LocalDateTime.now())));

        pgnb.append(tagLineMaker("Round", "?"));
        pgnb.append(tagLineMaker("White", game.getPlayer(Side.White).getName()));
        pgnb.append(tagLineMaker("Black", game.getPlayer(Side.Black).getName()));

        String resultStr = "*";
        if(game.hasEnded()) {
            switch (game.getResult()) {
                case WHITE_WINS -> resultStr = "1-0";
                case BLACK_WINS -> resultStr = "0-1";
                case DRAW -> resultStr = "1/2-1/2";
            }
        }
        pgnb.append(tagLineMaker("Result", resultStr));

        pgnb.append(tagLineMaker("SetUp", game.isDefaultStart() ? "0" : "1"));
        if(!game.isDefaultStart())
            pgnb.append(tagLineMaker("FEN", game.getStartFen().toString()));

        String terminationStr = "unterminated";
        if(game.hasEnded()) {
            switch (game.getTermination()) {
                case NORMAL, FORFEIT -> terminationStr = "normal";
                case TIME_FORFEIT -> terminationStr = "time forfeit";
                case ABANDONED -> terminationStr = "abandoned";
            }
        }
        pgnb.append(tagLineMaker("Termination", terminationStr));
        pgnb.append('\n');

        int ply = game.getStartPly();
        for(var moveData : game.getMoveList()) {
            if(ply % 2 == 0)
                pgnb.append(ply / 2 + 1).append(". ");
            pgnb.append(moveData.SAN).append(' ');
            if(moveData.hasComment())
                pgnb.append(commentMaker(moveData.comment)).append(' ');
            ply++;
        }
        pgnb.append(resultStr);
        pgn = pgnb.toString();
    }

    @Override
    public void set(Game game) {
        // TODO
    }

    private String tagLineMaker(String tag, String value) {
        return "[" + tag + " \"" + value + "\"]\n";
    }

    private String commentMaker(String comment) {
        return '{' + comment + '}';
    }

    @Override
    public String toString() {
        return pgn;
    }
}
