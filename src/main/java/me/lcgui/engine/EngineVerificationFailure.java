package me.lcgui.engine;

public class EngineVerificationFailure extends Exception {
    public EngineVerificationFailure() {}

    public EngineVerificationFailure(String msg) {
        super(msg);
    }

    public EngineVerificationFailure(Throwable cause) {
        super(cause);
    }
}
