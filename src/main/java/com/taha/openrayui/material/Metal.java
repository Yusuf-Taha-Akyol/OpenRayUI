package com.taha.openrayui.material;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.core.ScatterResult;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

public class Metal implements Material {
    private final Vec3 albedo;
    private final double fuzz; // 0 = Perfect mirror, 1 = Very fuzzy

    public Metal(Vec3 albedo, double fuzz) {
        this.albedo = albedo;
        this.fuzz = (fuzz < 1) ? fuzz : 1;
    }

    @Override
    public ScatterResult scatter(Ray rIn, HitRecord rec) {
        Vec3 reflected = reflect(rIn.direction().unitVector(), rec.normal);

        // Add fuzziness
        Ray scattered = new Ray(rec.p, reflected.add(randomInUnitSphere().mul(fuzz)));

        if (scattered.direction().dot(rec.normal) > 0) {
            return new ScatterResult(scattered, albedo);
        }
        return null;
    }

    private Vec3 reflect(Vec3 v, Vec3 n) {
        return v.sub(n.mul(2 * v.dot(n)));
    }

    private Vec3 randomInUnitSphere() {
        while (true) {
            Vec3 p = new Vec3(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1);
            if (p.lengthSquared() < 1) return p;
        }
    }
}
