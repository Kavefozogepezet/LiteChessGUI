package game.setup;

import game.Game;
import game.board.*;

public class Fen implements ChessNotation {
    private String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public Fen(String fen) {
        this.fen = fen;
    }

    public void set(Game game) {
        String[] sections = fen.split(" ");

        int file = 0, rank = Board.BOARD_SIZE - 1;

        for(char c : sections[0].toCharArray()) {
            if(c == '/') {
                rank--;
                file = 0;
            } else {
                if(Character.isDigit(c))
                    file += c - '0';
                else {
                    game.getBoard().setPiece(file, rank, Piece.fromChar(c));
                    file++;
                }
            }
        }

        Side turn = sections[1].equals("w") ? Side.White : Side.Black;

        int castleRights = 0;
        if(sections[2].contains("Q")) castleRights |= State.CASTLE_WQ;
        if(sections[2].contains("K")) castleRights |= State.CASTLE_WK;
        if(sections[2].contains("k")) castleRights |= State.CASTLE_BQ;
        if(sections[2].contains("q")) castleRights |= State.CASTLE_BK;

        Square epTarget = Square.fromString(sections[3]);

        int ply = Integer.parseInt(sections[5]) - 1;

        game.setState(new State(turn, castleRights, epTarget, ply));
    }

    @Override
    public String get(Game game) {
        return "";
    }
}
