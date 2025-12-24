package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Represents an Axis-Aligned Bounding Box (AABB) primitive.
 * Defined by two points: Minimum corner (pMin) and Maximum corner (pMax).
 */
public class Box extends Hittable {
    private Vec3 pMin; // Bottom-Left-Back corner
    private Vec3 pMax; // Top-Right-Front corner
    private Material material;

    public Box(Vec3 pMin, Vec3 pMax, Material material) {
        this.pMin = pMin;
        this.pMax = pMax;
        this.material = material;
    }

    public Vec3 getCenter() {
        return pMin.add(pMax).mul(0.5);
    }

    public Vec3 getSize() {
        return pMax.sub(pMin);
    }

    /**
     * Updates the box geometry based on a center point and size.
     * Useful for UI manipulation (Gizmo).
     */
    public void setTransform(Vec3 center, Vec3 size) {
        Vec3 halfSize = size.mul(0.5);
        this.pMin = center.sub(halfSize);
        this.pMax = center.add(halfSize);
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        // "Slab Method" for AABB Intersection.
        // Checks intersection against X, Y, and Z planes.

        double t0 = tMin;
        double t1 = tMax;

        int hitAxis = -1; // 0:x, 1:y, 2:z

        for (int i = 0; i < 3; i++) {
            double invD = 1.0 / r.direction().get(i);
            double tNear = (pMin.get(i) - r.origin().get(i)) * invD;
            double tFar = (pMax.get(i) - r.origin().get(i)) * invD;

            if (invD < 0.0) {
                double temp = tNear; tNear = tFar; tFar = temp;
            }

            // When updating t0, if we find a new tNear, that's the new surface we're hitting.
            if (tNear > t0) {
                t0 = tNear;
                hitAxis = i; // The collision occurred on this axis.
            }

            t1 = tFar < t1 ? tFar : t1;

            if (t1 <= t0) return false;
        }

        if (hitAxis == -1) {
            // If the ray is inside or on the border of the box, we still need to assign a normal.
            // Let's simply find the nearest surface (Old method, but only as a fallback)
            Vec3 p = r.at(t0);
            if (Math.abs(p.x - pMin.x) < 0.001 || Math.abs(p.x - pMax.x) < 0.001) hitAxis = 0;
            else if (Math.abs(p.y - pMin.y) < 0.001 || Math.abs(p.y - pMax.y) < 0.001) hitAxis = 1;
            else hitAxis = 2;
        }

        rec.t = t0;
        rec.p = r.at(t0);

        // Determine the normal vector based on which face was hit.
        Vec3 p = rec.p;
        double epsilon = 0.0001;

        if (Math.abs(p.x - pMin.x) < epsilon) rec.normal = new Vec3(-1, 0, 0);
        else if (Math.abs(p.x - pMax.x) < epsilon) rec.normal = new Vec3(1, 0, 0);
        else if (Math.abs(p.y - pMin.y) < epsilon) rec.normal = new Vec3(0, -1, 0);
        else if (Math.abs(p.y - pMax.y) < epsilon) rec.normal = new Vec3(0, 1, 0);
        else if (Math.abs(p.z - pMin.z) < epsilon) rec.normal = new Vec3(0, 0, -1);
        else rec.normal = new Vec3(0, 0, 1);

        rec.setFaceNormal(r, rec.normal);

        // --- UV Coordinates Calculation for Box ---
        // Calculates normalized (0-1) coordinates based on the hit face.
        double width = pMax.x - pMin.x;
        double height = pMax.y - pMin.y;
        double depth = pMax.z - pMin.z;

        double localX = (rec.p.x - pMin.x) / width;
        double localY = (rec.p.y - pMin.y) / height;
        double localZ = (rec.p.z - pMin.z) / depth;

        // Planar mapping based on the normal direction
        if (Math.abs(rec.normal.x) > 0.9) {
            // Side faces (YZ plane)
            rec.u = localZ; rec.v = localY;
        } else if (Math.abs(rec.normal.y) > 0.9) {
            // Top/Bottom faces (XZ plane)
            rec.u = localX; rec.v = localZ;
        } else {
            // Front/Back faces (XY plane)
            rec.u = localX; rec.v = localY;
        }

        rec.mat = material;
        return true;
    }

    @Override
    public AABB boundingBox() {
        return new AABB(pMin, pMax);
    }

    @Override
    public Material getMaterial() { return material; }

    @Override
    public void setMaterial(Material m) { this.material = m; }
}