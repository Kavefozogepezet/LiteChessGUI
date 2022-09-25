package player;

import board.*;
import board.event.SquareListener;
import board.types.Move;
import board.types.Square;

import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class HumanPlayer implements Player {
    private Board board;
    private boolean myTurn = false;
    private LinkedList<Move> selMoves = null;
    private Square sel = null;

    public HumanPlayer(Board board) {
        this.board = board;

        board.addSquareListener(new SquareListener() {
            @Override
            public void squareClicked(Square sq, MouseEvent e) {
                if(!(e.getButton() == MouseEvent.BUTTON1 && myTurn))
                    return;

                clearHighlight();

                Move moveToPlay = null;
                if(selMoves != null) {
                    for (var move : selMoves) {
                        if (move.to.equals(sq)) {
                            moveToPlay = move;
                            break;
                        }
                    }
                }
                sel = null;
                selMoves = null;

                if(moveToPlay != null) {
                    board.play(moveToPlay);
                    return;
                }

                if(board.getPiece(sq) != null) {
                    sel = sq;
                    selMoves = board.getMoves(sq);
                }
                setHighlight();
            }

            @Override
            public void squarePressed(Square sq, MouseEvent e) {

            }

            @Override
            public void squareReleased(Square sq, MouseEvent e) {

            }
        });
    }

    private void clearHighlight() {
        if(selMoves != null)
            for (var move : selMoves)
                board.setSqHighlight(move.to, Board.SqMoveHL.None);

        if(sel != null)
            board.setSqHighlight(sel, Board.SqMoveHL.None);
    }

    private void setHighlight() {
        if(selMoves != null)
            for (var move : selMoves)
                board.setSqHighlight(move.to, Board.SqMoveHL.Move);

        if(sel != null)
            board.setSqHighlight(sel, Board.SqMoveHL.Selected);
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
