package me.lcgui.game.movegen;

import me.lcgui.game.board.Square;

import java.io.Serializable;

/**
 * Memória hatékony tábla reprezentáció, ahol a tábla összes mezőjéhez hozzárendelhető egy logikai érték.
 */
public class BitBoard implements Serializable {
    long bits;

    public BitBoard() {
        bits = 0;
    }

    /**
     * Igazra állítja a mező értékét.
     * @param sq A mező.
     */
    public void set(Square sq) {
        long mask = 1L;
        mask = mask << sq.linearIdx();
        set(mask);
    }

    /**
     * Igazra állítja a mezőket, amelyek a másik táblában is igazak voltak.
     * @param other A másik tábla.
     */
    public void set(BitBoard other) {
        set(other.bits);
    }

    /**
     * Igazra állítja a long-ban beállított biteket.
     * @param mask A logn 64 bit-je a tábla egy egy mezejét jelöli a {@link Square#linearIdx()} visszatérési értéke alapján.
     */
    public void set(long mask) {
        bits |= mask;
    }

    /**
     * Hamisra állítja a megadott mező értékét.
     * @param sq A mező.
     */
    public void clear(Square sq) {
        long mask = 1L;
        mask = mask << sq.linearIdx();
        clear(mask);
    }

    /**
     * Hamisra állítja a mezőket, amelyek a másik táblában igazak voltak.
     * @param other A másik tábla.
     */
    public void clear(BitBoard other) {
        clear(other.bits);
    }

    /**
     * Hamisra állítja a long-ban beállított biteket.
     * @param mask A logn 64 bit-je a tábla egy egy mezejét jelöli a {@link Square#linearIdx()} visszatérési értéke alapján.
     */
    public void clear(long mask) {
        bits &= ~mask;
    }

    /**
     * @param sq A mező, aminek az értékét lekérjük.
     * @return A mezőhöz társított logikai érték.
     */
    public boolean get(Square sq) {
        long mask = 1L;
        mask = mask << sq.linearIdx();
        return get(mask);
    }

    /**
     * Megadja, hogy a long-ban beállított bitekhez tartozó mezőkhöz a táblában igaz érték társul-e.
     * Ha csak egyhez is hamis társul, a visszatérési érték hamis lesz.
     * @param mask A logn 64 bit-je a tábla egy egy mezejét jelöli a {@link Square#linearIdx()} visszatérési értéke alapján.
     * @return igaz, ha a mezők mindegyikéhez igaz társult.
     */
    public boolean get(long mask) {
        return (bits & mask) == mask;
    }

    /**
     * @return Egy logn érték, melynek 64 bit-je a tábla egy egy mezejét jelöli a {@link Square#linearIdx()} visszatérési értéke alapján.
     * Az egyes bitek értékei a hozzájuk tartozó mező alapján (mező értéke -> bit értéke):
     * hamis -> 0; igaz -> 1
     */
    public long toLong() {
        return bits;
    }
}
