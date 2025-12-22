package com.taha.openrayui.model;

import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.material.Dielectric;
import com.taha.openrayui.material.Lambertian;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.material.Metal;
import com.taha.openrayui.math.Vec3;

public class Scene {

    public static HittableList createWorld() {
        HittableList world = new HittableList();

        // 1. ZEMİN (Sarımsı Mat)
        Material groundMat = new Lambertian(new Vec3(0.8, 0.8, 0.0));
        world.add(new Sphere(new Vec3(0.0, -100.5, -1.0), 100.0, groundMat));

        // 2. ORTA (Mat Lacivert)
        Material centerMat = new Lambertian(new Vec3(0.1, 0.2, 0.5));
        world.add(new Sphere(new Vec3(0.0, 0.0, -1.0), 0.5, centerMat));

        // 3. SOL (CAM / Dielectric) - Kırılma indisi 1.5
        Material leftMat = new Dielectric(1.5);
        world.add(new Sphere(new Vec3(-1.0, 0.0, -1.0), 0.5, leftMat));

        // (Opsiyonel) Camın içine hava kabarcığı efekti için (Negatif yarıçap)
        // world.add(new Sphere(new Vec3(-1.0, 0.0, -1.0), -0.4, leftMat));

        // 4. SAĞ (ALTIN / Metal)
        Material rightMat = new Metal(new Vec3(0.8, 0.6, 0.2), 0.0);
        world.add(new Sphere(new Vec3(1.0, 0.0, -1.0), 0.5, rightMat));

        return world;
    }
}
