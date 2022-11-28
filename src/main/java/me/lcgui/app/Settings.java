package me.lcgui.app;

import me.lcgui.engine.args.Args;
import me.lcgui.gui.BoardStyle;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Beállítások egyszerű tárolására használható osztály.
 * Az egyes bellításokat String Object párokként tárolja,
 * így a beállítások listája könnyen változtatható és bővíthető.
 * Csak {@link Serializable} Osztályok példányai tárolhatók, ez garantálja a fájlba menthetőséget.
 */
public class Settings implements Serializable {
    private final HashMap<String, Serializable> settings = new HashMap<>();

    /**
     * Egy String-ből és Object-ből {@link Map.Entry}-t készít.
     * Ha a {@link Settings#withDefaults} funkciót használja, hogy alapbeálításokkalellátott Setting példányt készítsen,
     * az entry-ket ezzel a függvénnyel egyszerűen előállíthatja.
     * @param name A beállítás neve
     * @param value A beállítás értéke
     * @return A létrehozott entry
     */
    public static Map.Entry<String, ? extends Serializable>
    setting(String name, Serializable value) {
        return Map.entry(name, value);
    }

    /**
     * Létrehoz egy alapbeállításokkal rendelkező Setting példányt.
     * @param entries A {@link Map.Entry} példányok, amelyek az alap beállításokat tárolják
     * @return Setting példány az alapbeállításokkal
     */
    @SafeVarargs
    public static Settings withDefaults(Map.Entry<String, ? extends Serializable> ... entries) {
        Settings s = new Settings();
        for(var entry : entries)
            s.set(entry.getKey(), entry.getValue());

        return s;
    }

    /**
     * Megváltoztatja a megadott beállítást.
     * Ha nincs még ilyen, létrehozza a megadott értékkel.
     * @param name A beállítás neve.
     * @param value A beállítás értéke.
     */
    public void set(String name, Serializable value) {
        settings.put(name, value);
    }

    /**
     * Lekéri a megadott beállítást.
     * @param name A beállítás neve.
     * @return A beállítás értéke. Ha ne létezik a beállítás, null az értéke.
     */
    public Serializable get(String name) {
        return settings.get(name);
    }

    /**
     * Konkrét típusú beállítás lekérése.
     * Ha a beállítás nem létezik, vagy a megadott típus nem egyezett a tényleges érték típusával, az alapértékkel tér vissza.
     * Ha az alapértéket adja vissza, az nem írja felül a tárolt beállíást, és nem is hozza létre, ha még nem létezett volna.
     * @param name A beállítás neve.
     * @param defaultValue A beállítás alapértéke.
     * @return A beállítás, vagy az alapérék.
     * @param <Type> A beállítás feltételezett típusa.
     */
    @SuppressWarnings("unchecked")
    public <Type> Type get(String name, Type defaultValue) {
        try {
            var clazz = (Class<? extends Type>) defaultValue.getClass();
            Type value = clazz.cast(get(name));
            return value == null ? defaultValue : value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
}
