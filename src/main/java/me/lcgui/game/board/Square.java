package me.lcgui.game.board;

import java.io.Serializable;
import java.util.Objects;

public class Square implements Serializable {
    public static final Square
            a1 = new Square(0, 0), b1 = new Square(1, 0), c1 = new Square(2, 0), d1 = new Square(3, 0), e1 = new Square(4, 0), f1 = new Square(5, 0), g1 = new Square(6, 0), h1 = new Square(7, 0),
            a2 = new Square(0, 1), b2 = new Square(1, 1), c2 = new Square(2, 1), d2 = new Square(3, 1), e2 = new Square(4, 1), f2 = new Square(5, 1), g2 = new Square(6, 1), h2 = new Square(7, 1),
            a3 = new Square(0, 2), b3 = new Square(1, 2), c3 = new Square(2, 2), d3 = new Square(3, 2), e3 = new Square(4, 2), f3 = new Square(5, 2), g3 = new Square(6, 2), h3 = new Square(7, 2),
            a4 = new Square(0, 3), b4 = new Square(1, 3), c4 = new Square(2, 3), d4 = new Square(3, 3), e4 = new Square(4, 3), f4 = new Square(5, 3), g4 = new Square(6, 3), h4 = new Square(7, 3),
            a5 = new Square(0, 4), b5 = new Square(1, 4), c5 = new Square(2, 4), d5 = new Square(3, 4), e5 = new Square(4, 4), f5 = new Square(5, 4), g5 = new Square(6, 4), h5 = new Square(7, 4),
            a6 = new Square(0, 5), b6 = new Square(1, 5), c6 = new Square(2, 5), d6 = new Square(3, 5), e6 = new Square(4, 5), f6 = new Square(5, 5), g6 = new Square(6, 5), h6 = new Square(7, 5),
            a7 = new Square(0, 6), b7 = new Square(1, 6), c7 = new Square(2, 6), d7 = new Square(3, 6), e7 = new Square(4, 6), f7 = new Square(5, 6), g7 = new Square(6, 6), h7 = new Square(7, 6),
            a8 = new Square(0, 7), b8 = new Square(1, 7), c8 = new Square(2, 7), d8 = new Square(3, 7), e8 = new Square(4, 7), f8 = new Square(5, 7), g8 = new Square(6, 7), h8 = new Square(7, 7),

            invalid = new Square(-1, -1);

    public final int file, rank;

    public Square(int file, int rank) {
        this.file = file;
        this.rank = rank;
    }

    public Square(char fileCh, int rank) {
        this(char2file(fileCh), rank - 1);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;

        if(!(obj instanceof Square sq))
            return false;

        return sq.file == file && sq.rank == rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, rank);
    }

    public boolean valid() {
        return
                0 <= file && file < AbstractBoard.BOARD_SIZE &&
                0 <= rank && rank < AbstractBoard.BOARD_SIZE;
    }

    public Square shift(int file, int rank) {
        return new Square(this.file + file, this.rank + rank);
    }

    public static Square between(Square sq1, Square sq2) {
        return new Square(
                (sq1.file + sq2.file) / 2,
                (sq1.rank + sq2.rank) / 2
        );
    }

    public static Square cross(Square sqFile, Square sqRank) {
        return new Square(sqFile.file, sqRank.rank);
    }

    public boolean isLight() {
        return (file + rank) % 2 == 0;
    }

    public int linearIdx() {
        return (rank << 3) + file;
    }

    @Override
    public String toString() {
        if(!valid())
            return "-";
        else
            return "" + file2char(file) + rank2char(rank);
    }

    public static Square parse(String str) {
        if(str.length() != 2)
            return Square.invalid;

        int
                file = char2file(str.charAt(0)),
                rank = char2rank(str.charAt(1));

        return new Square(file, rank);
    }

    public static char file2char(int file) {
        return (char)('a' + file);
    }

    public static char rank2char(int rank) {
        return (char)('1' + rank);
    }

    public static int char2file(char ch) {
        return ch - 'a';
    }

    public static int char2rank(char ch) {
        return ch - '1';
    }

}
