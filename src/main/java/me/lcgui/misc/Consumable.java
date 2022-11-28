package me.lcgui.misc;

/**
 * Olyan {@link Event}-hez használandó csomagoló osztály, amely jelezni tudja hogy az eseményt már valaki feldolgozta.
 * @param <Type> A típus, amit az osztály csomagol.
 */
public class Consumable<Type> {
    private boolean consumed = false;
    private final Type obj;

    /**
     * Létrehoz egy Consumable példányt.
     * @param data A becsomagolandó objektum.
     * @return Az elkészített példány.
     * @param <Type> A becsomagolandó objektum típusa.
     */
    public static <Type> Consumable<Type> create(Type data) {
        return new Consumable<>(data);
    }

    /**
     * @return igaz, ha az eseményt már korábban feldolgozták.
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Az objektum állapotát feldolgozottba állítja.
     */
    public void consume() {
        consumed = true;
    }

    public Type getData() {
        return obj;
    }

    private Consumable(Type obj) {
        this.obj = obj;
    }
}
