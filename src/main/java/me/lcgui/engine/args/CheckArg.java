package me.lcgui.engine.args;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class CheckArg
        extends AbstractArg<Boolean>
        implements Serializable
{
    public boolean value;

    public CheckArg(String name, boolean value) {
        super(name);
        this.value = value;
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public Component getOptionDisplay() {
        var comp = new JCheckBox();
        comp.setSelected(value);
        comp.addActionListener((e) -> {
            value = comp.isSelected();
        });
        return comp;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
