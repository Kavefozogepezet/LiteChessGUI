package app;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

class ServerThread extends NetworkThread {
    private static final int PORT = 60123;

    private boolean isServer;

    private ObjectInputStream in;
    private ObjectOutputStream out;


    Scanner scn = new Scanner(System.in);

    public ServerThread(String id) {
        super(id);
    }

    @Override
    public void run() {
        ServerSocket ss;
        try {
            ss = new ServerSocket(PORT, 0, InetAddress.getByName(null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean connected = false;
        do {
            try (Socket socket = ss.accept()) {
                setSocket(socket);
                checkId(socket);
                connected = true;
                invokeConnectionCallback(true);
                beginReading();
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println(ex.getMessage());
                invokeConnectionCallback(false);
            }
        } while(!connected);
    }

    private void checkId(Socket socket) throws IOException, ClassNotFoundException {
        String sentId = (String) readEcho();
        boolean correctPw = sentId.equals(id);
        write(correctPw);

        if (!correctPw)
            throw new IOException("Id mismatch");
    }
}
