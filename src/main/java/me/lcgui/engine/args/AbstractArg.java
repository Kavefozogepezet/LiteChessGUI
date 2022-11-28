package me.lcgui.engine.args;

import me.lcgui.misc.Event;

import java.io.Serializable;

/**
 * Egy engine argumentumát tároló abstract alap osztály.
 * @param <Type> A tárolt argumantum típusa.
 */
public abstract class AbstractArg<Type> implements Serializable {
    protected final String name;

    /**
     * Az argumentum megváltoztatásának eseménye.
     */
    public final Event<Type> changedEvent = new Event<>();

    protected final Type dValue;
    protected Type value;

    /**
     * @param name Az argumentum neve
     * @param value Az argumentum alapértéke
     */
    public AbstractArg(String name, Type value) {
        this.name = name;
        this.dValue = value;
        setValue(value);
    }

    /**
     * Megadja hogy az argumentumnak van e ételmezhető értéke.
     * Például signal szerű argumentumoknál hamis.
     * @return Igaz ha van értéke, hamis ha nincs.
     */
    public abstract boolean hasValue();

    /**
     * Argumentum alapértékének a lekérése.
     * @return Az argumentum alapértéke.
     */
    public Type getDefault() {
        return dValue;
    }

    /**
     * Megadja, hogy az argumentum alapállapotban van-e.
     * @return Igaz, ha az argumentum értéke megegyezik az alapértékkel.
     */
    public boolean isDefault() {
        return dValue.equals(value);
    }

    /**
     * Visszaállítja az argumentumot az alapértékére.
     */
    public void reset() {
        value = dValue;
        changedEvent.invoke(value);
    }

    /**
     * Megadja az argumentum aktuális értékét.
     * @return Az aktuális érték.
     */
    public Type getValue() {
        return value;
    }

    /**
     * Beállítja az argumentum értékét.
     * @param value Az új érték.
     */
    public void setValue(Type value) {
        if(this.value == null || !this.value.equals(value)) {
            this.value = value;
            changedEvent.invoke(this.value);
        }
    }

    /**
     * Segédfüggvény az argumentumok grafikus megjelenítéséhez.
     * A leszármazott mehívja a megfelelő {@link ArgGUIProvider#createGUIObject} függvényt,
     * így a provider elkészítheti az argumentum specifikus megjelenítést.
     * @param guiProvider
     */
    public abstract void accept(ArgGUIProvider guiProvider);

    /**
     * Megadja az argumentum nevét.
     * @return A név.
     */
    public String getName() {
        return name;
    }

    /**
     * Szöveges formában megadja az argumentum típusát.
     * @return A típus string-je.
     */
    public abstract String getType();

    @Override
    public String toString() {
        return value.toString();
    }
}

