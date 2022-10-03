package engine;

public class SearchInfo implements Cloneable {
    public static final String[] InfoNames = {
            "depth", "score", "time", "nodes", "nps", "pv"
    };

    public static final int DEPTH = 0,  SCORE = 1, TIME = 2, NODES = 3, NPS = 4, PV = 5, INFO_COUNT = 6;

    public String[] array = new String[6];

    public SearchInfo() {}

    public SearchInfo(SearchInfo other) {
        System.arraycopy(other.array, 0, array, 0, 6);
    }

    public String get(int idx) {
        return array[idx];
    }

    public void set(int idx, String value) {
        array[idx] = value;
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
}
