package me.lcgui.gui;

import me.lcgui.game.player.Player;
import me.lcgui.gui.DrawableFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SelectablePlayer {
    String name();
    Class<? extends DrawableFactory<? extends Player>> factoryClass();
    boolean canUseGUI() default false;
}
