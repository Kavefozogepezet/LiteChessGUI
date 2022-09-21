package extensions;

public class MathExt {
    public int clamp(int value, int min, int max) {
        return java.lang.Math.max(min, java.lang.Math.min(value, max));
    }
}
