package player;

import GUI.BoardView;
import GUI.GameView;
import game.Game;
import game.event.SquareListener;
import game.movegen.Move;
import game.board.Square;

import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.LinkedList;

public class HumanPlayer implements Player, Serializable {
    private transient GameView gameView = null;
    private transient SquareListener myListener = null;

    private Game game = null;
    private boolean myTurn = false;
    private transient LinkedList<Move> selMoves = null;
    private transient Square sel = null;

    private void clearHighlights() {
        if(gameView == null)
            return;

        if(selMoves != null)
            for (var move : selMoves)
                gameView.getBoardView().setSqHighlight(move.to, BoardView.SqMoveHL.None);

        if(sel != null)
            gameView.getBoardView().setSqHighlight(sel, BoardView.SqMoveHL.None);
    }

    private void setHighlight() {
        if(selMoves != null)
            for (var move : selMoves)
                gameView.getBoardView().setSqHighlight(move.to, BoardView.SqMoveHL.Move);

        if(sel != null)
            gameView.getBoardView().setSqHighlight(sel, BoardView.SqMoveHL.Selected);
    }

    @Override
    public void myTurn() {
        myTurn = true;
    }

    @Override
    public void cancelTurn() {
        myTurn = false;
        clearHighlights();
    }

    @Override
    public void setGame(Game game) {
        clearHighlights();
        sel = null;
        selMoves = null;
        this.game = game;
    }

    public void setGameView(GameView view) {
        if(gameView != null)
            gameView.getBoardView().removeSquareListener(myListener);

        gameView = view;
        myListener = new SquareListener() {
            @Override
            public void squareClicked(Square sq, MouseEvent e) {
                if(!(e.getButton() == MouseEvent.BUTTON1 && myTurn))
                    return;

                clearHighlights();

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
                    myTurn = false;
                    return;
                }

                if(view.getBoardView().getPiece(sq) != null) {
                    sel = sq;
                    selMoves = view.getGame().getPossibleMoves().from(sq);
                }
                setHighlight();
            }

            @Override
            public void squarePressed(Square sq, MouseEvent e) {}

            @Override
            public void squareReleased(Square sq, MouseEvent e) {}
        };
        gameView.getBoardView().addSquareListener(myListener);
    }

    @Override
    public String getName() {
        return "Human";
    }
}
