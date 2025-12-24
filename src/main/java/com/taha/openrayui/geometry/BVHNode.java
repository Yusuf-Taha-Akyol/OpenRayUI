package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A Bounding Volume Hierarchy (BVH) node.
 * It recursively splits the objects into smaller groups (left and right children).
 * This structure allows rays to skip checking objects that are far away.
 */
public class BVHNode extends Hittable {

    private final Hittable left;
    private final Hittable right;
    private final AABB box;

    /**
     * Constructs a BVH tree from a list of objects.
     */
    public BVHNode(HittableList list) {
        this(list.objects, 0, list.objects.size());
    }

    /**
     * Recursive constructor that builds the tree structure.
     * It sorts objects along a random axis and splits them.
     */
    private BVHNode(List<Hittable> srcObjects, int start, int end) {
        // Create a modifiable copy of the list references for sorting
        List<Hittable> objects = new ArrayList<>(srcObjects);

        // 1. Choose a random axis (0=X, 1=Y, 2=Z) to split the objects
        int axis = ThreadLocalRandom.current().nextInt(3);

        // Define a comparator based on the chosen axis
        Comparator<Hittable> comparator = (a, b) -> {
            AABB boxA = a.boundingBox();
            AABB boxB = b.boundingBox();
            if (boxA == null || boxB == null) {
                System.err.println("No bounding box in BVHNode constructor.");
                return 0;
            }
            return Double.compare(boxA.min.get(axis), boxB.min.get(axis));
        };

        int objectSpan = end - start;

        if (objectSpan == 1) {
            // Leaf node: Only one object
            left = right = objects.get(start);
        } else if (objectSpan == 2) {
            // Two objects: Compare and assign
            if (comparator.compare(objects.get(start), objects.get(start + 1)) < 0) {
                left = objects.get(start);
                right = objects.get(start + 1);
            } else {
                left = objects.get(start + 1);
                right = objects.get(start);
            }
        } else {
            // More than two objects: Sort and split in half
            objects.subList(start, end).sort(comparator);

            int mid = start + objectSpan / 2;
            left = new BVHNode(objects, start, mid);
            right = new BVHNode(objects, mid, end);
        }

        // 2. Compute the bounding box for this node (surrounding both children)
        AABB boxLeft = left.boundingBox();
        AABB boxRight = right.boundingBox();

        if (boxLeft == null || boxRight == null) {
            System.err.println("No bounding box in BVHNode constructor.");
            box = null; // Should not happen with valid geometries
        } else {
            box = AABB.surroundingBox(boxLeft, boxRight);
        }
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        // Optimization: If ray misses the big box, don't check children!
        if (!box.hit(r, tMin, tMax)) {
            return false;
        }

        // Check children recursively
        boolean hitLeft = left.hit(r, tMin, tMax, rec);

        // If left hit, we only care about hits closer than the left hit (rec.t)
        // If left didn't hit, we check up to tMax
        double limit = hitLeft ? rec.t : tMax;

        boolean hitRight = right.hit(r, tMin, limit, rec);

        return hitLeft || hitRight;
    }

    @Override
    public AABB boundingBox() {
        return box;
    }

    // Nodes don't have a single material, return null
    @Override
    public Material getMaterial() { return null; }

    @Override
    public void setMaterial(Material m) { }
}
