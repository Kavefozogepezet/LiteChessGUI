package app;

import game.board.Piece;
import game.board.Square;
import game.movegen.Move;

import java.io.IOException;
import java.util.Scanner;

public class DebugMain {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        switch (args[0]) {
            case "y" -> server();
            case "n" -> client();
        }
    }

    private static void server() {
        ServerThread s = new ServerThread("test");
        boolean success = false;

        synchronized (s) {
            startThread(s);
            s.setConnectionCallback((isSuccess) -> {
                synchronized (s) {
                    s.notifyAll();
                }
            });

            try {
                s.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if(!s.getSocket().isConnected())
            return;

        var scn = new Scanner(System.in);
        String line;
        do {
            line = scn.nextLine();
            try {
                s.write(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (!"exit".equals(line));
    }

    private static void client() {
        ClientThread c = new ClientThread("test");
        startThread(c);

        c.setReceiverCallback((data) -> {});
    }

    private static void startThread(NetworkThread obj) {
        Thread thread = new Thread(obj);
        //thread.setDaemon(true);
        thread.start();
    }
}
