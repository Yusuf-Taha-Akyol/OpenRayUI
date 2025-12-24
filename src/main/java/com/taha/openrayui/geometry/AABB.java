package com.taha.openrayui.geometry;

import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Axis-Aligned Bounding Box (AABB).
 * Represents a rectangular region in space defined by min/max points.
 * Used for optimization (BVH) to quickly discard rays that miss the object entirely.
 */
public class AABB {
    public final Vec3 min;
    public final Vec3 max;

    public AABB(Vec3 min, Vec3 max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Optimized Slab Method to check if a ray hits this box.
     * We don't need the hit point or normal, just true/false.
     */
    public boolean hit(Ray r, double tMin, double tMax) {
        for (int a = 0; a < 3; a++) {
            // Get ray component (x, y, z) using index access
            double invD = 1.0 / r.direction().get(a);
            double t0 = (min.get(a) - r.origin().get(a)) * invD;
            double t1 = (max.get(a) - r.origin().get(a)) * invD;

            // Swap if t0 > t1 (handling negative direction)
            if (invD < 0.0) {
                double temp = t0; t0 = t1; t1 = temp;
            }

            // Narrow the search interval
            tMin = t0 > tMin ? t0 : tMin;
            tMax = t1 < tMax ? t1 : tMax;

            // If interval is empty, ray missed
            if (tMax <= tMin) return false;
        }
        return true;
    }

    /**
     * Creates a new AABB that encloses two other boxes.
     * Used to build the BVH tree hierarchy.
     */
    public static AABB surroundingBox(AABB box0, AABB box1) {
        Vec3 small = new Vec3(
                Math.min(box0.min.x, box1.min.x),
                Math.min(box0.min.y, box1.min.y),
                Math.min(box0.min.z, box1.min.z));

        Vec3 big = new Vec3(
                Math.max(box0.max.x, box1.max.x),
                Math.max(box0.max.y, box1.max.y),
                Math.max(box0.max.z, box1.max.z));

        return new AABB(small, big);
    }
}
