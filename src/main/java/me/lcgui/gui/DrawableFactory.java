package me.lcgui.gui;

/**
 * Olyan objektum gártó osztály, amely kirajzolható grafikus felületre,
 * és az objektum létrehozásához a grafikus felületen kapott információt használja fel.
 * @param <Type> A gyártott objektum típusa.
 */
public interface DrawableFactory<Type> extends GUICreator {
    /**
     * Legyártja a kért objektumot a felhasználói felületen kapott információ alapján.
     * @return A gyartott objektum.
     * @throws FactoryException Az objektum legyártása nem colt lehetséges.
     */
    Type instantiate() throws FactoryException;
}
