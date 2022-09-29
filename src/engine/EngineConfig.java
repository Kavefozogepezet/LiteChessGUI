package engine;

import java.io.File;
import java.util.HashMap;

public class EngineConfig {
    public enum Protocol {
        UCI, WINBOARD
    }
    
    public final Protocol protocol;
    public final File file;
    public final HashMap<String, Object> options = new HashMap<>();

    public EngineConfig(Protocol protocol, File file) {
        this.protocol = protocol;
        this.file = file;
    }

    public void setOption(String name, Object value) {
        options.put(name, value);
    }

    public Object getOption(String name) {
        return options.get(name);
    }
}
