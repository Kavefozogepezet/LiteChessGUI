package engine;

public interface EngineListener extends java.util.EventListener {
    void bestmove(java.lang.String s);
    void info(SearchInfo info);

    void anyCom(boolean isInput, String line);
}