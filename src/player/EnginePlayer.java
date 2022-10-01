package player;

import engine.Engine;
import engine.EngineConfig;
import engine.EngineListener;
import engine.UCIEngine;
import game.Game;
import game.board.Square;
import game.movegen.Move;

import javax.swing.*;

public class EnginePlayer implements Player {
    private Game game = null;
    private final Engine engine;
    private boolean myTurn = false;

    public EnginePlayer(Engine engine) {
        this.engine = engine;
        engine.addListener(new MoveListener());
    }

    private Move convertMove(String moveStr) {
        // TODO testing from && to && promotion instead
        Square from = Square.parse(moveStr.substring(0, 2));
        var list = game.getMoves(from);

        if(list != null) {
            for (var move : game.getMoves(from)) {
                if (move.toString().equals(moveStr))
                    return move;
            }
        }
        return null;
    }

    private void playMove(String moveStr) {
        Move move = convertMove(moveStr);
        if(move == null) {
            System.out.println("Invalid move detected");
            game.resign();
        }
        myTurn = false;
        game.play(move);
    }

    @Override
    public void myTurn() {
        if(game != engine.getCurrentGame()) //we need equal references
            engine.playingThis(game);

        myTurn = true;
        engine.startSearch();
    }

    @Override
    public void cancelTurn() {
        myTurn = false;
        engine.stopSearch();
    }

    @Override
    public void bind(Game game) {
        if(this.game == null)
            this.game = game;
    }

    private class MoveListener implements engine.EngineListener {

        @Override
        public void bestmove(String s) {
            if(!myTurn)
                return;
            SwingUtilities.invokeLater(() -> playMove(s));
        }

        @Override
        public void info(String[] info) {

        }
    }
}
