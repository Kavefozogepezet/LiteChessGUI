package player;

import GUI.BoardView;
import GUI.GameView;
import game.event.SquareListener;
import game.movegen.Move;
import game.board.Square;

import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class HumanPlayer implements Player {
    private GameView game;
    private boolean myTurn = false;
    private LinkedList<Move> selMoves = null;
    private Square sel = null;

    public HumanPlayer(GameView game) {
        this.game = game;

        game.getBoardView().addSquareListener(new SquareListener() {
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

                if(game.getBoardView().getPiece(sq) != null) {
                    sel = sq;
                    selMoves = game.getGame().getMoves(sq);
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
                game.getBoardView().setSqHighlight(move.to, BoardView.SqMoveHL.None);

        if(sel != null)
            game.getBoardView().setSqHighlight(sel, BoardView.SqMoveHL.None);
    }

    private void setHighlight() {
        if(selMoves != null)
            for (var move : selMoves)
                game.getBoardView().setSqHighlight(move.to, BoardView.SqMoveHL.Move);

        if(sel != null)
            game.getBoardView().setSqHighlight(sel, BoardView.SqMoveHL.Selected);
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
