package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Represents an Axis-Aligned Bounding Box (AABB).
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

    /**
     * Calculates the geometric center of the box.
     */
    public Vec3 getCenter() {
        return pMin.add(pMax).mul(0.5);
    }

    /**
     * Calculates the dimensions (Width, Height, Depth) of the box.
     */
    public Vec3 getSize() {
        return pMax.sub(pMin);
    }

    /**
     * Updates the box geometry based on a center point and size.
     * This is useful for UI manipulation (Moving or Resizing).
     * @param center The new center position.
     * @param size The new dimensions (x, y, z).
     */
    public void setTransform(Vec3 center, Vec3 size) {
        Vec3 halfSize = size.mul(0.5);
        this.pMin = center.sub(halfSize);
        this.pMax = center.add(halfSize);
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        // "Slab Method" for AABB Intersection.
        // We check intersection against X, Y, and Z planes (slabs).

        double t0 = tMin;
        double t1 = tMax;

        for (int i = 0; i < 3; i++) {
            double invD = 1.0 / r.direction().get(i);
            double tNear = (pMin.get(i) - r.origin().get(i)) * invD;
            double tFar = (pMax.get(i) - r.origin().get(i)) * invD;

            // Swap if near is actually further than far (negative direction)
            if (invD < 0.0) {
                double temp = tNear; tNear = tFar; tFar = temp;
            }

            // Narrow down the intersection interval
            t0 = tNear > t0 ? tNear : t0;
            t1 = tFar < t1 ? tFar : t1;

            // If interval is invalid, ray missed the box
            if (t1 <= t0) return false;
        }

        // Ray hit the box! Populate the HitRecord.
        rec.t = t0;
        rec.p = r.at(t0);

        // Calculate Normal vector based on which face was hit.
        // We check which coordinate is closest to the box boundaries.
        Vec3 p = rec.p;
        double epsilon = 0.0001;

        if (Math.abs(p.x - pMin.x) < epsilon) rec.normal = new Vec3(-1, 0, 0);
        else if (Math.abs(p.x - pMax.x) < epsilon) rec.normal = new Vec3(1, 0, 0);
        else if (Math.abs(p.y - pMin.y) < epsilon) rec.normal = new Vec3(0, -1, 0);
        else if (Math.abs(p.y - pMax.y) < epsilon) rec.normal = new Vec3(0, 1, 0);
        else if (Math.abs(p.z - pMin.z) < epsilon) rec.normal = new Vec3(0, 0, -1);
        else rec.normal = new Vec3(0, 0, 1);

        // Ensure normal points against the ray
        rec.setFaceNormal(r, rec.normal);
        rec.mat = material;

        return true;
    }

    @Override
    public Material getMaterial() { return material; }

    @Override
    public void setMaterial(Material m) { this.material = m; }

    @Override
    public AABB boundingBox() {
        // The box geometry itself IS an AABB
        return new AABB(pMin, pMax);
    }
}