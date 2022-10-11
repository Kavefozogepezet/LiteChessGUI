package lan;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class ServerThread extends NetworkThread {
    public ServerThread(String id) {
        super(id);
    }

    @Override
    public void run() {
        try (
                var s = new DatagramSocket(DATAGRAM_PORT);
                var ss = new ServerSocket()
        ){
            boolean connectionFound = false;
            do {
                DatagramPacket packet = receiveDatagram(s, 1024);

                String otherId = new String(packet.getData(), 0, packet.getLength());
                if (otherId.equals(getPassword())) {
                    ByteBuffer buffer = ByteBuffer.allocate(8).putInt(ACCEPT_PACKET).putInt(ss.getLocalPort());
                    sendDatagram(s, buffer.array(), packet.getAddress(), packet.getPort());
                    setSocket(ss.accept());
                    connectionFound = true;
                }
            } while(!connectionFound);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (Socket socket = getSocket()) {
            System.out.println("successful pairing with:\n\tipv4:\t" + socket.getInetAddress() + "\n\tport:\t" + socket.getPort());
            beginReading();
        } catch (IOException | ClassNotFoundException e) {
            invokeConnectionCallback(ConnectionEvent.DISCONNECTED);
        }
    }
}
