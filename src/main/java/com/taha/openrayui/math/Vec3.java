package com.taha.openrayui.math;

import java.io.Serializable;

/**
 * Represents a 3D vector used for coordinates (x, y, z), colors (r, g, b),
 * or directions. Immutable class for thread safety.
 */
public class Vec3 implements Serializable {
    private static final long serialVersionUID = 1L;

    public final double x;
    public final double y;
    public final double z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // --- NEW METHOD: Access by Index (Required for Box intersection loop) ---

    /**
     * Returns the component at the specified index (0=x, 1=y, 2=z).
     * Useful for algorithms iterating over axes (like AABB intersection).
     */
    public double get(int index) {
        if (index == 0) return x;
        if (index == 1) return y;
        return z;
    }

    // --- Vector Operations ---

    // Adds two vectors: (x1+x2, y1+y2, z1+z2)
    public Vec3 add(Vec3 v) {
        return new Vec3(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    // Subtracts vector v from this vector: (x1-x2, y1-y2, z1-z2)
    public Vec3 sub(Vec3 v) {
        return new Vec3(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    // Multiplies the vector by a scalar value (scaling)
    public Vec3 mul(double t) {
        return new Vec3(this.x * t, this.y * t, this.z * t);
    }

    // Divides the vector by a scalar value
    public Vec3 div(double t) {
        return mul(1.0 / t);
    }

    // --- Geometric Operations ---

    // Returns the length (magnitude) of the vector
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    // Returns the squared length (faster than length() as it avoids sqrt)
    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    // Dot product (Scalar product): x1*x2 + y1*y2 + z1*z2
    public double dot(Vec3 v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    // Cross product: Returns a vector perpendicular to both vectors
    public Vec3 cross(Vec3 v) {
        return new Vec3(
                this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x
        );
    }

    // Returns the unit vector (normalized vector with length 1)
    public Vec3 unitVector() {
        return this.div(this.length());
    }

    public static Vec3 randomUnitVector() {
        while (true) {
            Vec3 p = new Vec3(
                    Math.random() * 2 - 1,
                    Math.random() * 2 - 1,
                    Math.random() * 2 - 1
            );
            if (p.lengthSquared() < 1) return p.unitVector();
        }
    }

    @Override
    public String toString() {
        return "Vec3(" + x + ", " + y + ", " + z + ")";
    }
}
