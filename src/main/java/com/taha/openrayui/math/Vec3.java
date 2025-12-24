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

    // --- Accessor for Loop Operations ---
    public double get(int index) {
        if (index == 0) return x;
        if (index == 1) return y;
        return z;
    }

    // --- Basic Vector Arithmetic ---
    public Vec3 add(Vec3 v) {
        return new Vec3(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public Vec3 sub(Vec3 v) {
        return new Vec3(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    // Scalar Multiplication (Scaling a vector)
    public Vec3 mul(double t) {
        return new Vec3(this.x * t, this.y * t, this.z * t);
    }

    // --- CRITICAL: Hadamard Product (Color Blending) ---
    // Multiplies two vectors component-wise. Required for (Texture * Tint).
    public Vec3 mul(Vec3 v) {
        return new Vec3(this.x * v.x, this.y * v.y, this.z * v.z);
    }

    public Vec3 div(double t) {
        return mul(1.0 / t);
    }

    // --- Geometric Operations ---
    public double length() { return Math.sqrt(lengthSquared()); }
    public double lengthSquared() { return x * x + y * y + z * z; }

    public double dot(Vec3 v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vec3 cross(Vec3 v) {
        return new Vec3(
                this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x
        );
    }

    public Vec3 unitVector() {
        return this.div(this.length());
    }

    public static Vec3 randomUnitVector() {
        while (true) {
            Vec3 p = new Vec3(Math.random()*2-1, Math.random()*2-1, Math.random()*2-1);
            if (p.lengthSquared() < 1) return p.unitVector();
        }
    }

    @Override
    public String toString() { return "Vec3(" + x + ", " + y + ", " + z + ")"; }
}