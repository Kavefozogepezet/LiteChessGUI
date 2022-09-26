package player;

import game.Game;
import game.board.*;
import game.board.SquareListener;
import game.movegen.Move;
import game.board.Square;

import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class HumanPlayer implements Player {
    private Game game;
    private boolean myTurn = false;
    private LinkedList<Move> selMoves = null;
    private Square sel = null;

    public HumanPlayer(Game game) {
        this.game = game;

        game.getBoard().addSquareListener(new SquareListener() {
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
                    game.play(moveToPlay);
                    return;
                }

                if(game.getBoard().getPiece(sq) != null) {
                    sel = sq;
                    selMoves = game.getMoves(sq);
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
                game.getBoard().setSqHighlight(move.to, Board.SqMoveHL.None);

        if(sel != null)
            game.getBoard().setSqHighlight(sel, Board.SqMoveHL.None);
    }

    private void setHighlight() {
        if(selMoves != null)
            for (var move : selMoves)
                game.getBoard().setSqHighlight(move.to, Board.SqMoveHL.Move);

        if(sel != null)
            game.getBoard().setSqHighlight(sel, Board.SqMoveHL.Selected);
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
