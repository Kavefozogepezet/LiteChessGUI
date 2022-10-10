package lan;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class ClientThread extends NetworkThread {

    public ClientThread(String id) {
        super(id);
    }

    @Override
    public void run() {
        InetAddress address = null;
        int port = 0;

        try (
                var s = new DatagramSocket()
        ) {
            s.setBroadcast(true);
            broadcastDatagram(s, getId().getBytes());
            var packet = receiveDatagram(s, 1024);

            ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
            int response = buffer.getInt();
            port = buffer.getInt();

            if(response != ACCEPT_PACKET)
                return;

            address = packet.getAddress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (
                var socket = new Socket(address, port)
        ) {
            setSocket(socket);
            System.out.println("successful pairing with:\n\tipv4:\t" + address.getHostAddress() + "\n\tport:\t" + port);
            beginReading();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
