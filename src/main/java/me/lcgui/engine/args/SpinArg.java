package me.lcgui.engine.args;

import me.lcgui.misc.MathExt;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class SpinArg
        extends AbstractArg<Integer>
        implements Serializable
{
    public final int min, max;
    private int value;

    public SpinArg(String name, int min, int max, int value) {
        super(name);
        this.min = min;
        this.max = max;
        setValue(value);
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public Component getOptionDisplay() {
        var field = new JTextField();
        field.setText(Integer.toString(value));
        field.addActionListener((a) -> {
            boolean reset = false;
            try {
                int parsed = Integer.parseInt(field.getText());
                setValue(parsed);
                reset = parsed != value;
            } catch (Exception e) {
                reset = true;
            }
            if (reset)
                field.setText(Integer.toString(value));
        });
        return field;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public void setValue(int value) {
        this.value = MathExt.clamp(value, min, max);
    }
}
