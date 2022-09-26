package game;

import game.board.Side;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Clock {
    public static class Format {
        public enum IncType {
            NONE, DELAY, BRONSTEIN, FISCHER
        }

        public final int[]
                time = new int[2],
                inc = new int[2];
        public final IncType type;

        public Format(int wtime,int winc,int btime,int binc, IncType type) {
            this.time[Side.White.ordinal()] = wtime * 10;
            this.inc[Side.White.ordinal()] = winc * 10;
            this.time[Side.Black.ordinal()] = btime * 10;
            this.inc[Side.Black.ordinal()] = binc * 10;
            this.type = type;
        }

        public Format(int wtime, int btime) {
            this.time[Side.White.ordinal()] = wtime * 10;
            this.time[Side.Black.ordinal()] = btime * 10;
            this.type = IncType.NONE;
        }
    }

    private final JLabel[] timePanels = new JLabel[2];
    private final int[] remaining = new int[2]; // remaining time in 100ms
    private final int[] used = new int[2]; // time used since last move
    private Side clockState = Side.White;
    private final Format format;
    private Timer timer;

    public Clock(JLabel timeWhite, JLabel timeBlack, Side side2move, Format format) {
        timePanels[0] = timeWhite;
        timePanels[1] = timeBlack;
        this.format = format;

        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reduceTime();
            }
        });
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

        displayTime();
        clockState = clockState.other();
        displayTime();
        clockState = clockState.other();
    }

    public boolean isRunning() {
        return timer.isRunning();
    }

    public boolean isTimeout() {
        return remaining[clockState.ordinal()] == 0;
    }

    public void movePlayed() {
        int sIdx = clockState.ordinal();

        switch (format.type) {
            case FISCHER -> remaining[sIdx] += format.inc[sIdx];
            case BRONSTEIN -> remaining[sIdx] += Math.min(used[sIdx], format.inc[sIdx]);
        }
        used[0] = 0;
        used[1] = 0;

        displayTime();
        clockState = clockState.other();
    }

    private void reduceTime() {
        int sIdx = clockState.ordinal();
        ++used[sIdx];

        if(format.type != Format.IncType.DELAY || used[sIdx] > format.inc[sIdx])
            remaining[sIdx]--;

        displayTime();
    }

    private void displayTime() {
        int sIdx = clockState.ordinal();
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

        timePanels[sIdx].setText(timeStr.toString());
    }
}
