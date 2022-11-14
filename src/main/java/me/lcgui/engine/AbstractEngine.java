package me.lcgui.engine;

import me.lcgui.engine.args.AbstractArg;
import me.lcgui.game.Game;
import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.Move;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public abstract class AbstractEngine implements Engine {
    public static int VERIFICATION_WINDOW_SIZE = 3000;

    protected final EngineConfig config;
    protected String name;
    protected boolean running = false;
    protected boolean initStart = false;
    protected boolean searching = false;
    private EngineVerificationFailure exception = null;

    protected Game game;

    public final Event<Consumable<Move>> bestEvent = new Event<>();
    public final Event<SearchInfo> infoEvent = new Event<>();
    public final Event<ComData> comEvent = new Event<>();
    public final Event<Engine> releasedEvent = new Event<>();

    private Scanner in;
    private PrintStream out;

    private SearchInfo lastInfo = new SearchInfo();

    public AbstractEngine(EngineConfig config) {
        this.config = config;
        name = config.file.getName(); // temporary name
    }

    @Override
    public synchronized void verify() throws EngineVerificationFailure {
        if(running)
            return;

        try {
            wait(VERIFICATION_WINDOW_SIZE);
            if (!running) {
                if(exception == null)
                    exception = new EngineVerificationFailure("Engine not responded");
                throw exception;
            }
        } catch (InterruptedException ignored) {
            throw new EngineVerificationFailure("Thread interrupted");
        }
    }

    @Override
    public void release() {
        releasedEvent.invoke(this);
    }

    protected abstract void handshake() throws EngineVerificationFailure;

    protected abstract void processLine(String line);

    @Override
    public boolean isSearching() {
        return searching;
    }

    @Override
    public void addMoveListener(Event.Listener<Consumable<Move>> listener) {
        bestEvent.addListener(listener);
    }

    @Override
    public void removeMoveListener(Event.Listener<Consumable<Move>> listener) {
        bestEvent.removeListener(listener);
    }

    @Override
    public Event<SearchInfo> getInfoEvent() {
        return infoEvent;
    }

    @Override
    public Event<ComData> getComEvent() {
        return comEvent;
    }

    @Override
    public Event<Engine> getReleasedEvent() {
        return releasedEvent;
    }

    @Override
    public Game getCurrentGame() {
        return game;
    }

    @Override
    public void setInitStart(boolean initStart) {
        this.initStart = initStart;
    }

    @Override
    public String getEngineName() {
        return name;
    }

    @Override
    public void run() {
        if(!config.file.exists()) {
            setException(new EngineVerificationFailure("Missing engine executable"));
            return;
        }

        ProcessBuilder processBuilder = new ProcessBuilder(config.file.getPath());
        Process engineProcess = null;
        try {
            engineProcess = processBuilder.start();
        } catch (IOException e) {
            setException(new EngineVerificationFailure("Engine did not start"));
            return;
        }

        try (
                var input = engineProcess.getInputStream();
                var output = engineProcess.getOutputStream()
        ) {
            in = new Scanner(input);
            out = new PrintStream(output, true, StandardCharsets.UTF_8);

            try {
                handshake();
                running = true;
            } catch (EngineVerificationFailure e) {
                setException(e);
                return;
            } finally {
                synchronized (this) {
                    this.notifyAll();
                }
            }

            for(var op : config.options.values())
                if(!op.isDefault())
                    setOption(op.getName(), op.toString());

            while(in.hasNextLine()) {
                String line = readFromEngine();
                processLine(line);
            }
        } catch (IOException ignored) {
            exception = new EngineVerificationFailure("");
        } finally {
            engineProcess.destroy();
        }
    }

    protected SearchInfo cloneInfo() {
        return lastInfo.clone();
    }

    protected void newInfo(SearchInfo info) {
        if(info.isDirty()) {
            lastInfo = info;
            infoEvent.invoke(info);
        }
    }

    protected Move convertMove(String moveStr) {
        Move move = null;
        Square from = Square.parse(moveStr.substring(0, 2));
        for (var possible : game.getPossibleMoves().from(from)) {
            if (possible.toString().equals(moveStr)) {
                move = possible;
                break;
            }
        }
        return move;
    }

    protected void setException(EngineVerificationFailure exception) {
        synchronized (exception) {
            this.exception = exception;
        }
    }

    protected synchronized EngineVerificationFailure getException() {
        synchronized (exception) {
            return exception;
        }
    }

    protected void writeToEngine(String line) {
        out.println(line);
        comEvent.invoke(new ComData(true, line));
    }

    protected String readFromEngine() {
        String line = in.hasNextLine()
                ? in.nextLine()
                : "";
        comEvent.invoke(new ComData(false, line));
        return line;
    }
}
