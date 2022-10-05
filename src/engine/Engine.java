package engine;

import game.Game;

import java.io.Serializable;

public interface Engine extends Runnable {
    void verify() throws EngineVerificationFailure;
    void isReady(); // waits until engine is ready
    void quit();

    void startSearch();
    void stopSearch();

    void addListener(EngineListener listener);
    void removeListener(EngineListener listener);

    void playingThis(Game game);
    Game getCurrentGame();

    void setInitStart(boolean initStart);
    void setOption(String option, String value);

    String getEngineName();
}
