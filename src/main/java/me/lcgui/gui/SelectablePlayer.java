package me.lcgui.gui;

import me.lcgui.game.player.Player;
import me.lcgui.gui.DrawableFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Olyan {@link Player} osztányok annotálandók vele, amelyek választhatóak a grafikus felületen.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SelectablePlayer {
    /**
     * @return A játékostípus neve.
     */
    String name();

    /**
     * @return A factory osztály, amely létrehozza a játékost.
     */
    Class<? extends DrawableFactory<? extends Player>> factoryClass();

    /**
     * @return A játékos típus amit reprezentál tudja használni a grafikus felületet.
     */
    boolean canUseGUI() default false;
}
