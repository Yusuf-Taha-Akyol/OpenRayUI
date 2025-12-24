package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Represents a Sphere primitive.
 * Implements standard spherical UV mapping for correct texture wrapping.
 */
public class Sphere extends Hittable {
    private Vec3 center;
    private double radius;
    private Material material;

    public Sphere(Vec3 center, double radius, Material material) {
        this.center = center;
        this.radius = radius;
        this.material = material;
    }

    // --- Getters & Setters for UI ---
    public Vec3 getCenter() { return center; }
    public void setCenter(Vec3 center) { this.center = center; }
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        Vec3 oc = r.origin().sub(center);
        double a = r.direction().lengthSquared();
        double half_b = oc.dot(r.direction());
        double c = oc.lengthSquared() - radius * radius;
        double discriminant = half_b * half_b - a * c;

        if (discriminant < 0) return false;
        double sqrtd = Math.sqrt(discriminant);

        // Find the nearest root that lies in the acceptable range.
        double root = (-half_b - sqrtd) / a;
        if (root < tMin || tMax < root) {
            root = (-half_b + sqrtd) / a;
            if (root < tMin || tMax < root) return false;
        }

        rec.t = root;
        rec.p = r.at(rec.t);

        // Calculate outward normal (normalized)
        Vec3 outwardNormal = (rec.p.sub(center)).div(radius);
        rec.setFaceNormal(r, outwardNormal);

        // --- UV Mapping ---
        getSphereUV(outwardNormal, rec);

        rec.mat = material;
        return true;
    }

    /**
     * Calculates UV coordinates for a point on the unit sphere.
     * @param p The point on the sphere (must be a unit vector relative to center).
     * @param rec The HitRecord to store u and v.
     */
    private static void getSphereUV(Vec3 p, HitRecord rec) {
        // phi: angle around the Y axis (-PI to +PI)
        // theta: angle from Y=-1 to Y=+1 (0 to PI)
        double theta = Math.acos(-p.y);
        double phi = Math.atan2(-p.z, p.x) + Math.PI;

        // Normalize to [0, 1] range
        rec.u = phi / (2 * Math.PI);
        rec.v = theta / Math.PI;
    }

    @Override
    public AABB boundingBox() {
        return new AABB(
                center.sub(new Vec3(radius, radius, radius)),
                center.add(new Vec3(radius, radius, radius))
        );
    }

    @Override public Material getMaterial() { return material; }
    @Override public void setMaterial(Material m) { this.material = m; }
}
