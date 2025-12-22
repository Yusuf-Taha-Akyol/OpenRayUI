package com.taha.openrayui.model;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.material.Dielectric;
import com.taha.openrayui.material.Lambertian;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.material.Metal;
import com.taha.openrayui.math.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the 3D world scene.
 * Manages all objects and provides access for the renderer and UI.
 */
public class Scene {
    private static Scene instance;
    private final HittableList world;

    // We keep a separate list to access specific object properties easily (for UI)
    // HittableList stores them as generic Hittables, but here we might want to cast them later.
    private final List<Hittable> objectList;

    private Scene() {
        world = new HittableList();
        objectList = new ArrayList<>();
        loadDefaultScene();
    }

    public static Scene getInstance() {
        if (instance == null) {
            instance = new Scene();
        }
        return instance;
    }

    public HittableList getWorld() {
        return world;
    }

    public List<Hittable> getObjectList() {
        return objectList;
    }

    // Adds an object to both the render world and the management list
    public void addObject(Hittable object) {
        world.add(object);
        objectList.add(object);
    }

    // Clears and reloads the default scene
    public void reset() {
        world.clear();
        objectList.clear();
        loadDefaultScene();
    }

    private void loadDefaultScene() {
        // 1. Ground (Yellowish Matte)
        Material groundMat = new Lambertian(new Vec3(0.8, 0.8, 0.0));
        addObject(new Sphere(new Vec3(0.0, -100.5, -1.0), 100.0, groundMat));

        // 2. Center (Matte Blue)
        Material centerMat = new Lambertian(new Vec3(0.1, 0.2, 0.5));
        addObject(new Sphere(new Vec3(0.0, 0.0, -1.0), 0.5, centerMat));

        // 3. Left (Glass / Dielectric)
        Material leftMat = new Dielectric(1.5);
        addObject(new Sphere(new Vec3(-1.0, 0.0, -1.0), 0.5, leftMat));

        // 4. Right (Gold / Metal)
        Material rightMat = new Metal(new Vec3(0.8, 0.6, 0.2), 0.0);
        addObject(new Sphere(new Vec3(1.0, 0.0, -1.0), 0.5, rightMat));
    }
}