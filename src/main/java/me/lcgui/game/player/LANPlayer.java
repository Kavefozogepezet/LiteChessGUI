package me.lcgui.game.player;

import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
import me.lcgui.game.movegen.Move;
import me.lcgui.gui.SelectablePlayer;
import me.lcgui.gui.factory.LANPlayerFactory;
import me.lcgui.lan.NetworkThread;

import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;

@SelectablePlayer(name = "Player over LAN", factoryClass = LANPlayerFactory.class)
public class LANPlayer implements Player {
    // TODO sync comments
    private enum Command {
        NAME, // name sync
        SIDE, // side sync
        GAME, // sending game to client side
        INIT_READY, // sent all info
        MOVE, // move has been made
        RESULT // game has ended on host
    }

    private static class Packet implements Serializable {
        public final Command command;
        public final Object payload;

        public Packet(Command cmd, Object obj) {
            this.command = cmd;
            this.payload = obj;
        }

        public Packet(Command cmd) {
            this(cmd, null);
        }

        @Override
        public String toString() {
            return command.toString() + (payload == null ? "" : " : " + payload.toString());
        }
    }

    private transient NetworkThread thread;
    private transient boolean host;

    private transient Game game;
    private Side mySide;
    private boolean myTurn = false;
    private String name;

    private Move myMove = null;

    private boolean initialized = false;

    public LANPlayer(NetworkThread thread, boolean host) {
        this.host = host;
        this.thread = thread;
        this.thread.setReceiverCallback(this::inputHandler);
    }

    public void initGame(HumanPlayer other) throws IOException, InterruptedException {
        if(host)
            throw new RuntimeException("Cannot call on a host");

        synchronized (this) {
            sendPacket(Command.NAME, other.getName());
            sendPacket(Command.INIT_READY);
            wait(); // wait until INIT_READY received
        }
        game.setPlayer(mySide.other(), other);
    }

    @Override
    public void myTurn() {
        myTurn = true;
        try {
            if(!game.getMoveList().isEmpty()) {
                sendPacket(Command.MOVE, game.getMoveList().getLast().move);
            }
        } catch (IOException e) {
            game.resign();
        }
    }

    @Override
    public void cancelTurn() {
        myTurn = false;
    }

    @Override
    public void setGame(Game game, Side mySide) {
        if(this.game != null)
            throw new RuntimeException("The game can be set only once.");

        this.game = game;
        this.mySide = mySide;
        if(host) {
            try {
                sendPacket(Command.GAME, game);
                sendPacket(Command.SIDE, mySide.other());
                SwingUtilities.invokeLater(() -> { // Cannot finnish initialization now, because the other player might be null.
                    try {
                        sendPacket(Command.NAME, game.getPlayer(mySide.other()).getName());
                        sendPacket(Command.INIT_READY);
                    } catch (IOException e) {
                        game.endGame(Game.Result.lost(mySide), Game.Termination.ABANDONED);
                    }
                });

                synchronized (this) {
                    if(!initialized)
                        wait();
                }
            } catch (IOException e) {
                game.endGame(Game.Result.lost(mySide), Game.Termination.ABANDONED);
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void gameEnd() {
        try {
            sendPacket(Command.RESULT, game.getResultData());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        thread.close();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void handleDrawClaim() {}

    public Game getGame() {
        return game;
    }

    public Side getSide() {
        return mySide;
    }

    public void sendPacket(Command cmd, Object data) throws IOException {
        thread.write(new Packet(cmd, data));
    }

    public void sendPacket(Command cmd) throws IOException {
        thread.write(new Packet(cmd));
    }

    private synchronized void inputHandler(Object object) {
        if(!(object instanceof Packet packet))
            return;

        if(packet.command == Command.NAME) {
            name = (String) packet.payload;
        } else if(packet.command == Command.MOVE) {
            if (!myTurn)
                return;

            Move move = (Move) packet.payload;
            if (game.isValidMove(move)) {
                game.play(move);
            } else {
                game.resign();
            }
        } else if(packet.command == Command.INIT_READY) {
            initialized = true;
            if (!host) {
                Game g = this.game;
                this.game = null;
                g.setPlayer(mySide, this);
            }
            notify();
        } else if(packet.command == Command.RESULT) {
            Game.ResultData data = (Game.ResultData) packet.payload;
            game.endGame(data.result, data.termination);
        } else if(!host && packet.command == Command.GAME) {
            game = (Game) packet.payload;
        } else if(!host && packet.command == Command.SIDE) {
            mySide = (Side) packet.payload;
        }
    }
}
