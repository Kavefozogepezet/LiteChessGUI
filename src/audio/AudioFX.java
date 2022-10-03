package audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioFX {
    public enum Name {
        MOVE, CAPTURE, GAME_END,
        FX_COUNT
    }
    private static File soundsDir = new File(System.getProperty("user.dir"), "sounds");

    private static Clip[] clips = new Clip[Name.FX_COUNT.ordinal()];

    public static void innit() {
        loadSound(Name.MOVE, "move.wav");
        loadSound(Name.CAPTURE, "capture.wav");
        loadSound(Name.GAME_END, "gameend.wav");
    }

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
