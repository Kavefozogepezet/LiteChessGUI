package me.lcgui.engine;

import me.lcgui.game.Game;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

public interface Engine extends Runnable {
    void verify() throws EngineVerificationFailure;
    void isReady(); // waits until engine is ready
    void quit();

    void startSearch();
    void stopSearch();
    boolean isSearching();

    Event<Consumable<String>> getBestEvent();
    Event<SearchInfo> getInfoEvent();
    Event<ComData> getComEvent();

    void playingThis(Game game);
    Game getCurrentGame();

    void setInitStart(boolean initStart);
    void setOption(String option, String value);

    String getEngineName();
}
