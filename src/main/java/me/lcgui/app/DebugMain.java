package me.lcgui.app;

import me.lcgui.engine.args.AbstractArg;
import me.lcgui.engine.args.Args;
import me.lcgui.game.Game;
import me.lcgui.game.setup.PGN;
import me.lcgui.gui.ArgComponentProvider;
import me.lcgui.lan.NetworkThread;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Scanner;

public class DebugMain {
    private static NetworkThread t;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        try {
            String pgnStr = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);

            PGN pgn = new PGN(pgnStr);
            new Game(pgn);

        } catch (UnsupportedFlavorException e) {
            throw new RuntimeException(e);
        }
    }
}
