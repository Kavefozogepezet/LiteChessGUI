package player;

import app.LiteChessGUI;
import engine.*;
import game.Game;
import game.board.Side;
import game.board.Square;
import game.movegen.Move;
import jdk.jshell.spi.ExecutionControl;

import javax.swing.*;
import java.io.Serial;
import java.io.Serializable;

public class EnginePlayer implements Player, Serializable {
    private transient Game game = null;
    private Side mySide = null;
    private transient Engine engine;
    private transient MoveListener listener = new MoveListener();
    private final String engineName;
    private transient boolean myTurn = false;

    public EnginePlayer(String engineName) throws ExecutionControl.NotImplementedException, EngineVerificationFailure {
        this.engineName = engineName;
        this.engine = LiteChessGUI.engineManager.getInstance(engineName);
        engine.addListener(new MoveListener());
    }

    private Move convertMove(String moveStr) {
        // TODO testing from && to && promotion instead
        Square from = Square.parse(moveStr.substring(0, 2));
        for (var move : game.getPossibleMoves().from(from)) {
            if (move.toString().equals(moveStr))
                return move;
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
    public void setGame(Game game, Side side) {
        if(this.game != null)
            throw new RuntimeException("The game can be set only once.");
        this.game = game;
        mySide = side;
    }

    @Override
    public void gameEnd() {
        LiteChessGUI.engineManager.releaseInstance(engine);
    }

    @Override
    public String getName() {
        return engine.getEngineName();
    }

    public Engine getEngine() {
        return engine;
    }

    private class MoveListener implements engine.EngineListener {

        @Override
        public void bestmove(String s) {
            if(!myTurn)
                return;
            SwingUtilities.invokeLater(() -> playMove(s));
        }

        @Override
        public void info(SearchInfo info) {}

        @Override
        public void anyCom(boolean isInput, String line) {}
    }

    @Serial
    protected Object readResolve() throws
            ExecutionControl.NotImplementedException,
            EngineVerificationFailure
    {
        engine = LiteChessGUI.engineManager.getInstance(engineName);
        if(engine.isSearching()) {
            engine.stopSearch();
            engine.isReady(); // wait until search is truly stopped
        }
        listener = new MoveListener();
        engine.addListener(listener);
        return this;
    }
}
