package me.lcgui.engine;

import me.lcgui.engine.args.AbstractArg;
import me.lcgui.engine.args.Args;
import me.lcgui.game.Game;
import me.lcgui.game.movegen.Move;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// TODO
@ProtocolImplementation(name = "XBoard")
public class XBoardEngine implements Engine {
    private final EngineConfig config;
    private final Features myFeatures = new Features();
    private String name;
    private boolean running = false;
    private boolean initStart = false;
    private boolean searching = false;

    private Game game;
    private String pvMove = null;

    public final Event<Consumable<Move>> bestEvent = new Event<>();
    public final Event<SearchInfo> infoEvent = new Event<>();
    public final Event<ComData> comEvent = new Event<>();
    public final Event<Engine> releasedEvent = new Event<>();

    private Scanner in;
    private PrintStream out;

    private SearchInfo lastInfo = new SearchInfo();

    public XBoardEngine(EngineConfig config) {
        this.config = config;
        name = config.file.getName(); // temporary name
    }

    @Override
    public void verify() throws EngineVerificationFailure {
        if(running)
            return;

        try {
            synchronized (this) {
                wait(VERIFICATION_WINDOW_SIZE);
                if(!running)
                    throw new EngineVerificationFailure(
                            "Engine not responded correctly, check that the it is installed with the correct protocol"
                    );
            }
        } catch (InterruptedException ignored) {
            throw new EngineVerificationFailure("Thread interrupted");
        }
    }

    @Override
    public void isReady() {
        if(!myFeatures.ping)
            return;

        try {
            synchronized (this) {
                writeToEngine("ping 1"); // TODO pong N
                this.wait();
            }
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void quit() {
        writeToEngine("quit");
    }

    @Override
    public void release() {
        releasedEvent.invoke(this);
    }

    @Override
    public void startSearch() {
        // TODO
    }

    @Override
    public void stopSearch() {
        // TODO
    }

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
    public void playingThis(Game game) {
        // TODO
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
    public void setOption(String option, String value) {

    }

    @Override
    public String getEngineName() {
        return name;
    }

    @Override
    public void run() {
        ProcessBuilder processBuilder = new ProcessBuilder(config.file.getPath());
        Process engineProcess = null;
        try {
            engineProcess = processBuilder.start();
        } catch (IOException e) {
            return;
        }

        try (
                var input = engineProcess.getInputStream();
                var output = engineProcess.getOutputStream()
        ) {
            in = new Scanner(input);
            out = new PrintStream(output, true, StandardCharsets.UTF_8);

            writeToEngine("xboard");
            writeToEngine("protover 2");

            String inputLine;
            do {
                inputLine = readFromEngine();
                String[] line = inputLine.split(" ");
                if (line[0].equals("feature")) {
                    for(int i = 1; i < line.length; i++) {
                        readFeature(line[i]);
                    }
                }
            } while (!myFeatures.done);

            writeToEngine("ping 2");
            do {
                inputLine = readFromEngine();
            } while (!inputLine.equals("pong 2"));

            running = true;
            synchronized (this) {
                this.notifyAll();
            }

            writeToEngine("post");
            writeToEngine("new");

            // TODO obviously
        } catch (Exception ignored) {}
        finally {
            engineProcess.destroy();
        }
    }

    private void readFeature(String featureStr) {
        String[] feature = featureStr.split("=");
        String
                fName = feature[0],
                fValue = feature[1];

        if(fValue.charAt(0) == '"')
            fValue = fValue.substring(1, fValue.length() - 1);

        switch (fName) {
            case "myname" -> {
                name = fValue;
                writeToEngine("accepted " + fName);
            }
            case "done" -> {
                if("1".equals(fValue))
                    myFeatures.done = true;
                writeToEngine("accepted " + fName);
            }
            case "ping" -> {
                if("1".equals(fValue))
                    myFeatures.ping = true;
                writeToEngine("accepted " + fName);
            }
            case "reuse" -> {
                if("1".equals(fValue)) {
                    myFeatures.reuse = true;
                    writeToEngine("accepted " + fName);
                } else {
                    writeToEngine("rejected " + fName);
                }
            }
            case "option" -> {
                if(initStart) {
                    var option = readOption(fValue);
                    config.options.put(option.getName(), option);
                }
            }
            default -> writeToEngine("rejected " + fName);
        }
    }

    private AbstractArg<?> readOption(String optionStr) {
        String[] option = optionStr.split(" ", 3);
        AbstractArg<?> arg = null;

        switch (option[1]) {
            case "-spin" -> {
                String[] params = option[2].split(" ");
                int
                        value = Integer.parseInt(params[2]),
                        min = Integer.parseInt(params[3]),
                        max = Integer.parseInt(params[4]);
                arg = new Args.Spin(option[0], min, max, value);
            }
            case "-combo" -> {
                String[] values = option[2].split(" /// ");
                String value = values[0];
                for(int i = 0; i < values.length; i++) {
                    if(values[i].charAt(0) == '*') {
                        values[i] = values[i].substring(1);
                        value = values[i];
                    }
                }
                arg = new Args.Combo(option[0], value, List.of(values));
            }
            case "-check" -> {
                boolean value = Integer.parseInt(option[3]) == 1;
                arg = new Args.Check(option[0], value);
            }
            case "-string", "-file", "-path" -> arg = new Args.Str(option[0], option[3]);
            case "-button", "-reset", "-save" -> arg = new Args.Button(option[0]);
        }
        return arg;
    }

    private void writeToEngine(String line) {
        out.println(line);
        comEvent.invoke(new ComData(true, line));
    }

    private String readFromEngine() {
        String line = in.hasNextLine()
                ? in.nextLine()
                : "";
        comEvent.invoke(new ComData(false, line));
        return line;
    }

    private class Features {
        public boolean ping = false;
        public boolean reuse = false;
        public boolean done = false;
    }
}
