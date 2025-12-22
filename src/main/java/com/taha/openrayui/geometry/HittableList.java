package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.math.Ray;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of hittable objects (the world).
 * Manages the collection of objects that the ray tracer interacts with.
 */
public class HittableList extends Hittable {

    // Internal list to store objects
    private final List<Hittable> objects = new ArrayList<>();

    public HittableList() {}

    public HittableList(Hittable object) {
        add(object);
    }

    public void clear() {
        objects.clear();
    }

    /**
     * Adds an object to the scene.
     */
    public void add(Hittable object) {
        objects.add(object);
    }

    /**
     * Removes an object from the scene.
     * Essential for the delete functionality in the UI.
     */
    public void remove(Hittable object) {
        objects.remove(object);
    }

    public int size() {
        return objects.size();
    }

    public Hittable get(int index) {
        return objects.get(index);
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        HitRecord tempRec = new HitRecord();
        boolean hitAnything = false;
        double closestSoFar = tMax;

        for (Hittable object : objects) {
            if (object.hit(r, tMin, closestSoFar, tempRec)) {
                hitAnything = true;
                closestSoFar = tempRec.t;
                rec.copyFrom(tempRec);
            }
        }

        return hitAnything;
    }
}
