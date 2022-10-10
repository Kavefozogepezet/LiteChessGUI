package game;

import game.board.Board;
import game.board.Side;
import game.board.State;
import game.event.GameListener;
import game.movegen.Move;
import game.movegen.MoveGen;
import game.movegen.SANBuilder;
import game.setup.FEN;
import game.setup.GameSetup;
import game.setup.StartPos;
import player.Player;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;

public class Game implements Serializable {
    public enum Result {
        WHITE_WINS, BLACK_WINS, DRAW;

        public static Result won(Side side) {
            return side == Side.White ? WHITE_WINS : BLACK_WINS;
        }
        public static Result lost(Side side) {
            return side == Side.Black ? WHITE_WINS : BLACK_WINS;
        }
    }

    public enum Termination {
        NORMAL, FORFEIT, TIME_FORFEIT
    }

    private Clock clock = null;
    private final Board board = new Board();
    private State state = new State();
    private final LinkedList<MoveData> moveList = new LinkedList<>();
    private final MoveGen possibleMoves = new MoveGen();

    private Result result = null;
    private Termination termination = null;

    private FEN startFen;
    private int startPly;
    private boolean defaultStart = false;

    private transient boolean started = false;
    private Player[] players = new Player[2];
    private transient LinkedList<GameListener> gameListeners = new LinkedList<>();

    public Game(GameSetup setup) {
        setup.set(this);
        possibleMoves.generate(board, state);
    }

    public Game(GameSetup setup, Clock.Format format) {
        this(setup);
        clock = new Clock(format, state.getTurn());
        clock.getTimer().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invokeTimeTick();
                if(clock.isTimeout()) {
                    players[state.getTurn().ordinal()].cancelTurn();
                    endGame(Result.lost(state.getTurn()), Termination.TIME_FORFEIT);
                }
            }
        });
    }

    public Game(Clock.Format format) {
        this(new StartPos(), format);
    }

    public Game() {
        this(new StartPos());
    }

    public void addListener(GameListener listener) {
        gameListeners.add(listener);
    }

    public void removeListener(GameListener listener) {
        gameListeners.remove(listener);
    }

    public void startGame() {
        if(started)
            return;

        if(players[0] == null || players[1] == null)
            throw new RuntimeException("Not all players set.");

        startPly = state.getPly();
        startFen = new FEN(this);
        defaultStart = startFen.equals(FEN.STARTPOS_FEN);

        players[state.getTurn().ordinal()].myTurn();
        started = true;
    }

    public Player getPlayer(Side side) {
        return players[side.ordinal()];
    }
    public void setPlayer(Side side, Player player) {
        if(started)
            throw new RuntimeException("Cannot set a player after the game has been started");
        players[side.ordinal()] = player;
        player.setGame(this);
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        if(started)
            throw new RuntimeException("Cannot set game state after the game has been started");
        this.state = state;
    }

    public FEN getStartFen() {
        return startFen;
    }
    public int getStartPly() {
        return startPly;
    }
    public Board getBoard() {
        return board;
    }
    public LinkedList<MoveData> getMoveList() {
        return moveList;
    }
    public MoveData getMoveData(int ply) {
        return moveList.get(ply - startPly);
    }
    public MoveGen getPossibleMoves() {
        return possibleMoves;
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

    public boolean hasEnded() {
        return result != null;
    }

    public boolean usesTimeControl() {
        return clock != null;
    }

    public boolean isDefaultStart() {
        return defaultStart;
    }

    public void play(Move move) {
        if(hasEnded())
            return;

        SANBuilder san = new SANBuilder(move, possibleMoves);
        board.play(move);
        state.movePlayed(move);

        if(usesTimeControl())
            clock.movePlayed();

        possibleMoves.generate(board, state);

        if(possibleMoves.isEmpty()) {
            termination = Termination.NORMAL;
            if(possibleMoves.isCheck()) { // TODO get check from board
                san.mate();
                result = state.getTurn() == Side.Black
                        ? Result.WHITE_WINS
                        : Result.BLACK_WINS;
            }
            invokeMovePlayed(move, san.toString());
            invokeGameEnd();
        } else {
            if(possibleMoves.isCheck())
                san.check();
            invokeMovePlayed(move, san.toString());
            players[state.getTurn().ordinal()].myTurn();
        }
    }

    public void resign() {
        if(hasEnded())
            return;
        players[state.getTurn().ordinal()].cancelTurn();
        endGame(Result.lost(state.getTurn()), Termination.FORFEIT);
    }

    public void draw() {
        if(hasEnded())
            return;
        players[state.getTurn().ordinal()].cancelTurn();
        endGame(Result.DRAW, Termination.NORMAL);
    }

    private void endGame(Result result, Termination termination) {
        this.result = result;
        this.termination = termination;
        invokeGameEnd();
    }

    private void invokeTimeTick() {
        for(var listener : gameListeners)
            listener.timeTick(clock);
    }
    private void invokeGameEnd() {
        for(var listener : gameListeners)
            listener.gameEnded(result, termination);
    }
    private void invokeMovePlayed(Move move, String SAN) {
        moveList.add(new MoveData(move, SAN));
        for(var listener : gameListeners)
            listener.movePlayed(move, SAN);
    }

    @Serial
    protected Object readResolve() {
        gameListeners = new LinkedList<>();
        players = new Player[2];
        return this;
    }

    public static class MoveData implements Serializable {
        public final Move move;
        public final String SAN;
        public String comment;

        MoveData(Move move, String SAN, String comment) {
            this.move = move;
            this.SAN = SAN;
            this.comment = comment;
        }

        MoveData(Move move, String SAN) {
            this(move, SAN, null);
        }

        public boolean hasComment() {
            return comment != null && !comment.equals("");
        }
    }
}
