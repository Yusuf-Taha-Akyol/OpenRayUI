package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of hittable objects (e.g., the entire scene world).
 * It delegates the ray intersection test to all objects in the list.
 */
public class HittableList extends Hittable {

    // The list of objects in the scene
    public final List<Hittable> objects = new ArrayList<>();

    public HittableList() {}

    public HittableList(Hittable object) {
        add(object);
    }

    public void clear() {
        objects.clear();
    }

    public void add(Hittable object) {
        objects.add(object);
    }

    public void remove(Hittable object) {
        objects.remove(object);
    }

    public Hittable get(int index) {
        return objects.get(index);
    }

    public int size() {
        return objects.size();
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        HitRecord tempRec = new HitRecord();
        boolean hitAnything = false;
        double closestSoFar = tMax;

        // Iterate through all objects to find the closest hit
        for (Hittable object : objects) {
            if (object.hit(r, tMin, closestSoFar, tempRec)) {
                hitAnything = true;
                closestSoFar = tempRec.t;

                // Update the main hit record with the closer hit details
                rec.t = tempRec.t;
                rec.p = tempRec.p;
                rec.normal = tempRec.normal;
                rec.mat = tempRec.mat;
                rec.frontFace = tempRec.frontFace;

                // FIX: Propagate UV coordinates from the geometry to the main record
                rec.u = tempRec.u;
                rec.v = tempRec.v;
            }
        }

        return hitAnything;
    }

    @Override
    public Material getMaterial() {
        return null;
    }

    @Override
    public void setMaterial(Material m) {
        // Intentionally empty.
    }

    @Override
    public AABB boundingBox() {
        if (objects.isEmpty()) return null;

        AABB outputBox = null;
        boolean firstBox = true;

        for (Hittable object : objects) {
            AABB tempBox = object.boundingBox();

            if (tempBox == null) return null;

            if (firstBox) {
                outputBox = tempBox;
                firstBox = false;
            } else {
                outputBox = AABB.surroundingBox(outputBox, tempBox);
            }
        }

        return outputBox;
    }
}