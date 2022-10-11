package lan;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.function.Consumer;

public abstract class NetworkThread extends Thread {
    public static final int DATAGRAM_PORT = 8888;
    public static final int ACCEPT_PACKET = 176394482;
    public static final String BROADCAST_IP = "255.255.255.255";

    public static void createConnection(NetworkThread run) {
        Thread thread = new Thread(run);
        //thread.setDaemon(true);
        try {
            synchronized (run) {
                thread.start();
                run.wait(); // wait until connection
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public enum ConnectionEvent {
        CONNECTED, DISCONNECTED, COULD_NOT_CONNECT, SHUT_DOWN
    }

    private int tcp_port;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final String password;

    private Consumer<Object> receiverCallback = null;
    private Consumer<ConnectionEvent> connectionCallback = null;

    public NetworkThread(String password) {
        this.password = password;
        setDaemon(true);
    }

    public String getPassword() {
        return password;
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public synchronized void close() {
        try {
            socket.close();
            connectionCallback.accept(ConnectionEvent.SHUT_DOWN);
        } catch (IOException ignore) {}
    }

    public synchronized void setConnectionCallback(Consumer<ConnectionEvent> connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    protected void invokeConnectionCallback(ConnectionEvent e) {
        if(connectionCallback != null)
            connectionCallback.accept(e);
    }

    public synchronized void setReceiverCallback(Consumer<Object> receiverCallback) {
        this.receiverCallback = receiverCallback;
    }

    protected void invokeReceiverCallback(Object obj) {
        if(receiverCallback != null)
            receiverCallback.accept(obj);
    }

    protected synchronized void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        synchronized (this) {
            notifyAll(); // notify threads waiting for connection
        }
    }

    protected Socket getSocket() {
        return socket;
    }

    protected void beginReading() throws IOException, ClassNotFoundException {
        while (socket.isConnected())
            invokeReceiverCallback(readEcho());
    }

    protected Object readEcho() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        System.out.println("echo read:" + obj);
        return obj;
    }

    public void write(Object obj) throws IOException {
        out.writeObject(obj);
        out.flush();
    }

    protected void broadcastDatagram(DatagramSocket s, byte[] bytes) throws IOException {
        DatagramPacket packet = new DatagramPacket(
                bytes, bytes.length,
                InetAddress.getByName(BROADCAST_IP), DATAGRAM_PORT);
        s.send(packet);
    }

    protected void sendDatagram(DatagramSocket s, byte[] bytes, InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(
                bytes, bytes.length, address, port);
        s.send(packet);
    }

    protected DatagramPacket receiveDatagram(DatagramSocket s, int maxSize) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[maxSize], maxSize);
        s.receive(packet);
        return packet;
    }
}
