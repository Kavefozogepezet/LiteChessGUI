package me.lcgui.player;

import me.lcgui.game.Game;
import me.lcgui.game.board.Side;
import me.lcgui.game.movegen.Move;
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
        SYNC // clock synchronisation
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
            thread.write(new Packet(Command.NAME, other.getName()));
            thread.write(new Packet(Command.INIT_READY));
            wait(); // wait until INIT_READY received
        }
        game.setPlayer(mySide.other(), other);
    }

    @Override
    public void myTurn() {
        myTurn = true;
        try {
            if(!game.getMoveList().isEmpty()) {
                thread.write(new Packet(Command.MOVE, game.getMoveList().getLast().move));
                if (host && game.usesTimeControl())
                    thread.write(new Packet(Command.SYNC, game.getClock()));
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
                thread.write(new Packet(Command.GAME, game));
                thread.write(new Packet(Command.SIDE, mySide.other()));
                SwingUtilities.invokeLater(() -> { // Cannot finnish initialization now, because the other player might be null.
                    try {
                        thread.write(new Packet(Command.NAME, game.getPlayer(mySide.other()).getName()));
                        thread.write(new Packet(Command.INIT_READY));
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

    private synchronized void inputHandler(Object object) {
        if(!(object instanceof Packet packet))
            return;

        try {
            if(packet.command == Command.NAME) {
                name = (String) packet.payload;
            } else if(packet.command == Command.MOVE) {
                if (!myTurn)
                    return;

                Move move = (Move) packet.payload;
                if (game.isValidMove(move)) {
                    game.play(move);
                    if (host && game.usesTimeControl())
                        thread.write(new Packet(Command.SYNC, game.getClock()));
                } else {
                    game.resign();
                }
            } else if(packet.command == Command.INIT_READY) {
                initialized = true;
                if(!host) {
                    Game g = this.game;
                    this.game = null;
                    g.setPlayer(mySide, this);
                }
                notify();
            } else if(!host && packet.command == Command.SYNC) {
                // TODO adjust clock : game.setClock((Clock) packet.payload);
            } else if(!host && packet.command == Command.GAME) {
                game = (Game) packet.payload;
            } else if(!host && packet.command == Command.SIDE) {
                mySide = (Side) packet.payload;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
