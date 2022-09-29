package engine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UCIEngine implements Runnable {
    EngineConfig config;
    Process engineProcess = null;
    boolean isok = false;
    boolean running = false;

    Scanner in;
    PrintStream out;


    public UCIEngine(EngineConfig config) {
        this.config = config;
    }

    public void go() {
        out.println("go");
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

            out.println("d");

            while(running) {
                System.out.println(in.nextLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
