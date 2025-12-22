package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.math.Ray;

/**
 * Abstract base class for all scene objects.
 * Updated to include a 'name' field for the UI Outliner.
 */
public abstract class Hittable { // Ensure this is 'abstract class', not 'interface'

    private String name = "Object";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean hit(Ray r, double tMin, double tMax, HitRecord rec);

    // This is vital! The JList in UI uses this method to display the text.
    @Override
    public String toString() {
        return name;
    }
}