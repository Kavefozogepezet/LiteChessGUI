package player;

import board.*;

import java.awt.event.MouseEvent;

public class HumanPlayer implements Player {
    private boolean myTurn = false;
    private Square from = null, to = null;

    public HumanPlayer(Board board) {
        board.addSquareListener(new SquareListener() {
            @Override
            public void squareClicked(Square sq, MouseEvent e) {
                if(!(e.getButton() == MouseEvent.BUTTON1 && myTurn))
                    return;

                Piece piece = board.getPiece(sq);
                if(from == null && piece != null && piece.side == board.getState().getTurn()) {
                    from = sq;
                } else if(from != null && to == null && (piece == null || board.getState().getTurn() != piece.side)) {
                    to = sq;
                    Move move = new Move(from, to, board.getPiece(from), piece, 0);
                    board.play(move);
                    from = null;
                    to = null;
                }
            }

            @Override
            public void squarePressed(Square sq, MouseEvent e) {

            }

            @Override
            public void squareReleased(Square sq, MouseEvent e) {

            }
        });
    }

    @Override
    public void myTurn() {
        myTurn = true;
    }

    @Override
    public void cancelTurn() {
        myTurn = false;
    }
}
