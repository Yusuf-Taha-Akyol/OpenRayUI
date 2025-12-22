package com.taha.openrayui.material;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.core.ScatterResult;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Represents a matte (diffuse) material.
 * It scatters light in random directions.
 */
public class Lambertian implements Material {

    // Not 'final' anymore, allowing runtime color changes
    private Vec3 albedo;

    public Lambertian(Vec3 albedo) {
        this.albedo = albedo;
    }

    // --- GETTERS & SETTERS (Required for Object Inspector) ---

    public Vec3 getAlbedo() {
        return albedo;
    }

    public void setAlbedo(Vec3 albedo) {
        this.albedo = albedo;
    }

    // --- SCATTER LOGIC ---

    @Override
    public ScatterResult scatter(Ray rIn, HitRecord rec) {
        // Scatter direction: Normal + Random unit vector
        Vec3 scatterDirection = rec.normal.add(randomInUnitSphere().unitVector());

        // Catch degenerate scatter direction (if random vector is exactly opposite to normal)
        if (nearZero(scatterDirection)) {
            scatterDirection = rec.normal;
        }

        Ray scattered = new Ray(rec.p, scatterDirection);
        return new ScatterResult(scattered, albedo);
    }

    private boolean nearZero(Vec3 v) {
        double s = 1e-8;
        return (Math.abs(v.x) < s) && (Math.abs(v.y) < s) && (Math.abs(v.z) < s);
    }

    private Vec3 randomInUnitSphere() {
        while (true) {
            Vec3 p = new Vec3(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1);
            if (p.lengthSquared() < 1) return p;
        }
    }
}
