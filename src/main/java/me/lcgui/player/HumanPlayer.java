package me.lcgui.player;

import me.lcgui.app.LiteChessGUI;
import me.lcgui.app.Settings;
import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
import me.lcgui.game.movegen.Move;
import me.lcgui.gui.LCGUIWindow;
import me.lcgui.gui.MoveSupplier;
import me.lcgui.gui.factory.HumanPlayerFactory;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

import java.io.Serializable;

@SelectablePlayer(name = "Human", factoryClass = HumanPlayerFactory.class, canUseGUI = true)
public class HumanPlayer implements Player {
    public static final String AUTO_DRAW = "auto_draw";

    private MoveSupplier supplier;

    private final String name;
    private Game game = null;
    private Side mySide = null;
    private boolean myTurn = false;

    public HumanPlayer(String name) {
        this.name = name;
    }

    public HumanPlayer() {
        this("Human");
    }

    @Override
    public void myTurn() {
        myTurn = true;
    }

    @Override
    public void cancelTurn() {
        myTurn = false;
    }

    @Override
    public void setGame(Game game, Side side) {
        if(this.game != null)
            throw new RuntimeException("The game can be set only once.");
        this.game = game;
        this.mySide = side;
    }

    @Override
    public void gameEnd() {
        if(supplier != null)
            supplier.removeMoveListener(onMoveInput);
    }

    public void setMoveSupplier(MoveSupplier supplier) {
        if(this.supplier != null)
            this.supplier.removeMoveListener(onMoveInput);
        this.supplier = supplier;
        this.supplier.addMoveListener(onMoveInput);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void handleDrawClaim() {
        boolean can = mySide == game.getState().getTurn() && game.canClaimDraw();
        if(can && LiteChessGUI.settings.get(AUTO_DRAW, false))
            game.draw();
    }

    private final Event.Listener<Consumable<Move>> onMoveInput = (Consumable<Move> move) -> {
        if(myTurn && !move.isConsumed()) {
            game.play(move.getData());
            move.consume();
            myTurn = false;
        }
    };
}
