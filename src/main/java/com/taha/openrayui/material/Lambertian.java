package com.taha.openrayui.material;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.core.ScatterResult;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

public class Lambertian implements Material {
    private final Vec3 albedo; // The color of the material

    public Lambertian(Vec3 albedo) {
        this.albedo = albedo;
    }

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
