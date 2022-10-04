package engine;

import jdk.jshell.spi.ExecutionControl;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public class EngineManager {
    private final HashMap<String, EngineConfig> installedEngines = new HashMap<>();
    private final HashMap<String, EngineWrapper> runningEngines = new HashMap<>();


    public Collection<String> getInstalledEngines() {
        return installedEngines.keySet();
    }

    public String installEngine(File path, EngineConfig.Protocol protocol) throws
            ExecutionControl.NotImplementedException,
            EngineVerificationFailure
    {
        EngineConfig config = new EngineConfig(protocol, path);
        Engine engine = makeEngine(config);
        engine.setInitStart(true);
        startEngineThread(engine);

        String name = engine.getEngineName();
        if(installedEngines.containsKey(name))
            throw new IllegalArgumentException("This engine is already installed.");

        installedEngines.put(name, config);
        engine.quit();
        return name;
    }

    public Engine getInstance(String name) throws
            ExecutionControl.NotImplementedException,
            EngineVerificationFailure
    {
        EngineWrapper wrapper = runningEngines.get(name);
        Engine engine;

        if(wrapper == null)
            engine = startEngine(name);
        else
            engine = wrapper.getEngine();

        return engine;
    }

    public EngineConfig getConfig(String name) {
        return installedEngines.get(name);
    }

    public void releaseInstance(Engine engine) {
        EngineWrapper wrapper = runningEngines.get(engine.getEngineName());
        wrapper.releaseEngine();
        if(wrapper.shouldStop()) {
            runningEngines.remove(engine.getEngineName());
            wrapper.stop();
        }
    }

    private Engine startEngine(String name) throws
            ExecutionControl.NotImplementedException,
            EngineVerificationFailure
    {
        EngineConfig config = installedEngines.get(name);
        if(config == null)
            throw new IllegalArgumentException("There are no engines installed with this name: " + name);

        Engine engine = makeEngine(config);
        startEngineThread(engine);
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

    private Engine makeEngine(EngineConfig config) throws ExecutionControl.NotImplementedException {
        Engine engine = null;
        switch (config.protocol) {
            case UCI -> engine = new UCIEngine(config);
            case WINBOARD -> { throw new ExecutionControl.NotImplementedException("Winboard protocol is not implemented yet."); }
        }
        return engine;
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
            return users < 0;
        }
        public void stop() {
            engine.quit();
        }
    }
}
