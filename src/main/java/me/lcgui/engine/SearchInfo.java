package me.lcgui.engine;

public class SearchInfo implements Cloneable {
    public static final String[] InfoNames = {
            "depth", "score", "time", "nodes", "nps", "pv"
    };

    public static final int DEPTH = 0,  SCORE = 1, TIME = 2, NODES = 3, NPS = 4, PV = 5, INFO_COUNT = 6;

    public String[] array = new String[6];
    private boolean dirty = false;

    public SearchInfo() {}

    public String get(int idx) {
        return array[idx];
    }

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
