package game.setup;

import GUI.BoardView;
import game.Game;
import game.board.*;

public class Fen implements GameSetup {
    public static final String STARTPOS_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String[] castling = {
            "-", "K", "Q", "KQ", "k", "Kk", "Qk", "KQk", "q", "Kq", "Qq", "KQq", "kq", "Kkq", "Qkq", "KQkq"
    };

    private String fen = STARTPOS_FEN;

    public Fen(String fen) {
        this.fen = fen;
    }

    public Fen(Game game) {
        StringBuilder fenBuilder = new StringBuilder();
        Board board = game.getBoard();

        int emptySquares = 0;
        for(int rank = Board.BOARD_SIZE - 1; rank >= 0; rank++) {
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

            if(emptySquares != 0)
                fenBuilder.append(emptySquares);

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

    public void set(Game game) {
        String[] sections = fen.split(" ");

        int file = 0, rank = BoardView.BOARD_SIZE - 1;

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

        Square epTarget = Square.parse(sections[3]);

        int ply = Integer.parseInt(sections[5]) - 1;

        game.setState(new State(turn, castleRights, epTarget, ply));
        game.setStartFen(fen);
    }

    @Override
    public String toString() {
        return fen;
    }
}
