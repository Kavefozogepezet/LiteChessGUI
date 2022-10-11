package app;

import game.board.Piece;
import game.board.Square;
import game.movegen.Move;
import lan.ClientThread;
import lan.NetworkThread;
import lan.ServerThread;

import java.io.IOException;
import java.util.Scanner;

public class DebugMain {
    private static NetworkThread t;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        switch (args[0]) {
            case "y" -> server();
            case "n" -> client();
        }
    }

    private static void server() {
        ServerThread s = new ServerThread("test");
        NetworkThread.createConnection(s);
        t = s;
    }

    private static void client() {
        ClientThread c = new ClientThread("test");
        NetworkThread.createConnection(c);
        t = c;
    }
}
