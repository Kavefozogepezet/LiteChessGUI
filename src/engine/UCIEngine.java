package engine;

import game.Clock;
import game.Game;
import game.board.Side;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Scanner;

public class UCIEngine implements Engine {
    private EngineConfig config;
    private String name;
    private Process engineProcess = null;
    private boolean isok = false;
    private boolean running = false;

    private Game game;
    private String pvMove = null;
    private LinkedList<EngineListener> listeners = new LinkedList<>();

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
        out.println("isready");
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void quit() {
        out.println("quit");
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
        out.println(command);
    }

    @Override
    public void stopSearch() {
        out.println("stop");
    }

    @Override
    public void addListener(EngineListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(EngineListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void playingThis(Game game) {
        out.println("ucinewgame");
        isReady();
        this.game = game;
    }

    @Override
    public Game getCurrentGame() {
        return game;
    }

    @Override
    public void setOption(String option, String value) {
        out.println("setoption name " + option + " value " + value);
    }

    @Override
    public String getEngineName() {
        return name;
    }

    @Override
    public void run() {
        System.out.println("Engine start");
        ProcessBuilder processBuilder = new ProcessBuilder(config.file.getPath());
        try {
            engineProcess = processBuilder.start();
            running = true;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        try(
            var input = engineProcess.getInputStream();
            var output = engineProcess.getOutputStream();
            ) {
            in = new Scanner(input);
            out = new PrintStream(output, true, StandardCharsets.UTF_8);

            out.println("uci");

            String inputLine;
            do {
                inputLine = in.nextLine();
            } while(inputLine.equals("uciok"));

            out.println("ucinewgame");
            out.println("isready");
            do {
                inputLine = in.nextLine();
                String[] line = inputLine.split(" ");
                if(line[0].equals("id") && line[1].equals("name"))
                    name = line[2];
            } while(inputLine.equals("readyok"));

            running = true;
            synchronized (this) {
                this.notifyAll();
            }

            while(in.hasNextLine()) {
                String line = in.nextLine();
                String[] strs = line.split(" ");

                switch (strs[0]) {
                    case "info" -> processInfo(strs);
                    case "bestmove" -> {
                        pvMove = strs[1];
                        System.out.println("GOT BEST: " + pvMove);
                        for(var listener : listeners) {
                            listener.bestmove(pvMove);
                        }
                    }
                    case "readyok" -> {
                        synchronized (this) {
                            notifyAll();
                        }
                    }
                    default -> System.out.println("[ENGINE DEBUG]: " + line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Bye bye!");
    }

    private void processInfo(String[] info) {
        boolean infoChanged = false;

        SearchInfo newInfo = new SearchInfo(lastInfo);

        for(int i = 1; i < info.length; i++) {
            switch (info[i]) {
                case "depth" -> {
                    newInfo.set(SearchInfo.DEPTH, info[++i]);
                    infoChanged = true;
                }
                case "time" -> {
                    newInfo.set(SearchInfo.TIME, info[++i]);
                    infoChanged = true;
                }
                case "nodes" -> {
                    newInfo.set(SearchInfo.NODES, info[++i]);
                    infoChanged = true;
                }
                case "nps" -> {
                    newInfo.set(SearchInfo.NPS, info[++i]);
                    infoChanged = true;
                }
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
                    infoChanged = true;
                }
                case "pv" -> {
                    StringBuilder pv = new StringBuilder();
                    for(int pvIdx = i + 1; pvIdx < info.length; pvIdx++)
                        pv.append(info[pvIdx]).append(" ");

                    newInfo.set(SearchInfo.PV, pv.toString());
                    infoChanged = true;
                }
            }
        }

        if(infoChanged && !newInfo.equals(lastInfo)) {
            lastInfo = newInfo;
            for (var listener : listeners)
                listener.info(newInfo);
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
                command.append(" ").append(move);
            }
        }
        out.println(command);
    }
}
