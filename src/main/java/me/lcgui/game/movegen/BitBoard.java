package me.lcgui.game.movegen;

import me.lcgui.game.board.Square;

import java.io.Serializable;

public class BitBoard implements Serializable {
    long bits;

    BitBoard() {
        bits = 0;
    }

    public void set(Square sq) {
        long mask = 1L;
        mask = mask << sq.linearIdx();
        set(mask);
    }

    public void set(BitBoard other) {
        set(other.bits);
    }

    public void set(long mask) {
        bits |= mask;
    }

    public void clear(Square sq) {
        long mask = 1L;
        mask = mask << sq.linearIdx();
        clear(mask);
    }

    public void clear(BitBoard other) {
        clear(other.bits);
    }

    public void clear(long mask) {
        bits &= ~mask;
    }

    public boolean get(Square sq) {
        long mask = 1L;
        mask = mask << sq.linearIdx();
        return get(mask);
    }

    public boolean get(long mask) {
        return (bits & mask) != 0;
    }
}
