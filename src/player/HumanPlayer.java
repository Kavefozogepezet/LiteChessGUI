package player;

import GUI.BoardView;
import GUI.GameView;
import game.Game;
import game.board.Side;
import game.event.SquareListener;
import game.movegen.Move;
import game.board.Square;

import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.LinkedList;

public class HumanPlayer implements Player, Serializable {
    private transient GameView gameView = null;
    private transient SquareListener myListener = null;

    private final String name;
    private transient Game game = null;
    private transient boolean myTurn = false;
    private transient LinkedList<Move> selMoves = null;
    private transient Square sel = null;

    public HumanPlayer(String name) {
        this.name = name;
    }

    public HumanPlayer() {
        this("Human");
    }

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
    public void setGame(Game game, Side side) {
        if(this.game != null)
            throw new RuntimeException("The game can be set only once.");
        this.game = game;
    }

    @Override
    public void gameEnd() {}

    public void setGameView(GameView view) {
        if(gameView != null)
            gameView.getBoardView().removeSquareListener(myListener);

        gameView = view;
        myListener = new SquareListener() {
            @Override
            public void squareClicked(Square sq, MouseEvent e) {
                if(!gameView.isFollowingGame())
                    return;

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
        return name;
    }
}
