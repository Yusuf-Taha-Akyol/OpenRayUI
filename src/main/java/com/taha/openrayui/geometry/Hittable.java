package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.math.Ray;

/**
 * Interface for any object that can be hit by a ray.
 */
public interface Hittable {
    /**
     * Determines if a ray hits this object within the range [tMin, tMax].
     *
     * @param r The ray to test
     * @param tMin The minimum distance to consider a hit
     * @param tMax The maximum distance to consider a hit
     * @param rec The HitRecord to populate with hit details if a hit occurs
     * @return true if the ray hits the object, false otherwise
     */
    boolean hit(Ray r, double tMin, double tMax, HitRecord rec);
}
