package engine;

import jdk.jshell.spi.ExecutionControl;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

public class EngineManager implements Serializable {
    private static final File logFile = new File("enginecom.log");

    private final HashMap<String, EngineConfig> installedEngines = new HashMap<>();
    private transient HashMap<String, EngineWrapper> runningEngines = new HashMap<>();
    private boolean logEngineCom = false;


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

    public void releaseInstance(Engine engine) {
        EngineWrapper wrapper = runningEngines.get(engine.getEngineName());
        wrapper.releaseEngine();
        if(wrapper.shouldStop()) {
            runningEngines.remove(engine.getEngineName());
            wrapper.stop();
        }
    }

    public EngineConfig getConfig(String name) {
        return installedEngines.get(name);
    }

    public void setLogging(boolean log) {
        logEngineCom = log;
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

        engine.addListener(new EngineListener() {
            @Override
            public void bestmove(String s) {}

            @Override
            public void info(SearchInfo info) {}

            @Override
            public void anyCom(boolean isInput, String line) {
                String sender = isInput ? "[GUI->" + engine.getEngineName() + "]: " : "[" + engine.getEngineName() + "]: ";
                try (
                        var fileOut = new FileWriter("engine.log", true)
                ){
                    fileOut.write(sender + line);
                    fileOut.write('\n');
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

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

    @Serial
    protected Object readResolve() {
        runningEngines = new HashMap<>();
        return this;
    }
}
