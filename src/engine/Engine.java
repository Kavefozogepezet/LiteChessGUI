package engine;

import game.Game;

import java.awt.event.ActionListener;

public interface Engine extends Runnable {
    boolean verificationFailure();
    void isReady(); // waits until engine is ready

    void startSearch();
    void stopSearch();

    void addListener(EngineListener listener);
    void removeListener(EngineListener listener);

    void playingThis(Game game);
    Game getCurrentGame();
}
