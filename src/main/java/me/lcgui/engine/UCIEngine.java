package me.lcgui.engine;

import me.lcgui.engine.args.*;
import me.lcgui.game.Clock;
import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
import me.lcgui.game.board.Square;
import me.lcgui.game.movegen.Move;
import me.lcgui.misc.Consumable;
import me.lcgui.misc.Event;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

@ProtocolImplementation(name = "UCI")
public class UCIEngine extends AbstractEngine {
    public UCIEngine(EngineConfig config) {
        super(config);
    }

    public synchronized void isReady() {
        try {
            writeToEngine("isready");
            this.wait();
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
    public void playingThis(Game game) {
        writeToEngine("ucinewgame");
        isReady();
        this.game = game;
    }

    @Override
    public void setOption(String option, String value) {
        writeToEngine("setoption name " + option + " value " + value);
    }

    @Override
    protected void handshake() {
        writeToEngine("uci");

        String inputLine;
        do {
            inputLine = readFromEngine();
            String[] line = inputLine.split(" ");
            if (line[0].equals("id") && line[1].equals("name"))
                name = line[2];
            else if (initStart && line[0].equals("option")) {
                var option = readOption(line);
                config.options.put(option.getName(), option);
            }
        } while (!inputLine.equals("uciok"));

        writeToEngine("ucinewgame");
        writeToEngine("isready");
        do {
            inputLine = readFromEngine();
        } while (!inputLine.equals("readyok"));
    }

    @Override
    protected void processLine(String line) {
        String[] strs = line.split(" ");

        switch (strs[0]) {
            case "info" -> processInfo(strs);
            case "bestmove" -> {
                Move best = convertMove(strs[1]);
                bestEvent.invoke(Consumable.create(best));
            }
            case "readyok" -> {
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }

    private void processInfo(String[] infoArray) {
        SearchInfo info = cloneInfo();

        for(int i = 1; i < infoArray.length; i++) {
            switch (infoArray[i]) {
                case "depth" -> info.set(SearchInfo.DEPTH, infoArray[++i]);
                case "time" -> info.set(SearchInfo.TIME, infoArray[++i]);
                case "nodes" -> info.set(SearchInfo.NODES, infoArray[++i]);
                case "nps" -> info.set(SearchInfo.NPS, infoArray[++i]);
                case "score" -> {
                    String type = infoArray[++i];
                    StringBuilder scoreStr = new StringBuilder();
                    if(type.equals("cp")) {
                        scoreStr.append(infoArray[++i]);
                        if(infoArray[i + 1].equals("upperbound")) {
                            scoreStr.insert(0, '<');
                            i++;
                        } else if(infoArray[i + 1].equals("lowerbound")) {
                            scoreStr.insert(0, '>');
                            i++;
                        }
                    } else if(type.equals("mate")) {
                        scoreStr.append("mate in ").append(infoArray[++i]);
                    }
                    info.set(SearchInfo.SCORE, scoreStr.toString());
                }
                case "pv" -> {
                    StringBuilder pv = new StringBuilder();
                    for(int pvIdx = i + 1; pvIdx < infoArray.length; pvIdx++)
                        pv.append(infoArray[pvIdx]).append(" ");

                    info.set(SearchInfo.PV, pv.toString());
                }
            }
        }
        newInfo(info);
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
            case "check" -> option = new Args.Check(name, Boolean.parseBoolean(value));
            case "spin" -> option = new Args.Spin(name, min, max, Integer.parseInt(value));
            case "combo" -> option = new Args.Combo(name, value, options);
            case "button" -> option = new Args.Button(name);
            case "string" -> option = new Args.Str(name, value);
        }
        return option;
    }
}
