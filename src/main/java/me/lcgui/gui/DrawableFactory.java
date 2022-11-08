package me.lcgui.gui;

public interface DrawableFactory<Type> extends GUICreator {
    Type instantiate() throws Exception;
}
