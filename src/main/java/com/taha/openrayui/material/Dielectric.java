package com.taha.openrayui.material;

import com.taha.openrayui.core.HitRecord;
import com.taha.openrayui.core.ScatterResult;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;

/**
 * Represents a transparent material (glass, water, diamond).
 */
public class Dielectric implements Material {

    private double ir; // Index of Refraction (e.g., 1.5 for glass)

    public Dielectric(double indexOfRefraction) {
        this.ir = indexOfRefraction;
    }

    // --- GETTER & SETTER ---

    public double getIr() { return ir; }

    public void setIr(double ir) { this.ir = ir; }

    @Override
    public ScatterResult scatter(Ray rIn, HitRecord rec) {
        Vec3 attenuation = new Vec3(1.0, 1.0, 1.0); // Glass absorbs nothing (usually)
        double refractionRatio = rec.frontFace ? (1.0 / ir) : ir;

        Vec3 unitDirection = rIn.direction().unitVector();
        double cosTheta = Math.min(unitDirection.mul(-1).dot(rec.normal), 1.0);
        double sinTheta = Math.sqrt(1.0 - cosTheta * cosTheta);

        boolean cannotRefract = refractionRatio * sinTheta > 1.0;
        Vec3 direction;

        // Determine if ray reflects or refracts (Fresnel effect & Snell's Law)
        if (cannotRefract || reflectance(cosTheta, refractionRatio) > Math.random()) {
            direction = reflect(unitDirection, rec.normal);
        } else {
            direction = refract(unitDirection, rec.normal, refractionRatio);
        }

        return new ScatterResult(new Ray(rec.p, direction), attenuation);
    }

    private Vec3 refract(Vec3 uv, Vec3 n, double etai_over_etat) {
        double cosTheta = Math.min(uv.mul(-1).dot(n), 1.0);
        Vec3 rOutPerp = uv.add(n.mul(cosTheta)).mul(etai_over_etat);
        double rOutParallelVal = -Math.sqrt(Math.abs(1.0 - rOutPerp.lengthSquared()));
        return rOutPerp.add(n.mul(rOutParallelVal));
    }

    private Vec3 reflect(Vec3 v, Vec3 n) {
        return v.sub(n.mul(2 * v.dot(n)));
    }

    private double reflectance(double cosine, double refIdx) {
        // Schlick's approximation for reflectance
        double r0 = (1 - refIdx) / (1 + refIdx);
        r0 = r0 * r0;
        return r0 + (1 - r0) * Math.pow((1 - cosine), 5);
    }
}
