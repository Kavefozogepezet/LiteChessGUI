package me.lcgui.engine;

import me.lcgui.engine.args.*;
import me.lcgui.game.Clock;
import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

@ProtocolImplementation(name = "UCI")
public class UCIEngine implements Engine {
    private final EngineConfig config;
    private String name;
    private boolean running = false;
    private boolean initStart = false;
    private boolean searching = false;

    private Game game;
    private String pvMove = null;

    public final Event<Consumable<String>> bestEvent = new Event<>();
    public final Event<SearchInfo> infoEvent = new Event<>();
    public final Event<ComData> comEvent = new Event<>();

    private Scanner in;
    private PrintStream out;

    private SearchInfo lastInfo = new SearchInfo();

    public UCIEngine(EngineConfig config) {
        this.config = config;
        name = config.file.getName(); // temporary name
    }

    @Override
    public void verify() throws EngineVerificationFailure {
        if(running)
            return;

        try {
            synchronized (this) {
                wait(10000);
                if(!running)
                    throw new EngineVerificationFailure(
                            "Engine not responded correctly, check that the it is installed with the correct protocol"
                    );
            }
        } catch (InterruptedException ignored) {
            throw new EngineVerificationFailure("Thread interrupted");
        }
    }

    public void isReady() {
        try {
            synchronized (this) {
                writeToEngine("isready");
                this.wait();
            }
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void quit() {
        writeToEngine("quit");
    }

    @Override
    public void startSearch() {
        if(game.hasEnded())
            return;

        setPosition();

        StringBuilder command = new StringBuilder("go");
        if(game.usesTimeControl()) {
            Clock clock = game.getClock();
            command
                    .append(" wtime ").append(clock.getRemainingMs(Side.White))
                    .append(" winc ").append(clock.format.inc[Side.White.ordinal()])
                    .append(" btime ").append(clock.getRemainingMs(Side.Black))
                    .append(" binc ").append(clock.format.inc[Side.Black.ordinal()]);
        } else {
            command.append(" movetime 5000");
        }
        writeToEngine(command.toString());
        searching = true;
    }

    @Override
    public void stopSearch() {
        writeToEngine("stop");
        searching = false;
    }

    @Override
    public boolean isSearching() {
        return searching;
    }

    @Override
    public Event<Consumable<String>> getBestEvent() {
        return bestEvent;
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
    public void playingThis(Game game) {
        writeToEngine("ucinewgame");
        isReady();
        this.game = game;
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
        writeToEngine("setoption name " + option + " value " + value);
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

            running = true;
            writeToEngine("uci");

            String inputLine;
            do {
                inputLine = readFromEngine();
                String[] line = inputLine.split(" ");
                if (line[0].equals("id") && line[1].equals("name"))
                    name = line[2];
                else if (initStart && line[0].equals("option")) {
                    var option = readOption(line);
                    config.options.put(option.name, option);
                }
            } while (!inputLine.equals("uciok"));

            writeToEngine("ucinewgame");
            writeToEngine("isready");
            do {
                inputLine = readFromEngine();
            } while (!inputLine.equals("readyok"));

            running = true;
            synchronized (this) {
                this.notifyAll();
            }

            while (in.hasNextLine()) {
                String line = readFromEngine();
                String[] strs = line.split(" ");

                switch (strs[0]) {
                    case "info" -> processInfo(strs);
                    case "bestmove" -> {
                        pvMove = strs[1];
                        bestEvent.invoke(Consumable.create(pvMove));
                    }
                    case "readyok" -> {
                        synchronized (this) {
                            notifyAll();
                        }
                    }
                }
            }
        } catch (IOException ignored) {}
        finally {
            engineProcess.destroy();
        }
    }

    private void processInfo(String[] info) {
        SearchInfo newInfo = new SearchInfo(lastInfo);

        for(int i = 1; i < info.length; i++) {
            switch (info[i]) {
                case "depth" -> newInfo.set(SearchInfo.DEPTH, info[++i]);
                case "time" -> newInfo.set(SearchInfo.TIME, info[++i]);
                case "nodes" -> newInfo.set(SearchInfo.NODES, info[++i]);
                case "nps" -> newInfo.set(SearchInfo.NPS, info[++i]);
                case "score" -> {
                    String type = info[++i];
                    StringBuilder scoreStr = new StringBuilder();
                    if(type.equals("cp")) {
                        scoreStr.append(info[++i]);
                        if(info[i + 1].equals("upperbound")) {
                            scoreStr.insert(0, '<');
                            i++;
                        } else if(info[i + 1].equals("lowerbound")) {
                            scoreStr.insert(0, '>');
                            i++;
                        }
                    } else if(type.equals("mate")) {
                        scoreStr.append("mate in ").append(info[++i]);
                    }
                    newInfo.set(SearchInfo.SCORE, scoreStr.toString());
                }
                case "pv" -> {
                    StringBuilder pv = new StringBuilder();
                    for(int pvIdx = i + 1; pvIdx < info.length; pvIdx++)
                        pv.append(info[pvIdx]).append(" ");

                    newInfo.set(SearchInfo.PV, pv.toString());
                }
            }
        }
        if(newInfo.isDirty() && !newInfo.equals(lastInfo)) {
            lastInfo = newInfo;
            infoEvent.invoke(newInfo);
        }
    }

    private void setPosition() {
        StringBuilder command = new StringBuilder("position ");

        if(game.isDefaultStart())
            command.append("startpos");
        else
            command.append("fen ").append(game.getStartFen());

        if(!game.getMoveList().isEmpty()) {
            command.append(" moves");
            for(var move : game.getMoveList()) {
                command.append(" ").append(move.move);
            }
        }
        writeToEngine(command.toString());
    }

    private AbstractArg<?> readOption(String[] line) {
        StringBuilder nameBuilder = new StringBuilder();
        String type = "-", value = "";
        int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE;
        ArrayList<String> options = new ArrayList<>();

        int idx = 2;
        while(!line[idx].equals("type"))
            nameBuilder.append(line[idx++]);

        type = line[++idx];

        for(; idx < line.length - 1; idx++) {
            switch (line[idx]) {
                case "default" -> value = line[++idx];
                case "min" -> min = Integer.parseInt(line[++idx]);
                case "max" -> max = Integer.parseInt(line[++idx]);
                case "var" -> options.add(line[++idx]);
            }
        }

        String name = nameBuilder.toString();
        AbstractArg<?> option = null;
        switch (type) {
            case "check" -> option = new CheckArg(name, Boolean.parseBoolean(value));
            case "spin" -> option = new SpinArg(name, min, max, Integer.parseInt(value));
            case "combo" -> option = new ComboArg(name, value, options);
            case "button" -> option = new ButtonArg(name);
            case "string" -> option = new StringArg(name, value);
        }
        return option;
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
}
