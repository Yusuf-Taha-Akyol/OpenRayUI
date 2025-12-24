package com.taha.openrayui.material;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.core.ScatterResult;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.texture.SolidColor;
import com.taha.openrayui.texture.Texture;

/**
 * Represents a matte (diffuse) material.
 * It scatters light in random directions.
 */
public class Lambertian implements Material {

    private Texture albedo;

    public Lambertian(Vec3 color) {
        this.albedo = new SolidColor(color);
    }

    public Lambertian(Texture texture) {
        this.albedo = texture;
    }

    // --- GETTERS & SETTERS (Required for Object Inspector) ---

    public Vec3 getAlbedoColor() {
        return albedo.value(0,0, new Vec3(0,0,0));
    }

    public void setAlbedo(Vec3 color) {
        this.albedo = new SolidColor(color);
    }

    public void setAlbedoColor(Texture texture) {
        this.albedo = texture;
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

        Vec3 attenuation = albedo.value(rec.u, rec.v, rec.p);

        return new ScatterResult(scattered, attenuation);
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
