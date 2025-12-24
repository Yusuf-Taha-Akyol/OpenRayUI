package com.taha.openrayui.core;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

import java.util.concurrent.ThreadLocalRandom; // Added for thread-safe fast random generation

/**
 * The core engine that calculates the color of rays.
 * Contains the recursive ray tracing logic.
 * OPTIMIZED: Implements Russian Roulette for early ray termination.
 */
public class Renderer {

    private final int maxDepth;

    public Renderer(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Calculates the color of a ray by tracing it through the scene.
     * Recursive function with Russian Roulette optimization.
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

        // tMin is set to 0.001 to avoid shadow acne (floating point self-intersection errors)
        if (world.hit(r, 0.001, Double.POSITIVE_INFINITY, rec)) {
            ScatterResult scattered = rec.mat.scatter(r, rec);

            if (scattered != null) {
                Vec3 attenuation = scattered.attenuation;

                // --- RUSSIAN ROULETTE OPTIMIZATION ---
                // Only apply this optimization after a certain number of bounces (e.g., allow first 5 bounces).
                // This preserves the quality of primary reflections and shadows while optimizing deep recursion.
                if (depth < maxDepth - 5) {
                    // Determine the "survival probability" based on the material's brightness (attenuation).
                    // Darker surfaces absorb more light, so rays hitting them are more likely to terminate.
                    double maxComponent = Math.max(attenuation.x, Math.max(attenuation.y, attenuation.z));
                    double survivalProbability = maxComponent;

                    // Clamp probability: Ensure at least a 5% chance of survival to avoid killing too many rays.
                    if (survivalProbability < 0.05) survivalProbability = 0.05;
                    // Probability cannot exceed 1.0 (100%)
                    if (survivalProbability > 1.0) survivalProbability = 1.0;

                    // Roll the dice: If the random value is greater than survival probability, terminate the ray.
                    if (ThreadLocalRandom.current().nextDouble() > survivalProbability) {
                        return new Vec3(0, 0, 0); // Ray dies here, returning black
                    }

                    // If the ray survives, boost its energy (normalize) to satisfy the statistical equation.
                    // This prevents "Survival Bias" which would otherwise make the image look artificially dark.
                    attenuation = attenuation.div(survivalProbability);
                }
                // -------------------------------------

                Vec3 nextBounceColor = rayColor(scattered.scattered, world, depth - 1);

                return new Vec3(
                        attenuation.x * nextBounceColor.x,
                        attenuation.y * nextBounceColor.y,
                        attenuation.z * nextBounceColor.z
                );
            }
            // Ray absorbed (scatter failed or material absorbed all light)
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
