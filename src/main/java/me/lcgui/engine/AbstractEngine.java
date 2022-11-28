package me.lcgui.engine;

import me.lcgui.engine.args.AbstractArg;
import me.lcgui.game.Game;
import me.lcgui.game.IllegalMoveException;
import me.lcgui.game.IncorrectNotationException;
import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.Move;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Általános engine funkciókat megvalósító abstract osztály.
 */
public abstract class AbstractEngine implements Engine {
    /**
     * A minimum idő ms-ben, amennyit várni kell az engine process elindítása után.
     * Ha leteltéig az engine nem válaszol, közelező leállítani az engine-t.
     */
    public static int VERIFICATION_WINDOW_SIZE = 3000;

    /**
     * A konfiguráció, ami alapján az engine elindult.
     */
    protected final EngineConfig config;

    /**
     * Az engine neve.
     * Vagy a futtatható állomány neve, vagy amit az engine közölt a kimenetén.
     */
    protected String name;

    protected boolean running = false;
    protected boolean initStart = false;
    protected boolean searching = false;
    private EngineVerificationFailure exception = null;

    /**
     * Az engine által játszott parti.
     */
    protected Game game;

    /**
     * Legjobb lépés megtalálásának eseménye.
     */
    public final Event<Consumable<Move>> bestEvent = new Event<>();

    /**
     * Keresési infó frissítésének eseménye.
     */
    public final Event<SearchInfo> infoEvent = new Event<>();

    /**
     * GUI és engine közötti bármiféle kommunikáció eseménye.
     */
    public final Event<ComData> comEvent = new Event<>();

    /**
     * Az engine elengedésének eseménye.
     */
    public final Event<Engine> releasedEvent = new Event<>();

    private Scanner in;
    private PrintStream out;

    private SearchInfo lastInfo = new SearchInfo();

    public AbstractEngine(EngineConfig config) {
        this.config = config;
        name = config.file.getName(); // temporary name
    }

    /**
     * Vár az engine verifikálására. Ha a mellékszál kivételt dobott, továbbdobja itt exceptiont.
     * @throws EngineVerificationFailure
     */
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

    /**
     * A leszármazottak ebben a metódusban adhatják ki az engine-nek a konfigurációs parancsokat, ha erre szükség van.
     * @throws EngineVerificationFailure Ha a konfiguráció során váratlan válasz érkezik az engine-től, vagy egyáltalán nem válaszol.
     */
    protected abstract void handshake() throws EngineVerificationFailure;

    /**
     * Akkor hívódik meg, ha az engine process egy új sort írt a kimenetére. Így a sor továbbítódik a leszármazottakhoz.
     * Itt kell implementálni a parancsok értelmezését.
     * @param line
     */
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

    /**
     * Elindítja az engine process-t, majd meghívja a {@link AbstractEngine#handshake()} metódust.
     * Ha {@link EngineVerificationFailure} kivétel adódik, elmenti azt az exception mezőbe,
     * így a kivétel a fő szálról is elérhető lesz.
     * Végtelen ciklusban vár az engine kimenetére.
     */
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

    /**
     * Lemásolja az utoljára kapott inó csomagot az engine-től.
     * @return A lemásolt információ csomag.
     */
    protected SearchInfo cloneInfo() {
        return lastInfo.clone();
    }

    /**
     * Új információ érkezésénél hívandó meg. Ha a csomag valóban tartalmaz új infót,
     * meghívódik az {@link AbstractEngine#infoEvent}
     * @param info Az új infó csomag.
     */
    protected void newInfo(SearchInfo info) {
        if(info.isDirty()) {
            lastInfo = info;
            infoEvent.invoke(info);
        }
    }

    protected Move convertMove(String moveStr) {
        /*Move move = null;
        Square from = Square.parse(moveStr.substring(0, 2));
        for (var possible : game.getPossibleMoves().from(from)) {
            if (possible.toString().equals(moveStr)) {
                move = possible;
                break;
            }
        }
        return move;*/
        try {
            return game.parseLAMove(moveStr);
        } catch (IllegalMoveException | IncorrectNotationException e) {
            return null;
        }
    }

    /**
     * Szálbiztosan beállítja az exception mező értékét.
     * Arra használandó, hogy a mellékszálból a főszálba küldjük a kivételt kezelésre.
     * @param exception A dobott kivétel.
     */
    protected void setException(EngineVerificationFailure exception) {
        synchronized (exception) {
            this.exception = exception;
        }
    }

    /**
     * Szálbiztosan kiolvassa a mellékszálban dobott kivételt.
     * @return A dobott kivétel.
     */
    protected synchronized EngineVerificationFailure getException() {
        synchronized (exception) {
            return exception;
        }
    }

    /**
     * Parancsot küld az engine-nek, meghívja a {@link AbstractEngine#comEvent} esemélyt.
     * @param line A szöveges parancs.
     */
    protected void writeToEngine(String line) {
        out.println(line);
        comEvent.invoke(new ComData(true, line));
    }

    /**
     * Beolvas egy sort az engine kimenetéről, meghívja a {@link AbstractEngine#comEvent} eseményt.
     * @return A beolvasott sor.
     */
    protected String readFromEngine() {
        String line = in.hasNextLine()
                ? in.nextLine()
                : "";
        comEvent.invoke(new ComData(false, line));
        return line;
    }
}
