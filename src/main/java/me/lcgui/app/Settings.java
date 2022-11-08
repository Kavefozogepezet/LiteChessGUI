package me.lcgui.app;

import me.lcgui.engine.args.Args;
import me.lcgui.gui.BoardStyle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Settings implements Serializable {
    private final HashMap<String, Object> settings = new HashMap<>();

    public static Map.Entry<String, ? extends Serializable>
    setting(String name, Serializable value) {
        return Map.entry(name, value);
    }

    @SafeVarargs
    public static Settings withDefaults(Map.Entry<String, ? extends Serializable> ... entries) {
        Settings s = new Settings();
        for(var entry : entries)
            s.set(entry.getKey(), entry.getValue());

        return s;
    }

    public <Type extends Serializable> void set(String name, Type value) {
        settings.put(name, value);
    }

    public Object get(String name) {
        return settings.get(name);
    }

    @SuppressWarnings("unchecked")
    public <Type> Type get(String name, Type defaultValue) {
        try {
            var clazz = (Class<? extends Type>) defaultValue.getClass();
            Type value = clazz.cast(get(name));
            return value == null ? defaultValue : value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
}
