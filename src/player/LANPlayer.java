package player;

import game.Game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LANPlayer implements Player {
    @Override
    public void myTurn() {

    }

    @Override
    public void cancelTurn() {

    }

    @Override
    public void setGame(Game game) {

    }

    @Override
    public void gameEnd() {

    }

    @Override
    public String getName() {
        return null;
    }

    private class SocketThread implements Runnable {
        private static final int PORT = 60123;

        private boolean isServer;
        private String id;
        private Socket socket;

        public SocketThread(boolean isServer, String id) {
            this.isServer = isServer;
            this.id = id;
        }


        @Override
        public void run() {
            try {
                if (isServer) {
                    ServerSocket serverSocket = new ServerSocket(PORT);
                    socket = serverSocket.accept();
                } else {
                    socket = new Socket("localhost", PORT);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
