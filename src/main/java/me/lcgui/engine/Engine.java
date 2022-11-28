package me.lcgui.engine;

import me.lcgui.game.Game;
import me.lcgui.game.player.MoveSupplier;
import me.lcgui.misc.Event;

/**
 * Interface az engine kommunikációs protokollt megvalósító osztályokhoz.
 * Úgy kell megvalósítani, hogy külön szálon futtatva is működjön.
 */
public interface Engine extends Runnable, MoveSupplier {
    /**
     * Vár, amíg meg nem bizonyosodik hogy az elindított engine sikeresen elindult, és a megfelelő protokollt használja.
     * @throws EngineVerificationFailure Ha az engine nem indul, vagy nem megfelelő protokollt használ.
     */
    void verify() throws EngineVerificationFailure;

    /**
     * Addig vár, amíg az engine nem lesz ismét parancs fogadásra képes állapotban.
     */
    void isReady();

    /**
     * Leállítja az engine-t
     */
    void quit();

    /**
     * Jelzi az engine számára, hogy aki lefoglalta, nem tart többé igényt rá.
     * A leszármazottaknak implementálniuk kell egy {@link Event} példányt, amely invoke-ol, ha ez a metódus meghívódik.
     * Ennek hatására az {@link EngineManager} felülvizsgálja hogy az engine-nek van-e még használója.
     * Ha nincs, az engine leáll.
     */
    void release();

    /**
     * Az aktuális pozíción elkezdi a keresést.
     */
    void startSearch();

    /**
     * Félbeszakítja a keresést.
     */
    void stopSearch();

    /**
     * Megadja, hogy az engine éppen keresést folytat-e.
     * @return Igaz, ha éppen keres.
     */
    boolean isSearching();

    /**
     * @return Olyan esemény, amely új keresési információ érkeztét jelöli.
     */
    Event<SearchInfo> getInfoEvent();

    /**
     * @return Olyan esemény, amely bármilyen GUI és engine közötti komunikációt jelöl.
     */
    Event<ComData> getComEvent();

    /**
     * @return Olyan esemény, amely a {@link Engine#release()} meghívásakor jelez.
     */
    Event<Engine> getReleasedEvent();

    /**
     * Az engine által játszott partit átállítja.
     * @param game Az új parti.
     */
    void playingThis(Game game);

    /**
     * Megadja az engine által játszott partit.
     * @return A parti.
     */
    Game getCurrentGame();

    /**
     * Beállítja, hogy az engine-t telepítési szándékkal futtatjuk-e.
     * Amennyiben igen, elvárt, hogy az engine a hozzá tartozó {@link EngineConfig} példány argumentumait frissítse.
     * @param initStart igaz, ha telepítés céljából futtatunk.
     */
    void setInitStart(boolean initStart);

    /**
     * Az engine egy argumentumát azonnal beállítja.
     * @param option Az argumentum neve.
     * @param value Az argumentum szöveges értéke.
     */
    void setOption(String option, String value);

    /**
     * Megadja az engine nevét. Ez vagy a futtatható állomány neve, vagy az, amit
     * az engine közölt a kimenetén.
     * @return
     */
    String getEngineName();
}
