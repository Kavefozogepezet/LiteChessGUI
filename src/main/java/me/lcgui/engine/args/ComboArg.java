package me.lcgui.engine.args;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class ComboArg
        extends AbstractArg<String>
        implements Serializable
{
    public final ArrayList<String> options;
    private String value;

    public ComboArg(String name, String value, ArrayList<String> options) {
        super(name);
        this.options = options;

        if (options.contains(value))
            this.value = value;
        else
            this.value = options.get(0);
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
        var comb = new JComboBox<String>((String[]) options.toArray());
        comb.setSelectedItem(value);
        comb.addActionListener((l) -> {
            value = (String) comb.getSelectedItem();
        });
        return comb;
    }

    @Override
    public String toString() {
        return value;
    }

    private void setValue(String value) {
    }
}
