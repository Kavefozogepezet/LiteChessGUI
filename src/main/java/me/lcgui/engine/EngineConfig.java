package me.lcgui.engine;

import me.lcgui.engine.args.AbstractArg;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Az engine konfiguráció tartalmazza a futtatható állomány elérési útját, és az engine beállításait.
 */
public class EngineConfig implements Serializable {
    /**
     * A protockoll neve, amivel az engine-t installálták.
     */
    public final String protocol;

    /**
     * Az engine elérési útja.
     */
    public final File file;

    /**
     * Beállítások listája. A kulcs a beállítások nevei.
     */
    public final HashMap<String, AbstractArg<?>> options = new HashMap<>();

    public EngineConfig(String protocol, File file) {
        this.protocol = protocol;
        this.file = file;
    }

    /**
     * @param name A beállítás neve.
     * @return A kért beállítás.
     */
    public AbstractArg<?> getOption(String name) {
        return options.get(name);
    }
}
