package me.lcgui.game.setup;

import me.lcgui.game.Game;
import me.lcgui.game.IncorrectNotationException;
import me.lcgui.game.board.PieceType;
import me.lcgui.game.board.Side;
import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.Move;
import me.lcgui.game.player.HumanPlayer;
import me.lcgui.game.player.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

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

    public PGN(String str) {
        this.pgn = str;
    }

    @Override
    public void set(Game game) throws IncorrectNotationException {
        var pgnT = new PGNTokenizer();

        Player white = null, black = null;
        Game.Result result = null;
        Game.Termination termination = null;
        GameSetup setup = new StartPos();

        for(var tag : pgnT.tags) {
            switch (tag.name) {
                case "White" -> white = new HumanPlayer(tag.value);
                case "Black" -> black = new HumanPlayer(tag.value);
                case "Result" -> result = getResult(tag.value);
                case "Termination" -> termination = getTermination(tag.value);
                case "FEN" -> setup = new FEN(tag.value);
            }
        }

        if(white == null || black == null)
            throw new IncorrectNotationException("Missing mandatory player tags (White & Black)");

        setup.set(game);
        game.getPossibleMoves().generate();
        game.setPlayer(Side.White, white);
        game.setPlayer(Side.Black, black);

        game.startGame();

        for(var moveData : pgnT.moves) {
            Move move = null;
            String san = moveData.san;

            while("#+!?".contains(san.substring(san.length() - 1)))
                san = san.substring(0, san.length() - 1);

            try {
                if (san.equals("O-O")) {
                    Square to = game.getState().getTurn() == Side.White ? Square.g1 : Square.g8;
                    for (var candidate : game.getPossibleMoves().to(to)) {
                        if (candidate.moving.type == PieceType.King) {
                            game.play(candidate);
                            break;
                        }
                    }
                    if (moveData.comment != null)
                        game.getMoveData(game.getState().getPly() - 1).comment = moveData.comment;
                } else if (moveData.san.equals("O-O-O")) {
                    Square to = game.getState().getTurn() == Side.White ? Square.c1 : Square.c8;
                    for (var candidate : game.getPossibleMoves().to(to)) {
                        if (candidate.moving.type == PieceType.King) {
                            game.play(candidate);
                            break;
                        }
                    }
                    if (moveData.comment != null)
                        game.getMoveData(game.getState().getPly() - 1).comment = moveData.comment;
                } else {
                    char first = san.charAt(0);
                    char second = san.charAt(1);
                    char prom = 0;

                    PieceType pt;
                    if (Character.isUpperCase(first)) {
                        pt = PieceType.fromChar(first);
                        if (pt == null)
                            throw new IncorrectNotationException("Invalid piece notation in move " + san);
                    } else {
                        pt = PieceType.Pawn;
                    }

                    if(san.contains("=")) {
                        int idx = san.indexOf('=');
                        prom = Character.toLowerCase(san.charAt(idx + 1));
                        san = san.substring(0, idx);
                    }

                    String toStr = san.substring(san.length() - 2, san.length());
                    Square to = Square.parse(toStr);
                    if (!to.valid())
                        throw new IncorrectNotationException("Invalid destination square in move " + san);

                    var legalMoves = game.getPossibleMoves().to(to);
                    LinkedList<Move> candidates = new LinkedList<>();

                    for(var candidate : legalMoves) {
                        if(
                                candidate.to.equals(to) && candidate.moving.type == pt
                                        && (prom == 0 || (candidate.isPromotion()
                                            && candidate.getPromotionPiece().type.toChar() == prom))
                        ) {
                            candidates.add(candidate);
                        }
                    }

                    if (moveCheck(game, moveData, candidates))
                        continue;

                    int file = pt == PieceType.Pawn
                            ? Square.char2file(first)
                            : Square.char2file(second);

                    candidates.removeIf(move1 -> move1.from.file != file);

                    if (moveCheck(game, moveData, candidates))
                        continue;

                    int rank = Square.char2file(san.charAt(3));
                    candidates.removeIf(move2 -> move2.from.rank != rank);

                    moveCheck(game, moveData, candidates);
                }
            }
            catch (IndexOutOfBoundsException e) {
                throw new IncorrectNotationException("Invalid move: " + san);
            }
        }

        if(result != null) {
            if(termination == null)
                termination = Game.Termination.NORMAL;
            game.endGame(result, termination);
        }
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

    private boolean moveCheck(Game game, PGNTokenizer.Move moveData, LinkedList<Move> candidates) throws IncorrectNotationException {
        if(candidates.size() == 1) {
            game.play(candidates.get(0));
            if(moveData.comment != null)
                game.getMoveData(game.getState().getPly() - 1).comment = moveData.comment;
            return true;
        } else if(candidates.size() == 0) {
            throw new IncorrectNotationException("No valid move found: " + moveData.san);
        }
        return false;
    }

    private Game.Result getResult(String str) {
        Game.Result result = null;
        switch (str) {
            case "1-0" -> result = Game.Result.WHITE_WINS;
            case "0-1" -> result = Game.Result.BLACK_WINS;
            case "1/2-1/2" -> result = Game.Result.DRAW;
        }
        return result;
    }

    private Game.Termination getTermination(String str) {
        Game.Termination t = Game.Termination.ABANDONED;
        switch (str) {
            case "normal" -> t = Game.Termination.NORMAL;
            case "time forfeit" -> t = Game.Termination.TIME_FORFEIT;
            case "unterminated" -> t = null;
        }
        return t;
    }

    private class PGNTokenizer {
        private record Tag(String name, String value) {}
        private record Move(String san, String comment) {}

        private class Index implements Cloneable {
            public int idx = 0, line = 1, column = 0;

            @Override
            public Index clone() {
                try {
                    Index clone = (Index) super.clone();
                    clone.idx = idx;
                    clone.line = line;
                    clone.column = column;
                    return clone;
                } catch (CloneNotSupportedException e) {
                    throw new AssertionError();
                }
            }

            public void step(char c) {
                idx++;
                if(c == '\n') {
                    line++;
                    column = 0;
                } else {
                    column++;
                }
            }

            @Override
            public String toString() {
                return "line " + line + " column " + column;
            }
        }

        public final LinkedList<Tag> tags = new LinkedList<>();
        public final LinkedList<Move> moves = new LinkedList<>();
        private Index idx = new Index();

        public PGNTokenizer() throws IncorrectNotationException {
            while (idx.idx < pgn.length()) {
                dropWS();
                char c = seeNext();
                if (c == '[')
                    readTag();
                else if (Character.isDigit(c))
                    break;
            }

            while (idx.idx < pgn.length()) {
                dropWS();
                String str = nextStr();

                if(isResult(str))
                    return;
                else
                    readMove();
            }
        }

        private char seeNext() {
            return pgn.charAt(idx.idx);
        }

        private void dropWS() {
            while (true) {
                char c = seeNext();
                if(!Character.isWhitespace(c)) {
                    return;
                } else {
                    next();
                }
            }
        }

        private char next() {
            char c = seeNext();
            idx.step(c);
            return c;
        }

        private String nextStr() {
            StringBuilder str = new StringBuilder();
            Index startIdx = idx.clone();

            dropWS();
            while (idx.idx < pgn.length()) {
                char c = seeNext();
                if (Character.isWhitespace(c))
                    break;
                str.append(next());
            }
            return str.toString();
        }

        private void readTag() throws IncorrectNotationException {
            StringBuilder tagStr = new StringBuilder();
            Index startIdx = idx.clone();

            if(next() != '[')
                throw new IncorrectNotationException("Expected tag at " + startIdx);

            try {
                while (true) {
                    char c = next();
                    if (c == ']') {
                        addTag(tagStr.toString(), startIdx);
                        break;
                    }
                    tagStr.append(c);
                }
            } catch (IndexOutOfBoundsException ex) {
                throw new IncorrectNotationException("Expected ']' for '[' at " + idx);
            }
        }

        private void addTag(String str, Index start) throws IncorrectNotationException {
            String[] tag = str.split(" ", 2);
            if(tag.length != 2)
                throw new IncorrectNotationException("Expected two elements in tag at " + start);

            String name = tag[0];
            String value = tag[1];
            if(value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"')
                throw new IncorrectNotationException("Invalid value in tag at " + start);

            value = value.substring(1, value.length() - 1);

            tags.add(new Tag(name, value));
        }

        private void readMove() throws IncorrectNotationException {
            for(int i = 0; i < 2 ; i++) {
                String[] moveData = { null, null };

                eatVariations();

                dropWS();
                if (Character.isDigit(seeNext()))
                    return;

                moveData[0] = nextStr();
                moveData[1] = readComments();
                eatVariations();

                moves.add(new Move(moveData[0], moveData[1]));
            }
        }

        private void eatVariations() throws IncorrectNotationException {
            while(eatVariation());
        }

        private String readComments() throws IncorrectNotationException {
            String comment = null;
            while(true) {
                String append = readComment();
                if(append != null)
                    comment = comment == null ? append : comment + append;
                else
                    break;
            }
            return comment;
        }

        private String readComment() throws IncorrectNotationException {
            dropWS();
            if(seeNext() != '{')
                return null;

            StringBuilder commentStr = new StringBuilder();
            Index startIdx = idx.clone();
            next();
            try {
                while (true) {
                    char c = next();
                    if (c == '}')
                        return commentStr.toString();
                    commentStr.append(c);
                }
            } catch (IndexOutOfBoundsException ex) {
                throw new IncorrectNotationException("Expected '}' pair for '{' at " + idx);
            }
        }

        private boolean eatVariation() throws IncorrectNotationException {
            dropWS();
            if(seeNext() != '(')
                return false;

            Index startIdx = idx.clone();
            next();
            try {
                while (true) {
                    char c = next();
                    if(c == '(')
                        eatVariation();
                    else if (c == ')')
                        return true;
                }
            } catch (IndexOutOfBoundsException ex) {
                throw new IncorrectNotationException("Expected ')' pair for '(' at " + idx);
            }
        }

        private boolean isResult(String str) {
            boolean result = false;
            switch (str) {
                case "1-0", "0-1", "1/2-1/2", "*" -> result = true;
            }
            return result;
        }
    }
}
