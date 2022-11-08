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
            implements Serializable
    {
        protected boolean value = false;

        public Check(String name, boolean value) {
            super(name);
            this.value = value;
        }

        @Override
        public Boolean getValue() {
            return value;
        }

        @Override
        public void setValue(Boolean value) {
            value = false;
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
            return Boolean.toString(value);
        }
    }

    public static class Button
            extends Check
            implements Serializable
    {
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
    }

    public static class Str
            extends AbstractArg<String>
            implements Serializable
    {
        protected String value;

        public Str(String name, String value) {
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
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public void accept(ArgGUIProvider guiProvider) {
            guiProvider.createGUIObject(this);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class Combo
        extends Str
        implements Serializable
    {
        private final ArrayList<String> options;

        public Combo(String name, String value, Collection<String> options) {
            super(name, value);
            this.options = new ArrayList<>(options);
        }

        @Override
        public void setValue(String value) {
            if(options.contains(value))
                this.value = value;
        }

        @Override
        public void accept(ArgGUIProvider guiProvider) {
            guiProvider.createGUIObject(this);
        }

        public ArrayList<String> getOptions() {
            return options;
        }
    }

    public static class Spin
            extends AbstractArg<Integer>
            implements Serializable
    {
        private final int min, max;
        private int value;

        public Spin(String name, int min, int max, int value) {
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
        public void setValue(Integer value) {
            this.value = MathExt.clamp(value, min, max);
        }

        @Override
        public void accept(ArgGUIProvider guiProvider) {
            guiProvider.createGUIObject(this);
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }
}
