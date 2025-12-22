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
 * Singleton class managing the 3D scene.
 * Synchronizes the data between the Ray Tracing Engine (HittableList) and the UI (DefaultListModel).
 */
public class Scene {
    private static Scene instance;

    // The list used by the renderer
    private final HittableList world;

    // The list model used by the Swing JList
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

    public DefaultListModel<Hittable> getUiListModel() {
        return uiListModel;
    }

    /**
     * Adds an object to both the render world and the UI list.
     */
    public void addObject(Hittable object) {
        world.add(object);
        uiListModel.addElement(object);
    }

    /**
     * Removes an object from both the render world and the UI list.
     * @param object The object to remove.
     */
    public void removeObject(Hittable object) {
        if (object != null) {
            world.remove(object);          // Remove from engine
            uiListModel.removeElement(object); // Remove from UI
        }
    }

    public void clear() {
        world.clear();
        uiListModel.clear();
    }

    private void loadDefaultScene() {
        // Initialize standard scene objects...
        Material groundMat = new Lambertian(new Vec3(0.8, 0.8, 0.0));
        Sphere ground = new Sphere(new Vec3(0.0, -100.5, -1.0), 100.0, groundMat);
        ground.setName("Ground (Floor)");
        addObject(ground);

        Material centerMat = new Lambertian(new Vec3(0.1, 0.2, 0.5));
        Sphere center = new Sphere(new Vec3(0.0, 0.0, -1.0), 0.5, centerMat);
        center.setName("Blue Sphere (Center)");
        addObject(center);

        Material leftMat = new Dielectric(1.5);
        Sphere left = new Sphere(new Vec3(-1.0, 0.0, -1.0), 0.5, leftMat);
        left.setName("Glass Sphere (Left)");
        addObject(left);

        Material rightMat = new Metal(new Vec3(0.8, 0.6, 0.2), 0.0);
        Sphere right = new Sphere(new Vec3(1.0, 0.0, -1.0), 0.5, rightMat);
        right.setName("Gold Sphere (Right)");
        addObject(right);
    }
}