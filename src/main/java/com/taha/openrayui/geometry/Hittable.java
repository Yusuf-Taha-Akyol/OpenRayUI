package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.math.Ray;

/**
 * Interface for any object that can be hit by a ray.
 */
public abstract class Hittable {

    // Name for UI identification
    protected String name = "Object";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Determines if a ray hits this object within the range [tMin, tMax].
     */
    public abstract boolean hit(Ray r, double tMin, double tMax, HitRecord rec);

    @Override
    public String toString() {
        return name; // This is important for JList to display the name correctly!
    }
}
