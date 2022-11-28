package me.lcgui.engine;

import me.lcgui.app.LiteChessGUI;
import me.lcgui.misc.Event;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Az engine-ek telepítését és futtatását kezelő osztály.
 */
public class EngineManager implements Serializable {
    /**
     * Az engine kommunikáció naplózásának bool értékű beállítása.
     */
    public static final String ENGINE_LOG = "engine_log";

    private static final File logFile = new File("enginecom.log");

    private final HashMap<String, EngineConfig> installedEngines = new HashMap<>();
    private transient HashMap<String, EngineWrapper> runningEngines = new HashMap<>();

    /**
     * @return Az összes telepített engine neve.
     */
    public Collection<String> getInstalledEngines() {
        return installedEngines.keySet();
    }

    /**
     * Telepíti a megadott engine-t.
     * @param path Az engine futtatható állományának elérési útja.
     * @param protocol A protokoll neve, amivel az osztály megpróbál kommunikálni az engine-el.
     * @return A telepített engine neve.
     * @throws EngineVerificationFailure Az engine nem indult el, vagy nem az elvárt módon kommunikált.
     * @throws EngineAlreadyInstalledException Az engine már installálva volt.
     */
    public String installEngine(File path, String protocol) throws
            EngineVerificationFailure,
            EngineAlreadyInstalledException
    {
        EngineConfig config = new EngineConfig(protocol, path);
        Engine engine = makeEngine(config);
        engine.setInitStart(true);
        startEngineThread(engine);

        String name = engine.getEngineName();
        if(installedEngines.containsKey(name))
            throw new EngineAlreadyInstalledException(name);

        installedEngines.put(name, config);
        engine.quit();
        return name;
    }

    /**
     * Törni az enginet a nyilvántartásából.
     * @param name Az engine neve.
     */
    public void uninstallEngine(String name) {
        installedEngines.remove(name);
    }

    /**
     * Visszaadja a kért engine-t. Ha az engine még nem futott volna, elindítja.
     * @param name A kért engine neve.
     * @return A kért engine
     * @throws EngineVerificationFailure Az engine nem indult el, vagy nem meglelelően kommunikált.
     */
    public Engine getInstance(String name) throws EngineVerificationFailure {
        EngineWrapper wrapper = runningEngines.get(name);
        Engine engine;

        if(wrapper == null)
            engine = startEngine(name);
        else
            engine = wrapper.getEngine();

        return engine;
    }

    /**
     * @return Az éppen futó engine-ek nevei.
     */
    public Set<String> getRunningEngines() {
        return runningEngines.keySet();
    }

    /**
     * Engine név alapján megadja a hozzá tartozó konfigurációt, ha telepítve van az engine.
     * @param name Az engine neve.
     * @return Az engine konfigurációja. Ha az engine nincs telepítve, null.
     */
    public EngineConfig getConfig(String name) {
        return installedEngines.get(name);
    }

    private void releaseInstance(Engine engine) {
        EngineWrapper wrapper = runningEngines.get(engine.getEngineName());
        wrapper.releaseEngine();
        if(wrapper.shouldStop()) {
            runningEngines.remove(engine.getEngineName());
            wrapper.stop();
            log("GUI", "USER", engine.getEngineName() + " stopped.");
        }
    }

    private Engine startEngine(String name) throws EngineVerificationFailure {
        EngineConfig config = installedEngines.get(name);
        if(config == null)
            throw new IllegalArgumentException("There are no engines installed with this name: " + name);

        log("GUI", "USER", name + " is starting ...");

        Engine engine = makeEngine(config);
        engine.getComEvent().addListener(new EngineComLogger(engine));
        engine.getReleasedEvent().addListener(this::releaseInstance);
        startEngineThread(engine);

        log("GUI", "USER", name + " is running.");

        EngineWrapper wrapper = new EngineWrapper(engine);
        runningEngines.put(name, wrapper);
        return wrapper.getEngine();
    }

    private void startEngineThread(Engine engine) throws EngineVerificationFailure {
        Thread engineThread = new Thread(engine);
        engineThread.setDaemon(true);
        engineThread.start();
        engine.verify();
    }

    private Engine makeEngine(EngineConfig config) throws EngineVerificationFailure {
        var clazz = LiteChessGUI.protocols.get(config.protocol);
        if(clazz == null)
            throw new EngineVerificationFailure("Protocol not implemented: " + config.protocol);

        try {
            return clazz.getDeclaredConstructor(EngineConfig.class).newInstance(config);
        } catch (Exception e) {
            throw new EngineVerificationFailure(e);
        }
    }

    @Serial
    protected Object readResolve() {
        runningEngines = new HashMap<>();
        return this;
    }

    private void log(String source, String reciever, String msg) {
        if(!LiteChessGUI.settings.get(ENGINE_LOG, false))
            return;

        try (
                var fileOut = new FileWriter("engine.log", true)
        ){
            String line = "[" + source + "->" + reciever + "]: " + msg;
            fileOut.write(line);
            fileOut.write('\n');
        } catch (IOException ignored) {}
    }

    private class EngineWrapper {
        private int users = 0;
        private Engine engine;

        EngineWrapper(Engine engine) {
            this.engine = engine;
        }
        public Engine getEngine() {
            users++;
            return engine;
        }
        public void releaseEngine() {
            users--;
        }
        public boolean shouldStop() {
            return users <= 0;
        }
        public void stop() {
            engine.quit();
        }
    }

    private class EngineComLogger implements Event.Listener<ComData> {
        Engine engine;

        public EngineComLogger(Engine engine) {
            this.engine = engine;
        }

        @Override
        public void invoked(ComData data) {
            String source = data.isInput ? "GUI" : engine.getEngineName();
            String reciever = data.isInput ? engine.getEngineName() : "GUI";
            log(source, reciever, data.line);
        }
    }
}
