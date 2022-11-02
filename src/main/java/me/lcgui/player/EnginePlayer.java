package me.lcgui.player;

import jdk.jshell.spi.ExecutionControl;
import me.lcgui.app.LiteChessGUI;
import me.lcgui.engine.Engine;
import me.lcgui.engine.EngineVerificationFailure;
import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.Move;
import me.lcgui.gui.factory.EnginePlayerFactory;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

import javax.swing.*;
import java.io.Serial;
import java.io.Serializable;

@SelectablePlayer(name = "Engine", factoryClass = EnginePlayerFactory.class)
public class EnginePlayer implements Player, Serializable {
    private transient Game game = null;
    private Side mySide = null;
    private transient Engine engine;
    private final String engineName;
    private transient boolean myTurn = false;

    public EnginePlayer(String engineName) throws ExecutionControl.NotImplementedException, EngineVerificationFailure {
        this.engineName = engineName;
        this.engine = LiteChessGUI.engineManager.getInstance(engineName);
        engine.getBestEvent().addListener(onBestEvent);
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
        engine.getBestEvent().removeListener(onBestEvent);
        LiteChessGUI.engineManager.releaseInstance(engine);
    }

    @Override
    public void handleDrawClaim() {
        if(mySide == game.getState().getTurn())
            game.draw();
    }

    @Override
    public String getName() {
        return engine.getEngineName();
    }

    public Engine getEngine() {
        return engine;
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

    private final Event.Listener<Consumable<String>> onBestEvent = (Consumable<String> c) -> {
        String moveStr = c.getData();
        if(myTurn && !c.isConsumed()) {
            c.consume();
            playMove(moveStr);
        }
    };

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
        engine.getBestEvent().addListener(onBestEvent);
        return this;
    }
}
