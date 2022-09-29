package game;

import game.board.Board;
import game.board.Side;
import game.board.Square;
import game.board.State;
import game.event.GameListener;
import game.movegen.Move;
import game.movegen.MoveGen;
import game.setup.GameSetup;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class Game {
    public enum Result {
        WHITE_WINS, BLACK_WINS, DRAW
    }

    public enum Termination {
        NORMAL, FORFEIT, TIME_FORFEIT
    }

    private Clock clock = null;
    private Board board = new Board();
    private State state = new State();
    private final LinkedList<Move> moveList = new LinkedList<Move>();
    private MoveGen movesNow = new MoveGen(this);

    private Result result = null;
    private Termination termination = null;

    private final LinkedList<GameListener> gameListeners = new LinkedList<>();

    public Game(GameSetup setup) {
        setup.set(this);
        movesNow.generate();
    }
    public Game(GameSetup setup, Clock.Format format) {
        this(setup);
        clock = new Clock(format, state.getTurn());
    }

    public void addGameListener(@NotNull GameListener gameListener) {
        gameListeners.add(gameListener);
    }

    public Board getBoard() {
        return board;
    }
    public State getState() {
        return state;
    }
    public LinkedList<Move> getMoveList() {
        return moveList;
    }
    public LinkedList<Move> getMoves(Square sq) {
        return movesNow.getMoves(sq);
    }
    public Clock getClock() {
        return clock;
    }

    public Result getResult() {
        return result;
    }
    public Termination getTermination() {
        return termination;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean hasEnded() {
        return result != null;
    }
    public boolean usesTimeControl() {
        return clock != null;
    }

    public void play(Move move) {
        if(hasEnded())
            return;

        board.play(move);
        state.movePlayed(move);

        if(clock != null)
            clock.movePlayed();

        moveList.add(move);
        movesNow.generate();
        if(movesNow.isEmpty()) {
            termination = Termination.NORMAL;
            if(movesNow.isCheck())
                result = state.getTurn() == Side.Black
                        ? Result.WHITE_WINS
                        : Result.BLACK_WINS;
        }
    }

    public void unplay() {
        if(moveList.isEmpty() || hasEnded())
            return;

        Move move = moveList.pollLast();
        board.unplay(move);
        state.moveUnplayed();

        if(clock != null)
            clock.movePlayed();

        movesNow.generate();
    }
}
