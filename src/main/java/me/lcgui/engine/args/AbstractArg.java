package me.lcgui.engine.args;

import me.lcgui.misc.Event;

import java.io.Serializable;

// TODO debug default
public abstract class AbstractArg<Type> implements Serializable {
    protected final String name;

    public final Event<Type> changedEvent = new Event<>();

    protected final Type dValue;
    protected Type value;

    public AbstractArg(String name, Type value) {
        this.name = name;
        this.dValue = value;
        setValue(value);
    }

    public abstract boolean hasValue();

    public Type getDefault() {
        return dValue;
    }
    public boolean isDefault() {
        return dValue.equals(value);
    }
    public void reset() { value = dValue; }

    public Type getValue() {
        return value;
    }
    public void setValue(Type value) {
        if(this.value == null || !this.value.equals(value)) {
            this.value = value;
            changedEvent.invoke(this.value);
        }
    }

    public abstract void accept(ArgGUIProvider guiProvider);

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

