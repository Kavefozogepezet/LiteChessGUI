package me.lcgui.gui;

public interface DrawableFactory<T> extends GUICreator {
    T instantiate() throws Exception;
}
