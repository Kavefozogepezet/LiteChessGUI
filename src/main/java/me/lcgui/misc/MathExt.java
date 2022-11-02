package me.lcgui.misc;

public class MathExt {
    public static int clamp(int value, int min, int max) {
        return java.lang.Math.max(min, java.lang.Math.min(value, max));
    }
}
