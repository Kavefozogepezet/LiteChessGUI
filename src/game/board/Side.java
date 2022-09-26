package game.board;

public enum Side {
    White, Black, Count;

    public Side other() {
        return this == White ? Black : White;
    }
}
