package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Represents a Sphere defined by a center point and radius.
 */
public class Sphere implements Hittable {
    private final Vec3 center;
    private final double radius;
    private final Material mat;

    public Sphere(Vec3 center, double radius, Material mat) {
        this.center = center;
        this.radius = radius;
        this.mat = mat;
    }

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        Vec3 oc = r.origin().sub(center);

        // Quadratic equation components: at^2 + bt + c = 0
        double a = r.direction().lengthSquared();
        double half_b = oc.dot(r.direction());
        double c = oc.lengthSquared() - radius * radius;

        double discriminant = half_b * half_b - a * c;
        if (discriminant < 0) {
            return false; // No real roots, ray misses the sphere
        }

        double sqrtd = Math.sqrt(discriminant);

        // Find the nearest root that lies in the acceptable range.
        double root = (-half_b - sqrtd) / a;
        if (root < tMin || tMax < root) {
            root = (-half_b + sqrtd) / a;
            if (root < tMin || tMax < root) {
                return false;
            }
        }

        // Record hit details
        rec.t = root;
        rec.p = r.at(rec.t);

        // Calculate outward normal: (point - center) / radius
        Vec3 outwardNormal = (rec.p.sub(center)).div(radius);
        rec.setFaceNormal(r, outwardNormal);

        rec.mat = this.mat; // Don't forget to assign the material!

        return true;
    }
}
