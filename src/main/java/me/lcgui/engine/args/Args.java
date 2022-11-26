package me.lcgui.engine.args;

import me.lcgui.misc.MathExt;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Args {
    public static class Check
            extends AbstractArg<Boolean>
            implements Serializable {

        public Check(String name, boolean value) {
            super(name, value);
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public void accept(ArgGUIProvider guiProvider) {
            guiProvider.createGUIObject(this);
        }

        @Override
        public String getType() {
            return "Check";
        }
    }

    public static class Button
            extends Check
            implements Serializable {
        public Button(String name) {
            super(name, false);
        }

        @Override
        public boolean hasValue() {
            return false;
        }

        @Override
        public void accept(ArgGUIProvider guiProvider) {
            guiProvider.createGUIObject(this);
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public String getType() {
            return "Button";
        }
    }

    public static class Str
            extends AbstractArg<String>
            implements Serializable
    {
        public Str(String name, String value) {
            super(name, value);
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public void accept(ArgGUIProvider guiProvider) {
            guiProvider.createGUIObject(this);
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public String getType() {
            return "String";
        }
    }

    public static class Combo
            extends Str
            implements Serializable {
        private final ArrayList<String> options;

        public Combo(String name, String value, Collection<String> options) {
            super(name, value);
            this.options = new ArrayList<>(options);
        }

        @Override
        public void setValue(String value) {
            if(options.contains(value))
                super.setValue(value);
        }

        @Override
        public void accept(ArgGUIProvider guiProvider) {
            guiProvider.createGUIObject(this);
        }

        public ArrayList<String> getOptions() {
            return options;
        }

        @Override
        public String getType() {
            return "Combo";
        }
    }

    public static class Spin
            extends AbstractArg<Integer>
            implements Serializable
    {
        private final int  min, max;

        public Spin(String name, int min, int max, int value) {
            super(name, value);
            this.min = min;
            this.max = max;
            setValue(value);
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public void setValue(Integer value) {
            value = MathExt.clamp(value, min, max);
            super.setValue(value);
        }

        @Override
        public void accept(ArgGUIProvider guiProvider) {
            guiProvider.createGUIObject(this);
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public String getType() {
            return "Spin(" + min + ", " + max + ")";
        }
    }
}
