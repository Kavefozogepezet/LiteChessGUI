package me.lcgui.engine.args;

public interface ArgGUIProvider {
    void createGUIObject(AbstractArg<?> arg);
    void createGUIObject(Args.Check arg);
    void createGUIObject(Args.Button arg);
    void createGUIObject(Args.Str arg);
    void createGUIObject(Args.Combo arg);
    void createGUIObject(Args.Spin arg);
}
