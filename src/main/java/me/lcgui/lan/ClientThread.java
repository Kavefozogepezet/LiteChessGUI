package me.lcgui.lan;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

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
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try (
                var socket = new Socket(address, port)
        ) {
            setSocket(socket);
            System.out.println("successful pairing with:\n\tipv4:\t" + address.getHostAddress() + "\n\tport:\t" + port);
            beginReading();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
