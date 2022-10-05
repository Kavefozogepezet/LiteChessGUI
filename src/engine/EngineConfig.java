package engine;

import extensions.MathExt;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EngineConfig implements Serializable {
    public enum Protocol {
        UCI, WINBOARD
    }
    
    public final Protocol protocol;
    public final File file;
    public final HashMap<String, AbstractOption> options = new HashMap<>();

    public EngineConfig(Protocol protocol, File file) {
        this.protocol = protocol;
        this.file = file;
    }

    public AbstractOption getOption(String name) {
        return options.get(name);
    }

    public static abstract class AbstractOption implements Serializable {
        public final String name;

        public AbstractOption(String name) {
            this.name = name;
        }

        public abstract String getValue();

        public boolean hasValue() {
            return true;
        }

        public abstract Component getOptionDisplay();
    }

    public static class CheckOption
            extends EngineConfig.AbstractOption
            implements Serializable
    {
        public boolean value;
        public CheckOption(String name, boolean value) {
            super(name);
            this.value = value;
        }
        @Override
        public String getValue() {
            return value ? "true" : "false";
        }

        @Override
        public Component getOptionDisplay() {
            var comp = new JCheckBox();
            comp.setSelected(value);
            comp.addActionListener((e) -> { value = comp.isSelected(); });
            return comp;
        }
    }
    public static class SpinOption
            extends EngineConfig.AbstractOption
            implements Serializable
    {
        public final int min, max;
        private int value;

        public SpinOption(String name, int min, int max, int value) {
            super(name);
            this.min = min;
            this.max = max;
            setValue(value);
        }
        @Override
        public String getValue() {
            return Integer.toString(value);
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
                } catch(Exception e) {
                    reset = true;
                }
                if(reset)
                    field.setText(Integer.toString(value));
            });
            return field;
        }

        public void setValue(int value) {
            this.value = MathExt.clamp(value, min, max);
        }
    }
    public static class ComboOption
            extends EngineConfig.AbstractOption
            implements Serializable
    {
        public final ArrayList<String> options;
        private String value;

        public ComboOption(String name, String value, ArrayList<String> options) {
            super(name);
            this.options = options;
            this.value = options.get(0);
            setValue(value);
        }
        @Override
        public String getValue() {
            return null;
        }

        @Override
        public Component getOptionDisplay() {
            var comb = new JComboBox<String>((String[])options.toArray());
            comb.setSelectedItem(value);
            comb.addActionListener((l) -> {
                value = (String) comb.getSelectedItem();
            });
            return comb;
        }

        public void setValue(String value) {
            if(options.contains(value))
                this.value = value;
        }
    }
    public static class StringOption
            extends EngineConfig.AbstractOption
            implements Serializable
    {
        public String value;
        public StringOption(String name, String value) {
            super(name);
            this.value = value;
        }
        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Component getOptionDisplay() {
            var field = new JTextField();
            field.setText(value);
            field.addActionListener((a) -> { field.setText(value); });
            return field;
        }
    }
    public static class ButtonOption
            extends EngineConfig.AbstractOption
            implements Serializable
    {
        private boolean sendAtStart = false;

        public ButtonOption(String name) {
            super(name);
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public boolean hasValue() {
            return false;
        }

        @Override
        public Component getOptionDisplay() {
            var btn = new JCheckBox("send at start:");
            btn.setSelected(sendAtStart);
            btn.addActionListener((l) -> { sendAtStart = btn.isSelected(); });
            return btn;
        }
    }
}
