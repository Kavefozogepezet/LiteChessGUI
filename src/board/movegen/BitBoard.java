package board.movegen;

import board.types.Square;

public class BitBoard {
    long bits;

    BitBoard() {
        bits = 0;
    }

    public void set(Square sq) {
        long mask = 1L;
        mask = mask << ((sq.rank << 3) + sq.file);
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
        mask = mask << ((sq.rank << 3) + sq.file);
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
        mask = mask << ((sq.rank << 3) + sq.file);
        return get(mask);
    }

    public boolean get(long mask) {
        return (bits & mask) != 0;
    }
}
