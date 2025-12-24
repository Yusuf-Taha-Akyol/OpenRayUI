package com.taha.openrayui.texture;

import com.taha.openrayui.math.Vec3;

import java.io.Serializable;

/**
 * Interface for textures.
 * A texture determines the color of a surface at a specific point (u, v).
 */
public interface Texture extends Serializable {
    /**
     * @param u Texture coordinate U
     * @param v Texture coordinate V
     * @param p The hit point in 3D space (useful for solid textures like Perlin noise)
     * @return The color at this coordinate
     */
    Vec3 value(double u, double v, Vec3 p);
}