package me.lcgui.engine;

/**
 * Egy engine keresési kimenetét tároló osztály.
 * Az értékeket egy tömbben tárolja szöveges formában.
 * A tárolt értékek az indexükkel érhetőek el.
 */
public class SearchInfo implements Cloneable {
    /**
     * A tárolt adatok elnevezése sorrendben.
     */
    public static final String[] InfoNames = {
            "depth", "score", "time", "nodes", "nps", "pv"
    };

    /**
     * A tárolt adatok egy indexe.
     * Ez használandó a get/set függvény meghívásánál.
     */
    public static final int DEPTH = 0,  SCORE = 1, TIME = 2, NODES = 3, NPS = 4, PV = 5, INFO_COUNT = 6;

    /**
     * A keresési kimenetet ez a tömb tárolja.
     */
    public String[] array = new String[6];

    private boolean dirty = false;

    public SearchInfo() {}

    /**
     * Megadja a kért adatot az indexe alapján.
     * @param idx A kért adat indexe.
     * @return Az adat szövegesen.
     */
    public String get(int idx) {
        return array[idx];
    }

    /**
     * Beállítja a megfelelő adatot, ha az különbözött az eddig beállítotttól.
     * Amennyiben igen, megjelöli magát mint dirty, így megállapítható,
     * hogy az engine tényleg új információt közölt, vagy az előzőt ismételte meg.
     * @param idx Az adat indexe.
     * @param value Az adat.
     */
    public void set(int idx, String value) {
        if(!value.equals(array[idx])) {
            array[idx] = value;
            dirty = true;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SearchInfo other))
            return false;

        boolean eq = true;
        for(int i = 0; i < 6; i++)
            eq = eq && array[i].equals(other.array[i]);

        return eq;
    }

    /**
     * Megadja, hogy az adatok állapota megváltozott-e a legutóbbi isDirty() hívás óta.
     * @return igaz, ha az adatot megváltoztatták.
     */
    public boolean isDirty() {
        boolean temp = dirty;
        dirty = false;
        return temp;
    }

    @Override
    public SearchInfo clone() {
        try {
            SearchInfo clone = (SearchInfo) super.clone();
            clone.array = array.clone();
            clone.dirty = false;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
