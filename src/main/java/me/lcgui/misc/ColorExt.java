package me.lcgui.misc;

import java.awt.*;

public class ColorExt {
    public static final Color transparent = new Color(0, 0, 0, 0);

    public static Color blend(Color c1, Color c2) {
        double totalAlpha = c1.getAlpha() + c2.getAlpha();
        double weight1 = c1.getAlpha() / totalAlpha;
        double weight2 = c2.getAlpha() / totalAlpha;

        double r = weight1 * c1.getRed() + weight2 * c2.getRed();
        double g = weight1 * c1.getGreen() + weight2 * c2.getGreen();
        double b = weight1 * c1.getBlue() + weight2 * c2.getBlue();
        double a = Math.max(c1.getAlpha(), c2.getAlpha());

        return new Color((int) r, (int) g, (int) b, (int) a);
    }

    public static Color overlay(Color c1, Color c2) {
        double alpha1 = c1.getAlpha() / 255d;
        double alpha2 = c2.getAlpha() / 255d;
        double alpha = alpha1 + alpha2 * (1d - alpha1);

        double r = (c2.getRed() * alpha2 + c1.getRed() * alpha1 * (1d - alpha2)) / alpha;
        double g = (c2.getGreen() * alpha2 + c1.getGreen() * alpha1 * (1d - alpha2)) / alpha;
        double b = (c2.getBlue() * alpha2 + c1.getBlue() * alpha1 * (1d - alpha2)) / alpha;

        return new Color((int) r, (int) g, (int) b, (int) alpha * 255);
    }
}
