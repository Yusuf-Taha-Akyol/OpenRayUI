package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Represents a 3D Sphere defined by a center point and a radius.
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
            if (root < tMin || tMax < root)
                return false;
        }

        rec.t = root;
        rec.p = r.at(rec.t);
        Vec3 outwardNormal = (rec.p.sub(center)).div(radius);
        rec.setFaceNormal(r, outwardNormal);
        rec.mat = material;

        return true;
    }

    public Vec3 getCenter() { return center; }
    public void setCenter(Vec3 center) { this.center = center; }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    // --- New Methods from Hittable ---

    @Override
    public Material getMaterial() { return material; }

    @Override
    public void setMaterial(Material m) { this.material = m; }

    @Override
    public AABB boundingBox() {
        // Box corners: center - radius, center + radius
        return new AABB(
                center.sub(new Vec3(radius, radius, radius)),
                center.add(new Vec3(radius, radius, radius))
        );
    }

    /**
     * Calculates the 2D map coordinates (u, v) of a point (p) on the sphere's surface.
     * p: Unit vector from the sphere's centre to the surface.
     */
    private static void getSphereUV(Vec3 p, HitRecord rec) {
        // p: a given point on the sphere of radius one, centered at the origin.
        // u: returned value [0,1] of angle around the Y axis from X=-1.
        // v: returned value [0,1] of angle from Y=-1 to Y=+1.
        //     <1 0 0> yields <0.50 0.50>       <-1  0  0> yields <0.00 0.50>
        //     <0 1 0> yields <0.50 1.00>       < 0 -1  0> yields <0.50 0.00>
        //     <0 0 1> yields <0.25 0.50>       < 0  0 -1> yields <0.75 0.50>

        double theta = Math.acos(-p.y);
        double phi = Math.atan2(-p.z, p.x) + Math.PI;

        rec.u = phi / (2 * Math.PI);
        rec.v = theta / Math.PI;
    }
}
