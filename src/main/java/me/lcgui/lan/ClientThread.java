package me.lcgui.lan;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Hálózati kommunikációra képes osztály kliens szerepében.
 * A kliens egy {@link ServerThread}-hez kapcsolódik.
 */
public class ClientThread extends NetworkThread {

    public ClientThread(String password) {
        super(password);
    }

    @Override
    public void run() {
        InetAddress address = null;
        int port = 0;

        try (
                var s = new DatagramSocket()
        ) {
            s.setBroadcast(true);
            broadcastDatagram(s, getPassword().getBytes());
            var packet = receiveDatagram(s, 1024);

            ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
            int response = buffer.getInt();
            port = buffer.getInt();

            if(response != ACCEPT_PACKET)
                return;

            address = packet.getAddress();
            Thread.sleep(1000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            return;
        }

        try (
                var socket = new Socket(address, port)
        ) {
            setSocket(socket);
            System.out.println("successful pairing with:\n\tipv4:\t" + address.getHostAddress() + "\n\tport:\t" + port);
            synchronized (this) { notifyAll(); }
            beginReading();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
