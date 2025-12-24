package com.taha.openrayui.texture;

import com.taha.openrayui.math.Vec3;

/**
 * A procedural texture that generates a checkerboard pattern.
 * FIXED: Reverted to pure Spatial Mapping (using 3D position 'p').
 * This guarantees the pattern appears on ALL objects (Box, Sphere) regardless of UV mapping issues.
 */
public class CheckerTexture implements Texture {
    private final Texture even;
    private final Texture odd;
    private final double scale;

    public CheckerTexture(Texture even, Texture odd, double scale) {
        this.even = even;
        this.odd = odd;
        this.scale = scale;
    }

    public CheckerTexture(Vec3 c1, Vec3 c2, double scale) {
        this(new SolidColor(c1), new SolidColor(c2), scale);
    }

    @Override
    public Vec3 value(double u, double v, Vec3 p) {
        // SPATIAL MAPPING (The reliable way)
        // Uses the actual 3D hit point (p.x, p.y, p.z) to generate the pattern.
        // Even if UVs are 0.0 or broken, this WILL generate a pattern.
        double sines = Math.sin(scale * p.x) * Math.sin(scale * p.y) * Math.sin(scale * p.z);

        if (sines < 0) {
            return odd.value(u, v, p);
        } else {
            return even.value(u, v, p);
        }
    }
}