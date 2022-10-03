package engine;

import game.Game;

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

    void setOption(String option, String value);

    String getEngineName();
}
