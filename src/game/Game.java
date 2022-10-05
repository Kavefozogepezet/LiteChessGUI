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
    private final Player[] players = new Player[2];
    private final LinkedList<Move> moveList = new LinkedList<>();
    private final MoveGen possibleMoves = new MoveGen();

    private Result result = null;
    private Termination termination = null;
    private boolean started = false;

    private String startFen;
    private boolean defaultStart = false;

    private transient LinkedList<GameListener> gameListeners = new LinkedList<>();

    public Game(Player white, Player black, GameSetup setup) {
        setup.set(this);
        players[Side.White.ordinal()] = white;
        players[Side.Black.ordinal()] = black;
        white.setGame(this);
        black.setGame(this);
        possibleMoves.generate(board, state);
    }

    public Game(Player white, Player black, GameSetup setup, Clock.Format format) {
        this(white, black, setup);
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

    public Game(Player white, Player black, Clock.Format format) {
        this(white, black, new StartPos(), format);
    }

    public Game(Player white, Player black) {
        this(white, black, new StartPos());
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

        players[state.getTurn().ordinal()].myTurn();
        started = true;
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }

    public String getStartFen() {
        return startFen;
    }
    public void setStartFen(String fen) {
        startFen = fen;
        if(fen.equals(FEN.STARTPOS_FEN))
            defaultStart = true;
    }

    public Board getBoard() {
        return board;
    }
    public Player getPlayer(Side side) {
        return players[side.ordinal()];
    }
    public LinkedList<Move> getMoveList() {
        return moveList;
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

        moveList.add(move);
        possibleMoves.generate(board, state);

        if(possibleMoves.isEmpty()) {
            termination = Termination.NORMAL;
            if(possibleMoves.isCheck()) { // TODO get check from board
                san.mate();
                result = state.getTurn() == Side.Black
                        ? Result.WHITE_WINS
                        : Result.BLACK_WINS;
            }
            invokeMovePlayed(san.toString());
            invokeGameEnd();
        } else {
            if(possibleMoves.isCheck())
                san.check();
            invokeMovePlayed(san.toString());
            players[state.getTurn().ordinal()].myTurn();
        }
    }

    public void resign() {
        players[state.getTurn().ordinal()].cancelTurn();
        endGame(Result.lost(state.getTurn()), Termination.FORFEIT);
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
    private void invokeMovePlayed(String SAN) {
        for(var listener : gameListeners)
            listener.movePlayed(moveList.getLast(), SAN);
    }

    @Serial
    protected Object readResolve() {
        gameListeners = new LinkedList<>();
        return this;
    }
}
