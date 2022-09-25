package board.setup;

import board.Board;
import board.types.Piece;
import board.types.Side;
import board.types.Square;

public class Fen implements ChessNotation {
    private String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public Fen(String fen) {
        this.fen = fen;
    }

    public void set(Board board) {
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
                    board.setPiece(file, rank, Piece.fromChar(c));
                    file++;
                }
            }
        }

        Side turn = sections[1].equals("w") ? Side.White : Side.Black;

        int castleRights = 0;
        if(sections[2].contains("Q")) castleRights |= Board.State.CASTLE_WQ;
        if(sections[2].contains("K")) castleRights |= Board.State.CASTLE_WK;
        if(sections[2].contains("k")) castleRights |= Board.State.CASTLE_BQ;
        if(sections[2].contains("q")) castleRights |= Board.State.CASTLE_BK;

        Square epTarget = Square.fromString(sections[3]);

        int ply = Integer.parseInt(sections[5]) - 1;

        board.setState(new Board.State(turn, castleRights, epTarget, ply));
    }

    @Override
    public String get(Board board) {
        return "";
    }
}
