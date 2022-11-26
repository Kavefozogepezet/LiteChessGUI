package me.lcgui.engine;

public class EngineAlreadyInstalledException extends Exception {
    private final String engineName;

    public EngineAlreadyInstalledException(String name) {
        this.engineName = name;
    }

    public String getEngineName() {
        return engineName;
    }
}
