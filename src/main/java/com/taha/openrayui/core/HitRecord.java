package com.taha.openrayui.core;

import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.material.Material; // Birazdan oluşturacağız

/**
 * A structure to store details about a ray-object intersection.
 * It holds the hit point, surface normal, ray parameter t, and material.
 */
public class HitRecord {
    public Vec3 p;         // Intersection point
    public Vec3 normal;    // Surface normal at the intersection
    public double t;       // Ray parameter t where P(t) = origin + t*direction
    public boolean frontFace; // True if ray hit the front face, false if inside
    public Material mat;   // The material of the object hit

    /**
     * Sets the hit record normal vector.
     * Ensures the normal always points against the incident ray.
     *
     * @param r The incident ray
     * @param outwardNormal The geometric normal pointing out of the surface
     */
    public void setFaceNormal(Ray r, Vec3 outwardNormal) {
        // If ray and normal face the same direction, ray is inside the object.
        frontFace = r.direction().dot(outwardNormal) < 0;
        normal = frontFace ? outwardNormal : outwardNormal.mul(-1);
    }
}
