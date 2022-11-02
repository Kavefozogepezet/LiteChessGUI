package me.lcgui.engine.args;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class StringArg
        extends AbstractArg<String>
        implements Serializable
{
    public String value;

    public StringArg(String name, String value) {
        super(name);
        this.value = value;
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Component getOptionDisplay() {
        var field = new JTextField();
        field.setText(value);
        field.addActionListener((a) -> {
            field.setText(value);
        });
        return field;
    }

    @Override
    public String toString() {
        return value;
    }
}
