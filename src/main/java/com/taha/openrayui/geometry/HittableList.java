package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.math.Ray;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of Hittable objects (e.g., spheres).
 * Checks intersections against all objects in the list and returns the closest hit.
 */
public class HittableList implements Hittable {
    public final List<Hittable> objects = new ArrayList<>();

    public HittableList() {}

    public HittableList(Hittable object) {
        add(object);
    }

    public void add(Hittable object) {
        objects.add(object);
    }

    public void clear() {
        objects.clear();
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        HitRecord tempRec = new HitRecord();
        boolean hitAnything = false;
        double closestSoFar = tMax;

        for (Hittable object : objects) {
            // Check each object. Only update if we hit something CLOSER than before.
            if (object.hit(r, tMin, closestSoFar, tempRec)) {
                hitAnything = true;
                closestSoFar = tempRec.t;

                // Copy details to the main record
                rec.p = tempRec.p;
                rec.normal = tempRec.normal;
                rec.t = tempRec.t;
                rec.frontFace = tempRec.frontFace;
                rec.mat = tempRec.mat;
            }
        }

        return hitAnything;
    }
}
