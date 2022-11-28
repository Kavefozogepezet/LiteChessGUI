package me.lcgui.game.player;

import me.lcgui.game.movegen.Move;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

import java.util.function.Consumer;

public interface MoveSupplier {
    void addMoveListener(Event.Listener<Consumable<Move>> listener);
    void removeMoveListener(Event.Listener<Consumable<Move>> listener);
}
