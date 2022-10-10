package app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread extends NetworkThread {
    private static final int PORT = 60123;

    private boolean isServer;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    Scanner scn = new Scanner(System.in);

    public ClientThread(String id) {
        super(id);
    }

    @Override
    public void run() {
        try (
                Socket socket = new Socket("localhost", PORT)
        ){
            setSocket(socket);
            tryConnect();
            invokeConnectionCallback(true);
            beginReading();
        } catch (IOException | ClassNotFoundException e) {
            invokeConnectionCallback(false);
            throw new RuntimeException(e);
        }
    }

    public void tryConnect() throws IOException, ClassNotFoundException {
        write(id);
        boolean ack = (Boolean) readEcho();
        if (!ack)
            throw new IOException("Id mismatch.");
    }
}
