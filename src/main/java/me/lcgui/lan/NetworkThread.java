package me.lcgui.lan;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * Abstract osztály hálózati működés megvalósítására.
 * TCP kapcsolat építésére használatos, amit egy jelszóval lehet felépíteni.
 * A kapcsolaton keresztül szerializált objektumok küldhetők.
 */
public abstract class NetworkThread extends Thread {
    /**
     * A hálózati kapcsolat állapota.
     */
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

    /**
     * @param password a jelszó, amivel csatlakozni lehet.
     */
    public NetworkThread(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * Bezárja a TCP kapcsolatot.
     */
    public void close() {
        try {
            socket.close();
            state = State.SHUT_DOWN;
            invokeConnectionCallback();
        } catch (IOException ignore) {}
    }

    /**
     * @param connectionCallback Kapcsolati állapot megváltozását feldolgozó objektum.
     */
    public void setConnectionCallback(Consumer<State> connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    /**
     * Kapcsolati állapot változásakor hívandó meg.
     */
    protected void invokeConnectionCallback() {
        if(connectionCallback != null)
            connectionCallback.accept(state);
    }

    /**
     * @param receiverCallback Hálózaton beárkezett adatot feldolgozó objektum.
     */
    public void setReceiverCallback(Consumer<Object> receiverCallback) {
        this.receiverCallback = receiverCallback;
    }

    /**
     * Hálózati forgalom esetén hívandó meg.
     * @param obj A hálózaton áljött objektum.
     */
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

    /**
     * A kapcsolat létrejöttekor beállítja a socket-ek, és a stream-eket amin a kommunikáció folyik.
     * @param socket A kapcsolódott socket.
     * @throws IOException A stream-eket nem sikerült kinyerni a socketből.
     */
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

    /**
     * Amíg a beállított socekt kapcsolódva van, folyamatosan olvassa a bemeneti stream-et,
     * és meghívja a feldolgozó eseményt.
     */
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

    /**
     * Üzenetet köld a hálózaton keresztül.
     * @param obj A küldeni kívánt objektom
     * @throws IOException
     */
    public void write(Serializable obj) throws IOException {
        out.writeObject(obj);
        out.flush();
    }

    /**
     * UDP datagram küldése broadcast módban.
     * @param s A socket amin a datagramot elküldi.
     * @param bytes Az adat, amit a datagramba foglal.
     * @throws IOException
     */
    protected void broadcastDatagram(DatagramSocket s, byte[] bytes) throws IOException {
        DatagramPacket packet = new DatagramPacket(
                bytes, bytes.length,
                InetAddress.getByName(BROADCAST_IP), DATAGRAM_PORT);
        s.send(packet);
    }

    /**
     * UDP datagram küldése konkrét címre.
     * @param s A socket amin a datagramot elküldi.
     * @param bytes Az adat, amit a datagramba foglal.
     * @param address A cél cím.
     * @param port A cél port.
     * @throws IOException
     */
    protected void sendDatagram(DatagramSocket s, byte[] bytes, InetAddress address, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(
                bytes, bytes.length, address, port);
        s.send(packet);
        System.out.println("Sent Datagram: " + packet);
    }

    /**
     * Hallgatózik UDP datagram-ra várva.
     * @param s A socket amin hallgatózik.
     * @param maxSize A datagram maximális mérete bájtokban.
     * @return A fogadott csomag.
     * @throws IOException
     */
    protected DatagramPacket receiveDatagram(DatagramSocket s, int maxSize) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[maxSize], maxSize);
        s.receive(packet);
        System.out.println("Recieved Datagram: " + packet);
        return packet;
    }
}
