package me.lcgui.app;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Audio effecteket tároló osztály.
 * Az effecteket képes lejátszani is.
 */
public class AudioFX {
    /**
     * Az elérhető effecteket az enum értékei jelölik.
     * Az effectek az értékek használatával játszhatóak le.
     */
    public enum Name {
        MOVE, CAPTURE, GAME_END,
        FX_COUNT
    }

    private static File soundsDir = new File(System.getProperty("user.dir"), "sounds");

    private static Clip[] clips = new Clip[Name.FX_COUNT.ordinal()];

    static {
        loadSound(Name.MOVE, "move.wav");
        loadSound(Name.CAPTURE, "capture.wav");
        loadSound(Name.GAME_END, "gameend.wav");
    }

    /**
     * Lejátssza a megadot effectet.
     * @param fxname Az effecthez tartozó enum érték.
     */
    public static void play(Name fxname) {
        Clip clip = clips[fxname.ordinal()];
        if(clip != null) {
            clip.setMicrosecondPosition(0);
            clip.start();
        }
    }

    private static void loadSound(Name fxname, String fileName) {
        try (
                AudioInputStream stream = AudioSystem.getAudioInputStream(new File(soundsDir, fileName))
        ) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            clips[fxname.ordinal()] = clip;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
