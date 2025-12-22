package com.taha.openrayui.math;

/**
 * Represents a ray in 3D space defined by an origin point and a direction vector.
 * Equation: P(t) = origin + t * direction
 */
public class Ray {
    private final Vec3 origin;
    private final Vec3 direction;

    public Ray(Vec3 origin, Vec3 direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vec3 origin() {
        return origin;
    }

    public Vec3 direction() {
        return direction;
    }

    /**
     * Calculates the point at parameter t along the ray.
     * @param t The distance parameter along the ray direction
     * @return The 3D point at P(t)
     */
    public Vec3 at(double t) {
        return origin.add(direction.mul(t));
    }
}
