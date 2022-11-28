package me.lcgui.game;

import me.lcgui.game.board.*;
import me.lcgui.game.movegen.Move;
import me.lcgui.game.movegen.MoveGen;
import me.lcgui.game.movegen.SANBuilder;
import me.lcgui.game.setup.FEN;
import me.lcgui.game.setup.GameSetup;
import me.lcgui.game.setup.StartPos;
import me.lcgui.misc.Event;
import me.lcgui.game.player.Player;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Egy partit reprezentáló osztály.
 */
public class Game implements Serializable {
    /**
     * A parti lehetséges kimeneteleit tartalmazó enum.
     */
    public enum Result {
        WHITE_WINS(Side.White), BLACK_WINS(Side.Black), DRAW(null);

        public final Side winner;

        Result(Side winner) {
            this.winner = winner;
        }

        /**
         * @param side Ez a fél nyerte a partit.
         * @return A kimenetel, miszerint az adott fél nyert.
         */
        public static Result won(Side side) {
            return side == Side.White ? WHITE_WINS : BLACK_WINS;
        }

        /**
         * @param side Ez a fél elvesztette a partit.
         * @return A kimenetel, miszerint az adott fél vesztett.
         */
        public static Result lost(Side side) {
            return side == Side.Black ? WHITE_WINS : BLACK_WINS;
        }
    }

    /**
     * A játék végének okát ábrázoló enum.
     */
    public enum Termination {
        NORMAL, FORFEIT, TIME_FORFEIT, ABANDONED
    }

    /**
     * Csomagoló osztály ami leírja egy játszma végét.
     * Tartalmazza a kimenetelt, és a parti végének okát.
     */
    public static class ResultData implements Serializable {
        public final Result result;
        public final Termination termination;

        public ResultData(Result result, Termination termination) {
            this.result = result;
            this.termination = termination;
        }
    }

    /**
     * CSomagoló osztály egy lépés adatainak tárolására.
     */
    public static class MoveData implements Serializable {
        public final int ply;
        public final Move move;
        public final String SAN;
        public String comment;

        MoveData(int ply, Move move, String SAN, String comment) {
            this.ply = ply;
            this.move = move;
            this.SAN = SAN;
            this.comment = comment;
        }

        MoveData(int ply, Move move, String SAN) {
            this(ply, move, SAN, null);
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
    private final MoveGen possibleMoves = new MoveGen(this);

    private ResultData result = null;

    private FEN startFen;
    private int startPly;
    private boolean defaultStart = false;

    private boolean initialized = false;
    private transient boolean started = false;
    private transient Player[] players = new Player[2];

    /**
     * Azt az eseményt jelöli, hogy az egyik játékos lépett a táblán.
     */
    public transient Event<MoveData> moveEvent = new Event<>();

    /**
     * A parti végének eseménye.
     */
    public transient Event<ResultData> endEvent = new Event<>();

    /**
     * Ha a partit időre játsszák, a sakkóra változásának eseménye.
     */
    public transient Event<Clock> tickEvent = new Event<>();

    /**
     * Parti létrehozása egyedi kezdőpotícióval.
     * @param setup A kezdőpotíció.
     * @throws IncorrectNotationException A pozíciót leító jelölés helytelen volt.
     */
    public Game(GameSetup setup) throws IncorrectNotationException {
        setup.set(this);
        possibleMoves.generate();
        recordPos();
    }

    /**
     * Parti létrehozása egyedi kezdőpozícióval, sakkórával.
     * @param setup A kezdőpotíció.
     * @param format A sakkóra formátuma.
     * @throws IncorrectNotationException A pozíciót leító jelölés helytelen volt.
     */
    public Game(GameSetup setup, Clock.Format format) throws IncorrectNotationException {
        this(setup);
        this.clock = new Clock(format, state.getTurn());
        clock.tickEvent.addListener((side) -> tickEvent.invoke(this.clock));
        clock.outOfTimeEvent.addListener((side) -> endGame(Result.lost(side), Termination.TIME_FORFEIT));
    }

    /**
     * Parti létrehozása a kezdőpozícióval, sakkórával.
     * @param format A sakkóra formátuma.
     */
    public Game(Clock.Format format) {
        this();
        this.clock = new Clock(format, state.getTurn());
        clock.tickEvent.addListener((side) -> tickEvent.invoke(this.clock));
        clock.outOfTimeEvent.addListener((side) -> endGame(Result.lost(side), Termination.TIME_FORFEIT));
    }

    /**
     * Parti létrehozása a kezdőpozícióval.
     */
    public Game() {
        StartPos setup = new StartPos();
        setup.set(this);
        possibleMoves.generate();
        recordPos();
    }

    /**
     * Játszma elkezdése.
     * Ez előtt a játékosokat be kell állítani.
     * Utána tilos a játékosokat változtatni.
     */
    public void startGame() {
        if(started || hasEnded())
            return;

        if(players[0] == null || players[1] == null)
            throw new RuntimeException("Not all players set.");

        if(!initialized) {
            startPly = state.getPly();
            startFen = new FEN(this);
            defaultStart = startFen.equals(FEN.STARTPOS_FEN);
            initialized = true;
        }

        players[state.getTurn().ordinal()].myTurn();
        started = true;
    }

    /**
     * Beállítja a parti játékosait, csak a parti megkezdése előtt hívható meg.
     * @param side A fél.
     * @return A megadott féllel játszó játékos.
     */
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

    public ResultData getResultData() {
        return result;
    }

    /**
     * Megadja, goy egy lépés legális e a parti állása szerint.
     * @param move A lépés amit vizsgál.
     * @return igaz, ha legális a lépés.
     */
    public synchronized boolean isValidMove(Move move) {
        for(Move validMove : possibleMoves.from(move.from))
            if(validMove.equals(move))
                return true;
        return false;
    }

    /**
     * @return igaz, ha a parti befejeződött.
     */
    public boolean hasEnded() {
        return result != null;
    }

    /**
     * @return igaz, ha a parti használ sakkórát.
     */
    public boolean usesTimeControl() {
        return clock != null;
    }

    /**
     * @return igaz, ha a parti a kezdőpozícióból indult.
     */
    public boolean isDefaultStart() {
        return defaultStart;
    }

    /**
     * @return igaz, ha a soron lévő játékosnak joga van döntetlent igényelni.
     */
    public synchronized boolean canClaimDraw() {
        if(hasEnded())
            return false;
        Integer i = prevPos.get(getPosStr());
        boolean threefold = i != null && Integer.compare(i, 3) >= 0;
        return state.get50move() >= 50 || threefold;
    }

    /**
     * A {@link Player} példányok hívják meg, elvégzi az adott lépést.
     * @param move A játékos lépése.
     */
    public synchronized void play(Move move) {
        if(hasEnded())
            return;

        SANBuilder san = new SANBuilder(move, possibleMoves);
        board.play(move);
        state.movePlayed(move);
        recordPos();

        if(usesTimeControl())
            clock.movePlayed();

        possibleMoves.generate();

        if(possibleMoves.isCheck()) {
            if(possibleMoves.isEmpty())
                san.mate();
            else
                san.check();
        }

        MoveData moveData = new MoveData(state.getPly() - 1, move, san.toString());
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

    /**
     * A soron lévő játékos feladja a partit.
     */
    public synchronized void resign() {
        if(!hasEnded())
            endGame(Result.lost(state.getTurn()), Termination.FORFEIT);
    }

    /**
     * A soron lévő játékos döntetlent igényel.
     */
    public synchronized void draw() {
        if(!hasEnded())
            endGame(Result.DRAW, Termination.NORMAL);
    }

    /**
     * Véget veta partinak.
     * @param result A parti eredménye.
     * @param termination A parti végének oka.
     */
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


    /**
     * Megvizsgálja hogy egy long algebraic notation-el megadott lépés legális e a jétszmában.
     * Ha igen, visszaadja a lépést reprezentáló {@link Move} objektumot.
     * @param moveStr A lépés long algebraic notation-ben.
     * @return A legális lépés objektuma.
     * @throws IncorrectNotationException A megadott karakterlánc nem volt értelmeznető.
     * @throws IllegalMoveException A megadott lépés nem legális a partiban.
     */
    public Move parseLAMove(String moveStr) throws IncorrectNotationException, IllegalMoveException {
        if (moveStr.length() < 4 || moveStr.length() > 5)
            throw new IncorrectNotationException("Move must be in long algebraic notation.");

        Square
                from = Square.parse(moveStr.substring(0, 2)),
                to = Square.parse(moveStr.substring(2, 4));

        if (!(from.valid() && to.valid()))
            throw new IncorrectNotationException("Move refers to invalid squares");

        if (moveStr.length() == 5)
            if (!"qrbn".contains(moveStr.substring(4, 5)))
                throw new IncorrectNotationException("Promotion piece is invalid");

        for (Move move : possibleMoves.from(from))
            if (move.toString().equals(moveStr))
                return move;

        throw new IllegalMoveException();
    }
}
