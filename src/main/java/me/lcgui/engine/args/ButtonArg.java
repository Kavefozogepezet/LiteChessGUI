package me.lcgui.engine.args;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class ButtonArg
        extends AbstractArg<Void>
        implements Serializable
{
    private boolean sendAtStart = false;

    public ButtonArg(String name) {
        super(name);
    }

    @Override
    public Void getValue() {
        return null;
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public Component getOptionDisplay() {
        var btn = new JCheckBox();
        btn.setSelected(sendAtStart);
        btn.addActionListener((l) -> {
            sendAtStart = btn.isSelected();
        });
        return btn;
    }

    @Override
    public String toString() {
        return "";
    }
}
