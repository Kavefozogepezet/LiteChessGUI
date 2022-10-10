package app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public abstract class NetworkThread implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    protected final String id;

    private Consumer<Object> receiverCallback = null;
    private Consumer<Boolean> connectionCallback = null;

    public NetworkThread(String id) {
        this.id = id;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignore) {}
    }

    public void setConnectionCallback(Consumer<Boolean> connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    protected void invokeConnectionCallback(boolean isSuccess) {
        if(connectionCallback != null)
            connectionCallback.accept(isSuccess);
    }

    public void setReceiverCallback(Consumer<Object> receiverCallback) {
        this.receiverCallback = receiverCallback;
    }

    protected void invokeReceiverCallback(Object obj) {
        if(receiverCallback != null)
            receiverCallback.accept(obj);
    }

    protected void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
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

    protected void write(Object obj) throws IOException {
        out.writeObject(obj);
        out.flush();
    }
}
