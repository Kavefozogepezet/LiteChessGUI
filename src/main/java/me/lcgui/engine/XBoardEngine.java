package me.lcgui.engine;

import me.lcgui.engine.args.AbstractArg;
import me.lcgui.engine.args.Args;
import me.lcgui.game.Clock;
import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
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

/**
 * XBoard protocollt megvalósító {@link Engine}
 */
@ProtocolImplementation(name = "XBoard")
public class XBoardEngine extends AbstractEngine {
    private final Features myFeatures = new Features();
    private Move lastPvMove = null;

    public XBoardEngine(EngineConfig config) {
        super(config);
    }

    @Override
    public synchronized void isReady() {
        if(!myFeatures.ping)
            return;

        try {
            writeToEngine("ping 1");
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

        Side side = game.getState().getTurn();
        Clock clock = game.getClock();
        if(game.usesTimeControl()) {
            writeToEngine("time " + clock.getRemainingMs(side));
            writeToEngine("otime " + clock.getRemainingMs(side.other()));
        } else {
            writeToEngine("st 5000");
        }
        writeToEngine("go");
    }

    @Override
    public void stopSearch() {
        writeToEngine("?");
    }

    @Override
    public void playingThis(Game game) {
        writeToEngine("new");
        if(!game.isDefaultStart())
            writeToEngine("setboard " + game.getStartFen());
        writeToEngine("force");
        isReady();

        if(this.game != null)
            this.game.moveEvent.removeListener(onMovePlayed);

        this.game = game;
        lastPvMove = null;
        game.moveEvent.addListener(onMovePlayed);
    }

    @Override
    public void setOption(String option, String value) {
        writeToEngine("option " + option + "=" + value);
    }

    @Override
    protected void handshake() throws EngineVerificationFailure {
        writeToEngine("xboard");
        writeToEngine("protover 2");

        String inputLine;
        do {
            inputLine = readFromEngine();
            String[] line = inputLine.split(" ", 2);
            if (line[0].equals("feature")) {
                readFeatureList(line[1]);
            }
        } while (!myFeatures.done);

        if(myFeatures.ping) {
            writeToEngine("ping 2");
            do {
                inputLine = readFromEngine();
            } while (!inputLine.equals("pong 2"));
        }

        writeToEngine("force");
    }

    @Override
    protected void processLine(String line) {
        String[] strs = line.split(" ");

        if(Character.isDigit(strs[0].charAt(0))) {
            processInfo(line);
            return;
        }

        switch (strs[0]) {
            case "move" -> {
                lastPvMove = convertMove(strs[1]);
                bestEvent.invoke(Consumable.create(lastPvMove));
            }
            case "pong" -> {
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }

    private void processInfo(String line) {
        SearchInfo info = cloneInfo();
        String[] infos = line.split("\t");
        if(infos.length == 2) {
            info.set(SearchInfo.PV, infos[1]);
            infos = infos[0].split(" ");
        }
        info.set(SearchInfo.DEPTH, infos[0]);
        info.set(SearchInfo.TIME, infos[2]);
        info.set(SearchInfo.NODES, infos[3]);

        int score = Math.abs(Integer.parseInt(infos[1]));
        if(score > 100000)
            info.set(SearchInfo.SCORE, "mate in " + (score - 100000));
        else
            info.set(SearchInfo.SCORE, infos[1]);

        int nps = Integer.parseInt(infos[3]) * Integer.parseInt(infos[2]) / 100;
        info.set(SearchInfo.NPS, Integer.toString(nps));

        newInfo(info);
    }

    private void readFeatureList(String features) throws EngineVerificationFailure {
        String remaining = features;
        while (remaining != null) {
            if (remaining.charAt(0) == ' ')
                remaining = remaining.substring(1);

            String[] eqSplit = remaining.split("=", 2);
            String fName = eqSplit[0];
            remaining = eqSplit[1];

            String[] split;
            if (remaining.charAt(0) == '"') {
                remaining = remaining.substring(1);
                split = remaining.split("\"", 2);
            } else {
                split = remaining.split(" ", 2);
            }
            String fValue = split[0];
            remaining = split.length > 1 ? split[1] : null;
            readFeature(fName, fValue);
        }
    }

    private void readFeature(String fName, String fValue) throws EngineVerificationFailure {
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
                if(!"1".equals(fValue))
                    throw new EngineVerificationFailure("Insufficent features");
            }
            case "usermove" -> {
                myFeatures.usermove = "1".equals(fValue);
                writeToEngine("accepted " + fName);
            }
            case "option" -> {
                if(initStart) {
                    var option = readOption(fValue);
                    config.options.put(option.getName(), option);
                }
            }
            default -> writeToEngine("accepted " + fName);
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

    private class Features {
        public boolean ping = false;
        public boolean done = false;
        public boolean usermove = false;
    }

    private final Event.Listener<Game.MoveData> onMovePlayed = moveData -> {
        Move move = moveData.move;
        if (move.equals(lastPvMove)) {
            lastPvMove = null;
            return;
        }

        String moveStr = move.toString();
        if (myFeatures.usermove)
            moveStr = "usermove " + moveStr;
        writeToEngine(moveStr);
    };
}
