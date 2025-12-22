package com.taha.openrayui.io;

import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.model.Scene;

import java.io.*;

/**
 * Handles the serialization and deserialization of the scene data.
 * Responsible for saving/loading .ray project files.
 */
public class SceneSerializer {

    /**
     * Saves the current scene objects to a file.
     * @param file The destination file.
     * @throws IOException If saving fails.
     */
    public static void save(File file) throws IOException {
        if (!file.getName().endsWith(".ray")) {
            file = new File(file.getAbsolutePath() + ".ray");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(Scene.getInstance().getWorld());
        }
    }

    /**
     * Loads a scene from a file and updates the Scene singleton.
     * @param file The source file.
     * @throws IOException If reading fails.
     * @throws ClassNotFoundException If the file format is invalid.
     */
    public static void load(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            HittableList loadedWorld = (HittableList) ois.readObject();
            Scene.getInstance().loadSceneFromList(loadedWorld);
        }
    }
}