package me.lcgui.engine;

import jdk.jshell.spi.ExecutionControl;
import me.lcgui.game.Game;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

// TODO
@ProtocolImplementation(name = "Winboard")
public class WBEngine implements Engine {
    public WBEngine() {
        throw new RuntimeException(new ExecutionControl.NotImplementedException("Winboard is not supported yet"));
    }

    @Override
    public void verify() throws EngineVerificationFailure {

    }

    @Override
    public void isReady() {

    }

    @Override
    public void quit() {

    }

    @Override
    public void startSearch() {

    }

    @Override
    public void stopSearch() {

    }

    @Override
    public boolean isSearching() {
        return false;
    }

    @Override
    public Event<Consumable<String>> getBestEvent() {
        return null;
    }

    @Override
    public Event<SearchInfo> getInfoEvent() {
        return null;
    }

    @Override
    public Event<ComData> getComEvent() {
        return null;
    }

    @Override
    public void playingThis(Game game) {

    }

    @Override
    public Game getCurrentGame() {
        return null;
    }

    @Override
    public void setInitStart(boolean initStart) {

    }

    @Override
    public void setOption(String option, String value) {

    }

    @Override
    public String getEngineName() {
        return null;
    }

    @Override
    public void run() {

    }
}
