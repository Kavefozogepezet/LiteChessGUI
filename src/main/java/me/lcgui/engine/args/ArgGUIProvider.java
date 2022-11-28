package me.lcgui.engine.args;

/**
 * Az argumentum leszármazottak grafikus megjelenítésének elkészítéséhez használatos interface.
 * A leszármazottak a konstruktorukban kapjanak egy {@link AbstractArg} típust, amelyhez a grafikus
 * megjelenítést elkészítik. A {@link AbstractArg#accept} függvény meghívja saját tíusával a createGUIObject függvényt.
 */
public interface ArgGUIProvider {
    /**
     *A függvény overload-jai elkészítik a típus specifikus megjelenítését az argumentumnak.
     * @param arg Az argumentum, amihez megjelenítést készítünk.
     */
    void createGUIObject(AbstractArg<?> arg);

    void createGUIObject(Args.Check arg);
    void createGUIObject(Args.Button arg);
    void createGUIObject(Args.Str arg);
    void createGUIObject(Args.Combo arg);
    void createGUIObject(Args.Spin arg);
}
