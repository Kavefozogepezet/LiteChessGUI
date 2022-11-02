package me.lcgui.misc;

public class Consumable<Type> {
    private boolean consumed = false;
    private final Type obj;

    public static <Type> Consumable<Type> create(Type data) {
        return new Consumable<>(data);
    }

    public boolean isConsumed() {
        return consumed;
    }

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
