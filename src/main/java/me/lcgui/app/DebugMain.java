package me.lcgui.app;

import me.lcgui.lan.ClientThread;
import me.lcgui.lan.NetworkThread;
import me.lcgui.lan.ServerThread;

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
        s.start();
        t = s;
    }

    private static void client() {
        ClientThread c = new ClientThread("test");
        c.start();
        t = c;
    }
}
