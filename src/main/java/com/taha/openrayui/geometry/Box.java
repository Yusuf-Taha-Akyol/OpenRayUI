package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Represents an Axis-Aligned Bounding Box (AABB).
 * Uses robust face detection to map textures correctly to each of the 6 faces.
 */
public class Box extends Hittable {
    private Vec3 pMin;
    private Vec3 pMax;
    private Material material;

    public Box(Vec3 pMin, Vec3 pMax, Material material) {
        // Ensure pMin is actually smaller than pMax in all axes
        this.pMin = new Vec3(Math.min(pMin.x, pMax.x), Math.min(pMin.y, pMax.y), Math.min(pMin.z, pMax.z));
        this.pMax = new Vec3(Math.max(pMin.x, pMax.x), Math.max(pMin.y, pMax.y), Math.max(pMin.z, pMax.z));
        this.material = material;
    }

    // --- UI Helpers ---
    public Vec3 getCenter() { return pMin.add(pMax).mul(0.5); }
    public Vec3 getSize() { return pMax.sub(pMin); }
    public void setTransform(Vec3 center, Vec3 size) {
        Vec3 halfSize = size.mul(0.5);
        this.pMin = center.sub(halfSize);
        this.pMax = center.add(halfSize);
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        double t0 = tMin;
        double t1 = tMax;

        // Slab Method: Iterate over X(0), Y(1), Z(2) axes
        for (int i = 0; i < 3; i++) {
            double invD = 1.0 / r.direction().get(i);
            double tNear = (pMin.get(i) - r.origin().get(i)) * invD;
            double tFar = (pMax.get(i) - r.origin().get(i)) * invD;

            if (invD < 0.0) {
                double temp = tNear; tNear = tFar; tFar = temp;
            }

            t0 = Math.max(tNear, t0);
            t1 = Math.min(tFar, t1);

            if (t1 <= t0) return false;
        }

        rec.t = t0;
        rec.p = r.at(t0);

        // --- Determine Normal & UVs ---
        // Since it's an AABB, the hit point must be on one of the planes defined by pMin or pMax.
        // We check which coordinate matches closely.

        Vec3 p = rec.p;
        double epsilon = 1e-5;

        // Default normal
        rec.normal = new Vec3(0, 0, 1);

        // Check proximity to faces to determine the hit face
        if (Math.abs(p.x - pMin.x) < epsilon)      rec.normal = new Vec3(-1, 0, 0);
        else if (Math.abs(p.x - pMax.x) < epsilon) rec.normal = new Vec3(1, 0, 0);
        else if (Math.abs(p.y - pMin.y) < epsilon) rec.normal = new Vec3(0, -1, 0);
        else if (Math.abs(p.y - pMax.y) < epsilon) rec.normal = new Vec3(0, 1, 0);
        else if (Math.abs(p.z - pMin.z) < epsilon) rec.normal = new Vec3(0, 0, -1);
        else if (Math.abs(p.z - pMax.z) < epsilon) rec.normal = new Vec3(0, 0, 1);

        rec.setFaceNormal(r, rec.normal);

        // Calculate UV coordinates based on the hit face
        computeBoxUV(rec);

        rec.mat = material;
        return true;
    }

    /**
     * Maps the hit point on the box surface to [0,1] UV coordinates.
     */
    private void computeBoxUV(HitRecord rec) {
        Vec3 p = rec.p;
        Vec3 n = rec.normal;
        double u = 0, v = 0;

        // Normalize coordinates relative to box dimensions
        double width  = pMax.x - pMin.x;
        double height = pMax.y - pMin.y;
        double depth  = pMax.z - pMin.z;

        // If normal is parallel to X (Right/Left faces) -> Use Z and Y
        if (Math.abs(n.x) > 0.5) {
            u = (p.z - pMin.z) / depth;
            v = (p.y - pMin.y) / height;
            // Correct orientation for left face
            if (n.x < 0) u = 1.0 - u;
        }
        // If normal is parallel to Y (Top/Bottom faces) -> Use X and Z
        else if (Math.abs(n.y) > 0.5) {
            u = (p.x - pMin.x) / width;
            v = (p.z - pMin.z) / depth;
            // Correct orientation for top face
            if (n.y > 0) v = 1.0 - v;
        }
        // If normal is parallel to Z (Front/Back faces) -> Use X and Y
        else {
            u = (p.x - pMin.x) / width;
            v = (p.y - pMin.y) / height;
            // Correct orientation for front face
            if (n.z > 0) u = 1.0 - u;
        }

        rec.u = clamp(u);
        rec.v = clamp(v);
    }

    private double clamp(double x) {
        if (x < 0) return 0;
        if (x > 1) return 1;
        return x;
    }

    @Override public AABB boundingBox() { return new AABB(pMin, pMax); }
    @Override public Material getMaterial() { return material; }
    @Override public void setMaterial(Material m) { this.material = m; }
}