package me.lcgui.engine;

import me.lcgui.engine.args.AbstractArg;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class EngineConfig implements Serializable {
    public final String protocol;
    public final File file;
    public final HashMap<String, AbstractArg<?>> options = new HashMap<>();

    public EngineConfig(String protocol, File file) {
        this.protocol = protocol;
        this.file = file;
    }

    public AbstractArg<?> getOption(String name) {
        return options.get(name);
    }
}
