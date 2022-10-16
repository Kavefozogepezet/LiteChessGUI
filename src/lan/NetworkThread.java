package lan;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.function.Consumer;

public abstract class NetworkThread extends Thread {
    public enum State {
        NOT_CONNECTED, CONNECTED, FAILED, DISCONNECTED, SHUT_DOWN
    }

    public static final int DATAGRAM_PORT = 8888;
    public static final int ACCEPT_PACKET = 176394482;
    public static final String BROADCAST_IP = "255.255.255.255";

    private State state = State.NOT_CONNECTED;
    private int tcp_port;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final String password;

    private Consumer<Object> receiverCallback = null;
    private Consumer<State> connectionCallback = null;

    public NetworkThread(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void close() {
        try {
            socket.close();
            connectionCallback.accept(State.SHUT_DOWN);
        } catch (IOException ignore) {}
    }

    public void setConnectionCallback(Consumer<State> connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    protected void invokeConnectionCallback() {
        if(connectionCallback != null)
            connectionCallback.accept(state);
    }

    public void setReceiverCallback(Consumer<Object> receiverCallback) {
        this.receiverCallback = receiverCallback;
    }

    protected void invokeReceiverCallback(Object obj) {
        if(receiverCallback != null)
            receiverCallback.accept(obj);
    }

    protected void setState(State state) {
        if(this.state != state) {
            this.state = state;
            invokeConnectionCallback();
        }
    }

    protected void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        synchronized (this) {
            notifyAll(); // notify threads waiting for connection
        }
        setState(State.CONNECTED);
    }

    protected Socket getSocket() {
        return socket;
    }

    protected void beginReading() {
        while (socket.isConnected()) {
            try {
                invokeReceiverCallback(readEcho());
            } catch (EOFException e) {
                if(state == State.CONNECTED)
                    state = State.SHUT_DOWN;
                invokeConnectionCallback();
            } catch (IOException | ClassNotFoundException e) {
                if(state == State.CONNECTED)
                    state = State.DISCONNECTED;
                invokeConnectionCallback();
            }
        }
    }

    private Object readEcho() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        System.out.println("echo read: " + obj);
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
        System.out.println("Sent Datagram: " + packet);
    }

    protected DatagramPacket receiveDatagram(DatagramSocket s, int maxSize) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[maxSize], maxSize);
        s.receive(packet);
        System.out.println("Recieved Datagram: " + packet);
        return packet;
    }
}
