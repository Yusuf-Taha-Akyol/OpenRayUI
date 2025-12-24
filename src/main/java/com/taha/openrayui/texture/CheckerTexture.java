package com.taha.openrayui.texture;

import com.taha.openrayui.math.Vec3;

/**
 * A texture that generates a checkerboard pattern.
 * UPDATED: Defaulted to use Spatial Mapping (useUV = false) to guarantee patterns on Boxes
 * even if UV coordinates are missing or zero.
 */
public class CheckerTexture implements Texture {
    private final Texture even;
    private final Texture odd;
    private final double scale;
    private final boolean useUV; // Toggle between UV and Spatial mapping

    /**
     * Constructs a CheckerTexture.
     * @param even Texture for even tiles.
     * @param odd Texture for odd tiles.
     * @param scale Controls the tile size/frequency.
     */
    public CheckerTexture(Texture even, Texture odd, double scale) {
        this.even = even;
        this.odd = odd;
        this.scale = scale;

        this.useUV = false;
    }

    /**
     * Convenience constructor for solid colors.
     */
    public CheckerTexture(Vec3 c1, Vec3 c2, double scale) {
        this(new SolidColor(c1), new SolidColor(c2), scale);
    }

    @Override
    public Vec3 value(double u, double v, Vec3 p) {
        double sines;

        if (useUV) {
            // UV-Based Mapping
            sines = Math.sin(scale * u) * Math.sin(scale * v);
        } else {
            sines = Math.sin(scale * p.x) * Math.sin(scale * p.y) * Math.sin(scale * p.z);
        }

        if (sines < 0) {
            return odd.value(u, v, p);
        } else {
            return even.value(u, v, p);
        }
    }
}