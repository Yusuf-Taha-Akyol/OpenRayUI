package com.taha.openrayui.geometry;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Represents a sphere object in the 3D scene.
 * Updated with getters and setters to allow dynamic editing via the UI.
 */
public class Sphere extends Hittable {

    // Fields are no longer 'final' so they can be modified at runtime
    private Vec3 center;
    private double radius;
    private Material mat;

    public Sphere(Vec3 center, double radius, Material mat) {
        this.center = center;
        this.radius = radius;
        this.mat = mat;
        // Set a default name if none is provided
        setName("Sphere");
    }

    // --- GETTERS & SETTERS (NEW) ---
    // These methods allow the Object Inspector to read and modify sphere properties.

    public Vec3 getCenter() { return center; }

    public void setCenter(Vec3 center) { this.center = center; }

    public double getRadius() { return radius; }

    public void setRadius(double radius) { this.radius = radius; }

    public Material getMaterial() { return mat; }

    public void setMaterial(Material mat) { this.mat = mat; }

    // --- RAY TRACING LOGIC ---

    @Override
    public boolean hit(Ray r, double tMin, double tMax, HitRecord rec) {
        Vec3 oc = r.origin().sub(center);
        double a = r.direction().lengthSquared();
        double half_b = oc.dot(r.direction());
        double c = oc.lengthSquared() - radius * radius;
        double discriminant = half_b * half_b - a * c;

        if (discriminant > 0) {
            double root = Math.sqrt(discriminant);
            double temp = (-half_b - root) / a;
            if (temp < tMax && temp > tMin) {
                rec.t = temp;
                rec.p = r.at(rec.t);
                Vec3 outwardNormal = (rec.p.sub(center)).div(radius);
                rec.setFaceNormal(r, outwardNormal);
                rec.mat = mat;
                return true;
            }
            temp = (-half_b + root) / a;
            if (temp < tMax && temp > tMin) {
                rec.t = temp;
                rec.p = r.at(rec.t);
                Vec3 outwardNormal = (rec.p.sub(center)).div(radius);
                rec.setFaceNormal(r, outwardNormal);
                rec.mat = mat;
                return true;
            }
        }
        return false;
    }
}
