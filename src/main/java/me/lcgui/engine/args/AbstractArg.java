package me.lcgui.engine.args;

import java.awt.*;
import java.io.Serializable;

public abstract class AbstractArg<Type> implements Serializable {
    public final String name;

    public AbstractArg(String name) {
        this.name = name;
    }

    public abstract boolean hasValue();

    public abstract Type getValue();

    public abstract Component getOptionDisplay();

    @Override
    public abstract String toString();
}

