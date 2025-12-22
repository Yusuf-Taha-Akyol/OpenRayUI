package com.taha.openrayui.model;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.material.Dielectric;
import com.taha.openrayui.material.Lambertian;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.material.Metal;
import com.taha.openrayui.math.Vec3;

import javax.swing.*;

/**
 * Manages the 3D world and provides data for both the Renderer and the UI Outliner.
 */
public class Scene {
    private static Scene instance;

    // The list used by the Ray Tracing engine (optimized for rendering)
    private final HittableList world;

    // The list model used by the Swing UI (optimized for JList)
    private final DefaultListModel<Hittable> uiListModel;

    private Scene() {
        world = new HittableList();
        uiListModel = new DefaultListModel<>();
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

    // New method for UI to access the list data
    public DefaultListModel<Hittable> getUiListModel() {
        return uiListModel;
    }

    // Unified method to add objects to both the engine and the UI
    public void addObject(Hittable object) {
        world.add(object);
        uiListModel.addElement(object);
    }

    public void clear() {
        world.clear();
        uiListModel.clear();
    }

    private void loadDefaultScene() {
        // 1. Ground
        Material groundMat = new Lambertian(new Vec3(0.8, 0.8, 0.0));
        Sphere ground = new Sphere(new Vec3(0.0, -100.5, -1.0), 100.0, groundMat);
        ground.setName("Ground (Floor)");
        addObject(ground);

        // 2. Center Sphere
        Material centerMat = new Lambertian(new Vec3(0.1, 0.2, 0.5));
        Sphere center = new Sphere(new Vec3(0.0, 0.0, -1.0), 0.5, centerMat);
        center.setName("Blue Sphere (Center)");
        addObject(center);

        // 3. Left Sphere (Glass)
        Material leftMat = new Dielectric(1.5);
        Sphere left = new Sphere(new Vec3(-1.0, 0.0, -1.0), 0.5, leftMat);
        left.setName("Glass Sphere (Left)");
        addObject(left);

        // 4. Right Sphere (Metal)
        Material rightMat = new Metal(new Vec3(0.8, 0.6, 0.2), 0.0);
        Sphere right = new Sphere(new Vec3(1.0, 0.0, -1.0), 0.5, rightMat);
        right.setName("Gold Sphere (Right)");
        addObject(right);
    }
}