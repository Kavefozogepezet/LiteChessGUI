package player;

import GUI.BoardView;
import GUI.GameView;
import game.Game;
import game.event.SquareListener;
import game.movegen.Move;
import game.board.Square;

import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class HumanPlayer implements Player {
    GameView gameView;
    Game game = null;
    private boolean myTurn = false;
    private LinkedList<Move> selMoves = null;
    private Square sel = null;

    public HumanPlayer(GameView gameView) {
        this.gameView = gameView;
        gameView.getBoardView().addSquareListener(new SquareListener() {
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

                if(gameView.getBoardView().getPiece(sq) != null) {
                    sel = sq;
                    selMoves = gameView.getGame().getPossibleMoves().from(sq);
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

    private void clearHighlights() {
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
    public void bind(Game game) {
        if(this.game == null)
            this.game = game;
    }

    @Override
    public String getName() {
        return "Human";
    }
}
