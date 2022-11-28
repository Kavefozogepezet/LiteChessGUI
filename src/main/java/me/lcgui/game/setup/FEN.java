package me.lcgui.game.setup;

import me.lcgui.game.Game;
import me.lcgui.game.IncorrectNotationException;
import me.lcgui.game.board.*;

import java.io.Serializable;

/**
 * FEN jelölést tartalmazó osztály.
 */
public class FEN implements GameSetup, Serializable {
    public static final String STARTPOS_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String[] castling = {
            "-", "K", "Q", "KQ", "k", "Kk", "Qk", "KQk", "q", "Kq", "Qq", "KQq", "kq", "Kkq", "Qkq", "KQkq"
    };

    private String fen = STARTPOS_FEN;

    /**
     * FEN jelölésből készült példány, amit parti felállításához lehet majd használni.
     * @param fen A jelölés.
     */
    public FEN(String fen) {
        this.fen = fen;
    }

    /**
     * {@link Game} példányból készült FEN jelölés, ami a parti aktuális állapotát ábrázolja.
     * @param game A parti, amit a FEN ábrázolni fog.
     */
    public FEN(Game game) {
        StringBuilder fenBuilder = new StringBuilder();
        Board board = game.getBoard();

        int emptySquares = 0;
        for(int rank = Board.BOARD_SIZE - 1; rank >= 0; rank--) {
            for(int file = 0; file < Board.BOARD_SIZE; file++) {
                Piece piece = board.getPiece(file, rank);;

                if(piece == null) {
                    emptySquares++;
                    continue;
                }

                if(emptySquares != 0) {
                    fenBuilder .append(emptySquares);
                    emptySquares = 0;
                }

                fenBuilder.append(piece.toString());
            }

            if(emptySquares != 0) {
                fenBuilder.append(emptySquares);
                emptySquares = 0;
            }

            if(rank != 0)
                fenBuilder.append('/');
        }

        State state = game.getState();
        fenBuilder
                .append(" ").append(state.getTurn() == Side.White ? "w" : "b")
                .append(" ").append(castling[state.getCastleRights()])
                .append(" ").append(state.getEpTarget().toString())
                .append(" ").append(state.get50move())
                .append(" ").append(state.getPly() / 2 + 1);

        fen = fenBuilder.toString();
    }

    /**
     * Beállítja a megadott parti kezdőpozicióját.
     * @param game A parti.
     * @throws IncorrectNotationException A FEN jelölés érvénytelen volt.
     */
    public void set(Game game) throws IncorrectNotationException {
        String[] sections = fen.split(" ");

        int file = 0, rank = AbstractBoard.BOARD_SIZE - 1;

        for(char c : sections[0].toCharArray()) {
            if(c == '/') {
                rank--;
                file = 0;
            } else {
                if(Character.isDigit(c))
                    file += c - '0';
                else {
                    Piece p = Piece.fromChar(c);
                    if(p == null)
                        throw new IncorrectNotationException(
                                "The character '" + c + "' is not a piece nor a rank break.");

                    game.getBoard().setPiece(file, rank, p);
                    file++;
                }
            }
        }

        Side turn = switch (sections[1]) {
            case "w" -> Side.White;
            case "b" -> Side.Black;
            default -> throw new IncorrectNotationException("Side to play is invalid (w/b)");
        };

        int castleRights = 0;
        if(!"-".equals(sections[2])) {
            for (char c : sections[2].toCharArray()) {
                switch (c) {
                    case 'Q' -> castleRights |= State.CASTLE_WQ;
                    case 'K' -> castleRights |= State.CASTLE_WK;
                    case 'q' -> castleRights |= State.CASTLE_BQ;
                    case 'k' -> castleRights |= State.CASTLE_BK;
                    default -> throw new IncorrectNotationException("Castle right is invalid (Q/K/q/k)");
                }
            }
        }

        Square epTarget = Square.parse(sections[3]);
        if(!("-".equals(sections[3]) ||epTarget.valid()))
            throw new IncorrectNotationException("En passant target square is invalid.");

        int ply50, ply;
        try {
            ply50 = Integer.parseInt(sections[4]);
            ply = (Integer.parseInt(sections[5]) - 1) * 2;
        } catch (NumberFormatException e) {
            throw new IncorrectNotationException(e);
        }

        if(turn == Side.Black)
            ply++;

        game.setState(new State(turn, castleRights, epTarget, ply, ply50));
    }

    @Override
    public String toString() {
        return fen;
    }

    /**
     * @param obj FEN vagy String típusú objektum.
     * @return igaz, ha a jelölés azonos.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FEN other)
            return fen.equals(other.fen);
        if(obj instanceof String fenStr)
            return fen.equals(fenStr);
        return false;
    }
}
