package me.lcgui.player;

import jdk.jshell.spi.ExecutionControl;
import me.lcgui.app.LiteChessGUI;
import me.lcgui.engine.Engine;
import me.lcgui.engine.EngineManager;
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
public class EnginePlayer implements Player {
    private Game game = null;
    private Side mySide = null;
    private final Engine engine;
    private boolean myTurn = false;

    public EnginePlayer(Engine engine) {
        this.engine = engine;
        engine.addMoveListener(onBestEvent);
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
        engine.removeMoveListener(onBestEvent);
        engine.release();
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

    private final Event.Listener<Consumable<Move>> onBestEvent = (Consumable<Move> c) -> {
        Move move = c.getData();
        if(myTurn && !c.isConsumed()) {
            c.consume();
            if(move == null)
                game.resign();
            else
                game.play(move);
            myTurn = false;
        }
    };
}
