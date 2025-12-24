package com.taha.openrayui.texture;

import com.taha.openrayui.math.Vec3;

public class SolidColor implements Texture {
    private final Vec3 color;

    public SolidColor(Vec3 color) {
        this.color = color;
    }

    public SolidColor(double red, double green, double blue) {
        this(new Vec3(red, green, blue));
    }

    @Override
    public Vec3 value(double u, double v, Vec3 p) {
        return color;
    }
}
