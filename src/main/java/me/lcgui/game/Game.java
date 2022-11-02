package me.lcgui.game;

import me.lcgui.game.board.Board;
import me.lcgui.game.board.PieceType;
import me.lcgui.game.board.Side;
import me.lcgui.game.board.State;
import me.lcgui.game.movegen.Move;
import me.lcgui.game.movegen.MoveGen;
import me.lcgui.game.movegen.SANBuilder;
import me.lcgui.game.setup.FEN;
import me.lcgui.game.setup.GameSetup;
import me.lcgui.game.setup.StartPos;
import me.lcgui.misc.Event;
import me.lcgui.player.Player;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

public class Game implements Serializable {
    public enum Result {
        WHITE_WINS(Side.White), BLACK_WINS(Side.Black), DRAW(null);

        public final Side winner;

        Result(Side winner) {
            this.winner = winner;
        }

        public static Result won(Side side) {
            return side == Side.White ? WHITE_WINS : BLACK_WINS;
        }
        public static Result lost(Side side) {
            return side == Side.Black ? WHITE_WINS : BLACK_WINS;
        }
    }

    public enum Termination {
        NORMAL, FORFEIT, TIME_FORFEIT, ABANDONED
    }

    public static class ResultData {
        public final Result result;
        public final Termination termination;

        public ResultData(Result result, Termination termination) {
            this.result = result;
            this.termination = termination;
        }
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

    private Clock clock = null;

    private final Board board = new Board();
    private State state = new State();
    private final HashMap<String, Integer> prevPos = new HashMap<>();
    private final LinkedList<MoveData> moveList = new LinkedList<>();
    private final MoveGen possibleMoves = new MoveGen();

    private ResultData result = null;

    private FEN startFen;
    private int startPly;
    private boolean defaultStart = false;

    private transient boolean started = false;
    private transient Player[] players = new Player[2];

    public transient Event<MoveData> moveEvent = new Event<>();
    public transient Event<ResultData> endEvent = new Event<>();
    public transient Event<Clock> tickEvent = new Event<>();

    public Game(GameSetup setup) {
        setup.set(this);
        possibleMoves.generate(board, state);
    }

    public Game(GameSetup setup, Clock.Format format) {
        this(setup);
        this.clock = new Clock(format, state.getTurn());
        clock.tickEvent.addListener((side) -> tickEvent.invoke(this.clock));
        clock.outOfTimeEvent.addListener((side) -> endGame(Result.lost(side), Termination.TIME_FORFEIT));
        recordPos();
    }

    public Game(Clock.Format format) {
        this(new StartPos(), format);
    }

    public Game() {
        this(new StartPos());
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
        player.setGame(this, side);
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
    public synchronized Board getBoard() {
        return board;
    }
    public synchronized LinkedList<MoveData> getMoveList() {
        return moveList;
    }
    public synchronized MoveData getMoveData(int ply) {
        return moveList.get(ply - startPly);
    }
    public synchronized MoveGen getPossibleMoves() {
        return possibleMoves;
    }
    public synchronized Clock getClock() {
        return clock;
    }

    public Result getResult() {
        return result.result;
    }
    public Termination getTermination() {
        return result.termination;
    }

    public synchronized boolean isValidMove(Move move) {
        for(Move validMove : possibleMoves.from(move.from))
            if(validMove.equals(move))
                return true;
        return false;
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

    public synchronized boolean canClaimDraw() {
        if(hasEnded())
            return false;
        Integer i = prevPos.get(getPosStr());
        boolean threefold = i != null && Integer.compare(i, 3) >= 0;
        return state.get50move() >= 50 || threefold;
    }

    public synchronized void play(Move move) {
        if(hasEnded())
            return;

        SANBuilder san = new SANBuilder(move, possibleMoves);
        board.play(move);
        state.movePlayed(move);
        recordPos();

        if(usesTimeControl())
            clock.movePlayed();

        possibleMoves.generate(board, state);

        if(possibleMoves.isCheck()) {
            if(possibleMoves.isEmpty())
                san.mate();
            else
                san.check();
        }

        MoveData moveData = new MoveData(move, san.toString());
        moveList.add(moveData);
        moveEvent.invoke(moveData);

        if(insufficientMaterial()) {
            draw();
            return;
        }

        if(possibleMoves.isEmpty()) {
            Result res = possibleMoves.isCheck()
                    ? Result.lost(state.getTurn())
                    : Result.DRAW;
            endGame(res, Termination.NORMAL);
        } else {
            players[state.getTurn().ordinal()].myTurn();
        }

        if (canClaimDraw())
            getPlayer(state.getTurn()).handleDrawClaim();
    }

    public synchronized void resign() {
        if(!hasEnded())
            endGame(Result.lost(state.getTurn()), Termination.FORFEIT);
    }

    public synchronized void draw() {
        if(!hasEnded())
            endGame(Result.DRAW, Termination.NORMAL);
    }

    public synchronized void endGame(Result result, Termination termination) {
        if(hasEnded())
            return;
        players[state.getTurn().ordinal()].cancelTurn();
        this.result = new ResultData(result, termination);
        if(usesTimeControl())
            clock.stop();

        players[0].gameEnd();
        players[1].gameEnd();

        endEvent.invoke(this.result);
    }

    private boolean insufficientMaterial() {
        boolean noPawnOrMajor =
                board.getMaterial(PieceType.Queen) == 0
                && board.getMaterial(PieceType.Rook) == 0
                && board.getMaterial(PieceType.Pawn) == 0;

        if(!noPawnOrMajor)
            return false;

        int
                k = board.getMaterial(PieceType.Knight),
                b = board.getMaterial(PieceType.Bishop);

        if(k + b < 2) // there is only one piece left beside the kings
            return true;

        int
                wb = board.getMaterial(Side.White, PieceType.Bishop),
                bb = board.getMaterial(Side.Black, PieceType.Bishop);

        if(b == 2 && wb == 1 && bb == 1) // only two bishops left
            return board.getLightBishops() == 1 && board.getDarkBishops() == 1; // are the bishops of the same coloured square

        return false;
    }

    private String getPosStr() {
        return board.toString() + state.getCastleRights();
    }

    private void recordPos() {
        String posStr = getPosStr();
        if(!prevPos.containsKey(posStr))
            prevPos.put(posStr, 1);
        else {
            Integer i = prevPos.get(posStr);
            prevPos.put(posStr, ++i);
        }
    }

    @Serial
    protected Object readResolve() {
        moveEvent = new Event<>();
        endEvent = new Event<>();
        tickEvent = new Event<>();

        players = new Player[2];
        return this;
    }
}
