import engine.EngineConfig;
import engine.EngineListener;
import engine.UCIEngine;
import game.Game;
import game.setup.StartPos;

import java.io.File;

public class DebugMain {
    public static void tryengine() {
        File stockfishPath = new File(System.getProperty("user.dir"));

        String[] path = { "engines", "stockfish_15_win_x64_avx2", "stockfish_15_x64_avx2.exe"};
        for(String str : path) {
            stockfishPath = new File(stockfishPath, str);
        }

        System.out.println("jeee");

        var config = new EngineConfig(
                EngineConfig.Protocol.UCI,
                stockfishPath
        );

        var engine = new UCIEngine(config);
        Thread engineThread = new Thread(engine);
        engineThread.setDaemon(true);
        engineThread.start();

        //Game game = new Game(new StartPos());

        try {
            if(engine.verificationFailure())
                return;

            engine.addListener(new EngineListener() {
                @Override
                public void bestmove(String string) {
                    System.out.println("Move noted");
                }

                @Override
                public void info(String[] info) {}
            });

            engine.startSearch();
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        tryengine();
    }
}
