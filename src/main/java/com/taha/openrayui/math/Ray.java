package com.taha.openrayui.math;

/**
 * Represents a ray in 3D space.
 * P(t) = origin + t * direction
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

    public Vec3 at(double t) {
        return origin.add(direction.mul(t));
    }
}
