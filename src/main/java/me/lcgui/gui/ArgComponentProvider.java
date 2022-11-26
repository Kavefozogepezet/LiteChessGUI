package me.lcgui.gui;

import me.lcgui.engine.args.AbstractArg;
import me.lcgui.engine.args.ArgGUIProvider;
import me.lcgui.engine.args.Args;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class ArgComponentProvider implements ArgGUIProvider, GUICreator {
    JComponent component;
    AbstractArg<?> arg;

    public ArgComponentProvider(AbstractArg<?> arg) {
        this.arg = arg;
        createGUI();
    }

    @Override
    public Component createGUI() {
        arg.accept(this);
        return component;
    }

    @Override
    public Component getRootComponent() {
        return component;
    }

    @Override
    public void createGUIObject(AbstractArg<?> arg) {
        System.err.println("ArgComponentProvider cannot provide component for " + arg.getClass());
        component = new JLabel("Uneditable");
    }

    @Override
    public void createGUIObject(Args.Check arg) {
        var check = new JCheckBox();
        check.setSelected(arg.getValue());

        check.addActionListener((e) -> {
            arg.setValue(check.isSelected());
        });

        arg.changedEvent.addListener(b -> {
            if(!b.equals(check.isSelected()))
                check.setSelected(b);
        });

        component = check;
    }

    @Override
    public void createGUIObject(Args.Button arg) {
        createGUIObject((Args.Check) arg);
    }

    @Override
    public void createGUIObject(Args.Str arg) {
        var field = new JTextField();
        field.setText(arg.getValue());

        field.addActionListener((a) -> {
            arg.setValue(field.getText());
        });

        arg.changedEvent.addListener(str -> {
            if(!str.equals(field.getText()))
                field.setText(str);
        });

        component = field;
    }

    @Override
    public void createGUIObject(Args.Combo arg) {
        var combo = new JComboBox<>(arg.getOptions().toArray(new String[0]));
        combo.setSelectedItem(arg.getValue());

        combo.addActionListener((l) -> {
            arg.setValue((String) combo.getSelectedItem());
        });

        arg.changedEvent.addListener(str -> {
            if(!str.equals(combo.getSelectedItem()))
                combo.setSelectedItem(str);
        });

        component = combo;
    }

    @Override
    public void createGUIObject(Args.Spin arg) {
        var field = new JFormattedTextField(NumberFormat.getNumberInstance());
        field.setText(Integer.toString(arg.getValue()));

        field.addActionListener((a) -> {
            boolean reset = false;
            try {
                int parsed = Integer.parseInt(field.getText());
                arg.setValue(parsed);
                reset = parsed != arg.getValue();
            } catch (Exception e) {
                reset = true;
            }
            if (reset)
                field.setText(Integer.toString(arg.getValue()));
        });

        arg.changedEvent.addListener(str -> {
            boolean reset = false;
            try {
                int parsed = Integer.parseInt(field.getText());
                reset = parsed != arg.getValue();
            } catch (Exception e) {
                reset = true;
            }

            if(reset)
                field.setText(Integer.toString(arg.getValue()));
        });

        component = field;
    }
}
