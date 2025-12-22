package com.taha.openrayui.core;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * The core engine that calculates the color of rays.
 * Contains the recursive ray tracing logic.
 */
public class Renderer {

    private final int maxDepth;

    public Renderer(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Calculates the color of a ray by tracing it through the scene.
     * Recursive function.
     *
     * @param r     The ray to trace
     * @param world The scene (list of objects)
     * @param depth Current recursion depth (remaining bounces)
     * @return The calculated color as a Vec3
     */
    public Vec3 rayColor(Ray r, Hittable world, int depth) {
        HitRecord rec = new HitRecord();

        // If we've exceeded the ray bounce limit, no more light is gathered.
        if (depth <= 0) {
            return new Vec3(0, 0, 0);
        }

        // tMin is 0.001 to avoid shadow acne (floating point errors)
        if (world.hit(r, 0.001, Double.POSITIVE_INFINITY, rec)) {
            ScatterResult scattered = rec.mat.scatter(r, rec);

            if (scattered != null) {
                // Ray scattered (bounced). Combine attenuation (color) with the next bounce's color.
                Vec3 attenuation = scattered.attenuation;
                Vec3 nextBounceColor = rayColor(scattered.scattered, world, depth - 1);

                return new Vec3(
                        attenuation.x * nextBounceColor.x,
                        attenuation.y * nextBounceColor.y,
                        attenuation.z * nextBounceColor.z
                );
            }
            // Ray absorbed (became black)
            return new Vec3(0, 0, 0);
        }

        // --- Background (Sky) ---
        Vec3 unitDirection = r.direction().unitVector();
        double t = 0.5 * (unitDirection.y + 1.0);
        Vec3 white = new Vec3(1.0, 1.0, 1.0);
        Vec3 blue = new Vec3(0.5, 0.7, 1.0);

        // Linear interpolation (lerp) between white and blue based on Y coordinate
        return white.mul(1.0 - t).add(blue.mul(t));
    }
}
