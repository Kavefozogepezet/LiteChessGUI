package me.lcgui.game;

public class IncorrectNotationException extends Exception {
    public IncorrectNotationException() {}

    public IncorrectNotationException(String msg) {
        super(msg);
    }

    public IncorrectNotationException(Throwable cause) {
        super(cause);
    }
}
