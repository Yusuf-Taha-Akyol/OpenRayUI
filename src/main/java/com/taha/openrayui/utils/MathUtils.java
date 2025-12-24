package com.taha.openrayui.utils;

import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {

    public static final double INFINITY = Double.POSITIVE_INFINITY;
    public static final double PI = Math.PI;

    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static double randomDouble(double min, double max) {
        return min + (max - min) * randomDouble();
    }

    public static double clamp(double x, double min, double max) {
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }
}
