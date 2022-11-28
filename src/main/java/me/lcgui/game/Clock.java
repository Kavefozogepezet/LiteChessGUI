package me.lcgui.game;

import me.lcgui.game.board.Side;
import me.lcgui.misc.Event;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * Sakkóra, amely támogatja a népszerű formátumokat.
 */
public class Clock implements Serializable {
    /**
     * Sakkóra formátumának leírására szolgáló osztály.
     * Statikus változóként definiál néhány népszerű formátumot.
     */
    public static class Format implements Serializable {
        public static final int MINUTE = 60;
        public static final int HOUR = MINUTE * 60;

        public static final Format FIDE_Blitz = new Format(3 * MINUTE, 2, IncType.FISCHER, "FIDE Blitz (3|2)");
        public static final Format FIDE_Rapid = new Format(15 * MINUTE, 10, IncType.FISCHER, "FIDE Rapid (15|10)");
        public static final Format Classical = new Format(30 * MINUTE, "Classical (30 min)");
        public static final Format Rapid = new Format(10 * MINUTE, "Rapid (10 min)");
        public static final Format Blitz = new Format(5 * MINUTE, "Blitz (5 min)");
        public static final Format Bullet = new Format(MINUTE, "Bullet (1 min)");

        /**
         * A metódus, amivel a sakkóra a lépések utáni többletidőt hozzáadja a megmaradt időhöz.
         */
        public enum IncType {
            NONE, DELAY, BRONSTEIN, FISCHER
        }

        public final int[]
                time = new int[2],
                inc = new int[2];
        public final IncType type;
        private final String name;

        /**
         * Formátumo létrehozása.
         * Az időmennyiségeket századmásodpercben kell megadni.
         * @param wtime világos ideje.
         * @param winc világos többletideje lépés után.
         * @param btime sötét ideje.
         * @param binc sötét többletideje lépés után.
         * @param type többletidő hozzáadásának metódusa.
         * @param name a formátum elnevezése.
         */
        public Format(int wtime,int winc,int btime,int binc, IncType type, String name) {
            this.time[Side.White.ordinal()] = wtime * 10;
            this.inc[Side.White.ordinal()] = winc * 10;
            this.time[Side.Black.ordinal()] = btime * 10;
            this.inc[Side.Black.ordinal()] = binc * 10;
            this.type = type;
            this.name = name;
        }

        /**
         * Formátum létrehozása, világos és sötét ideje egyenlő.
         * @param time a játékosok ideje
         * @param inc a többletidő lépés után
         * @param type többletidő hozzáadásának metódusa.
         * @param name a formátum elnevezése.
         */
        public Format(int time, int inc, IncType type, String name) {
            this(time, inc, time, inc, type, name);
        }

        /**
         * Formátum létrehozása többletidő nélkül.
         * @param time A játékosok ideje.
         * @param name a formátum elnevezése.
         */
        public Format(int time, String name) {
            this(time, 0, time, 0, IncType.NONE, name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Századmásodperc eltelésének esemélye, ekkor az óra frissíti az idejét.
     */
    public final Event<Side> tickEvent = new Event<>();

    /**
     * Az egyik játékos kifutott az időből.
     */
    public final Event<Side> outOfTimeEvent = new Event<>();

    private final int[] remaining = new int[2]; // remaining time in 100ms
    private final int[] used = new int[2]; // time used since last move
    private Side clockState = Side.White;
    public final Format format;
    private transient Timer timer = new Timer(100, e -> reduceTime());

    /**
     * @param format A sakkóra formátuma.
     * @param side2move A soron következő fél.
     */
    public Clock(Format format, Side side2move) {
        this.format = format;
        reset(side2move);
    }

    /**
     * Elindítja a sakkórát.
     */
    public void start() {
        timer.start();
    }

    /**
     * Leállítja a sakkórát.
     */
    public void stop() {
        timer.stop();
    }

    /**
     * Alapállapotba helyezi a sakkórát.
     * @param side2move A soron következő fél.
     */
    public void reset(Side side2move) {
        timer.stop();
        clockState = side2move;
        remaining[0] = format.time[0];
        remaining[1] = format.time[1];
    }

    /**
     * @return igaz, ha a sakkóra épp számlál.
     */
    public boolean isRunning() {
        return timer.isRunning();
    }

    /**
     * @return igaz, ha a sakkóra leállt, mert az egyik játékos kifutott az időből.
     */
    public boolean isTimeout() {
        return remaining[clockState.ordinal()] == 0;
    }

    /**
     * @param side A fél, akinek az idejét keressük.
     * @return A megadott fél megmaradt gondolkozási ideje.
     */
    public int getRemaining(Side side) {
        return remaining[side.ordinal()];
    }

    /**
     * @param side A fél, akinek az idejét keressük.
     * @return A megadott fél megmaradt gondolkozási ideje ms-ben.
     */
    public int getRemainingMs(Side side) {
        return getRemaining(side) * 100;
    }

    /**
     * A soron lévő játékos lépett, mostantól a másik játékos órája ketyeg.
     * Hozzáadja a többletidőt a lépő játékos idejéhez.
     * Ha az óra le volt állítva, újraindítja magát.
     */
    public void movePlayed() {
        int sIdx = clockState.ordinal();

        if(!isRunning())
            timer.start();

        switch (format.type) {
            case FISCHER -> remaining[sIdx] += format.inc[sIdx];
            case BRONSTEIN -> remaining[sIdx] += Math.min(used[sIdx], format.inc[sIdx]);
        }
        used[0] = 0;
        used[1] = 0;
        clockState = clockState.other();
    }

    private void reduceTime() {
        int sIdx = clockState.ordinal();
        ++used[sIdx];

        if(format.type != Format.IncType.DELAY || used[sIdx] > format.inc[sIdx]) {
            remaining[sIdx] = Math.max(remaining[sIdx] - 1, 0);
            tickEvent.invoke(clockState);
        }

        if(remaining[sIdx] == 0) {
            outOfTimeEvent.invoke(clockState);
            timer.stop();
        }
    }

    /**
     * @param side A fél, akinek az idejét kérjük.
     * @return hh:mm:ss.ms formátumban a megadott fél megmaradt gondolkpzási ideje.
     */
    public String getTimeStr(Side side) {
        int sIdx = side.ordinal();
        int[] ms_s_m_h = { 0, 0, 0, 0 };
        int time = remaining[sIdx];

        ms_s_m_h[0] = time % 10;
        time /= 10;

        for(int i = 1; i < ms_s_m_h.length; i++) {
            ms_s_m_h[i] = time % 60;
            time /= 60;
        }

        StringBuilder timeStr = new StringBuilder();
        boolean flag = false;

        for(int i = ms_s_m_h.length - 1; i >= 1; i--) {
            if(flag || ms_s_m_h[i] != 0 || i == 1) {
                if(flag)
                    timeStr.append(':');

                timeStr.append(flag ?
                        String.format("%02d", ms_s_m_h[i]) :
                        ms_s_m_h[i]);
                flag = true;
            }
        }
        if(remaining[sIdx] < 100) {// less than 10 sec remaining
            timeStr.append('.');
            timeStr.append(ms_s_m_h[0]);
        }

        return timeStr.toString();
    }

    @Serial
    private Object readResolve() {
        this.timer = new Timer(100, e -> reduceTime());
        return this;
    }
}
