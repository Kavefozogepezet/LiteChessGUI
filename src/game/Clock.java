package game;

import game.board.Side;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

public class Clock implements Serializable {
    public static class Format implements Serializable {
        public static final int MINUTE = 60;
        public static final int HOUR = MINUTE * 60;

        public static final Format FIDE_Blitz = new Format(3 * MINUTE, 2, IncType.FISCHER, "FIDE Blitz (3|2)");
        public static final Format FIDE_Rapid = new Format(15 * MINUTE, 10, IncType.FISCHER, "FIDE Rapid (15|10)");
        public static final Format Classical = new Format(30 * MINUTE, "Classical (30 min)");
        public static final Format Rapid = new Format(10 * MINUTE, "Rapid (10 min)");
        public static final Format Blitz = new Format(5 * MINUTE, "Blitz (5 min)");
        public static final Format Bullet = new Format(MINUTE, "Bullet (1 min)");

        public enum IncType {
            NONE, DELAY, BRONSTEIN, FISCHER
        }

        public final int[]
                time = new int[2],
                inc = new int[2];
        public final IncType type;
        private final String name;

        public Format(int wtime,int winc,int btime,int binc, IncType type, String name) {
            this.time[Side.White.ordinal()] = wtime * 10;
            this.inc[Side.White.ordinal()] = winc * 10;
            this.time[Side.Black.ordinal()] = btime * 10;
            this.inc[Side.Black.ordinal()] = binc * 10;
            this.type = type;
            this.name = name;
        }

        public Format(int time, int inc, IncType type, String name) {
            this(time, inc, time, inc, type, name);
        }

        public Format(int time, String name) {
            this(time, 0, time, 0, IncType.NONE, name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final int[] remaining = new int[2]; // remaining time in 100ms
    private final int[] used = new int[2]; // time used since last move
    private Side clockState = Side.White;
    public final Format format;
    private final Timer timer = new Timer(100, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            reduceTime();
        }
    });

    public Clock(Format format, Side side2move) {
        this.format = format;
        reset(side2move);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void reset(Side side2move) {
        timer.stop();
        clockState = side2move;
        remaining[0] = format.time[0];
        remaining[1] = format.time[1];
    }

    public boolean isRunning() {
        return timer.isRunning();
    }

    public boolean isTimeout() {
        return remaining[clockState.ordinal()] == 0;
    }

    public Timer getTimer() {
        return timer;
    }

    public int getRemaining(Side side) {
        return remaining[side.ordinal()];
    }
    public int getRemainingMs(Side side) {
        return getRemaining(side) * 100;
    }

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

        if(format.type != Format.IncType.DELAY || used[sIdx] > format.inc[sIdx])
            remaining[sIdx]--;

        if(remaining[sIdx] == 0)
            timer.stop();
    }

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
}
