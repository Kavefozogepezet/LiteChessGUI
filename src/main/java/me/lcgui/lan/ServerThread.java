package me.lcgui.lan;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Hálózati kommunikációra képes osztály szerver szerepében.
 * A szerver csak egy darab {@link ClientThread}-hez kapcsolódik.
 */
public class ServerThread extends NetworkThread {
    public ServerThread(String password) {
        super(password);
    }

    @Override
    public void run() {
        try (
                var s = new DatagramSocket(DATAGRAM_PORT);
                var ss = new ServerSocket(0)
        ){
            boolean connectionFound = false;
            do {
                DatagramPacket packet = receiveDatagram(s, 1024);

                String otherPw = new String(packet.getData(), 0, packet.getLength());
                if (otherPw.equals(getPassword())) {
                    ByteBuffer buffer = ByteBuffer.allocate(8).putInt(ACCEPT_PACKET).putInt(ss.getLocalPort());
                    sendDatagram(s, buffer.array(), packet.getAddress(), packet.getPort());
                    setSocket(ss.accept());
                    connectionFound = true;
                }
            } while(!connectionFound);
        } catch (IOException e) {
            setState(State.FAILED);
            return;
        }

        try (Socket socket = getSocket()) {
            System.out.println("successful pairing with:\n\tipv4:\t" + socket.getInetAddress() + "\n\tport:\t" + socket.getPort());
            synchronized (this) { notifyAll(); }
            beginReading();
        } catch (IOException e) {
            setState(State.DISCONNECTED);
        }
    }
}
