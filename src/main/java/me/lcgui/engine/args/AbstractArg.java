package me.lcgui.engine.args;

import java.io.Serializable;

// TODO getDefault, isDefault
public abstract class AbstractArg<Type> implements Serializable {
    protected final String name;

    public AbstractArg(String name) {
        this.name = name;
    }

    public abstract boolean hasValue();

    public abstract Type getValue();
    public abstract void setValue(Type value);

    public abstract void accept(ArgGUIProvider guiProvider);

    public String getName() {
        return name;
    }

    @Override
    public abstract String toString();
}

