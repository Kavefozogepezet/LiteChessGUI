package game;

import game.board.Board;
import game.board.Side;
import game.board.Square;
import game.board.State;
import game.event.GameListener;
import game.movegen.Move;
import game.movegen.MoveGen;
import game.setup.Fen;
import game.setup.GameSetup;
import game.setup.StartPos;
import player.Player;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

public class Game {
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
    private final LinkedList<Move> moveList = new LinkedList<Move>();
    private final MoveGen movesNow = new MoveGen(this);

    private Result result = null;
    private Termination termination = null;
    private boolean started = false;

    private String startFen;
    private boolean defaultStart = false;

    private final LinkedList<GameListener> gameListeners = new LinkedList<>();

    public Game(Player white, Player black, GameSetup setup) {
        setup.set(this);
        players[Side.White.ordinal()] = white;
        players[Side.Black.ordinal()] = black;
        white.bind(this);
        black.bind(this);
        movesNow.generate();
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
        if(fen.equals(Fen.STARTPOS_FEN))
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

        board.play(move);
        state.movePlayed(move);

        if(clock != null)
            clock.movePlayed();

        moveList.add(move);
        movesNow.generate();

        invokeMovePlayed();

        if(movesNow.isEmpty()) {
            termination = Termination.NORMAL;
            if(movesNow.isCheck())
                result = state.getTurn() == Side.Black
                        ? Result.WHITE_WINS
                        : Result.BLACK_WINS;
            invokeGameEnd();
        } else {
            players[state.getTurn().ordinal()].myTurn();
        }
    }

    public void resign() {
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
    private void invokeMovePlayed() {
        for(var listener : gameListeners)
            listener.movePlayed(moveList.getLast());
    }
}
