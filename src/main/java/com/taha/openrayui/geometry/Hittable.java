package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;

import java.io.Serializable;

/**
 * Abstract base class for all objects in the scene (Sphere, Box, etc.).
 * UPDATED: Now includes a 'name' field so objects can be identified in the UI.
 */
public abstract class Hittable implements Serializable {

    // --- NEW: Name Property ---
    // Every object now has a name, defaulting to "Object" if not set.
    private String name = "Object";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --- Abstract Methods (Must be implemented by subclasses) ---

    /**
     * Determines if a ray hits this object.
     */
    public abstract boolean hit(Ray r, double tMin, double tMax, HitRecord rec);

    /**
     * Gets the material assigned to this object.
     */
    public abstract Material getMaterial();

    /**
     * Sets a new material for this object.
     */
    public abstract void setMaterial(Material m);
}