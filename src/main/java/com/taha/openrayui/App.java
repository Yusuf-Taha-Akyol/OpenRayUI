package com.taha.openrayui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.taha.openrayui.core.Camera;
import com.taha.openrayui.core.Renderer;
import com.taha.openrayui.geometry.BVHNode;
import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.model.Scene;
import com.taha.openrayui.ui.MainFrame;
import com.taha.openrayui.core.RenderSettings;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * The main entry point of the application.
 * It initializes the UI with a modern dark theme and manages the multi-threaded rendering loop.
 */
public class App {
    // The main application window (Static access allows the render loop to update it)
    private static MainFrame frame;

    // Thread management variables to handle stopping/starting renders
    private static Thread currentRenderThread;
    private static volatile boolean keepRendering = true;

    public static void main(String[] args) {
        // --- STEP 1: SETUP MODERN DARK THEME (FLATLAF) ---
        // This replaces the old Swing look with a professional dark studio style.
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf theme.");
        }

        // --- STEP 2: LAUNCH THE UI ---
        SwingUtilities.invokeLater(() -> {
            // Initialize the main frame.
            // We pass a lambda function '() -> startNewRender()' so the UI can trigger new renders.
            frame = new MainFrame(() -> startNewRender());
            frame.setVisible(true);

            // Automatically start the first render
            startNewRender();
        });
    }

    /**
     * Stops any currently running render thread and starts a fresh one.
     * This is called when the app starts or when settings (camera, quality) change.
     */
    private static void startNewRender() {
        // 1. Signal the existing thread to stop
        keepRendering = false;

        // 2. Wait for the thread to die (graceful shutdown)
        if (currentRenderThread != null && currentRenderThread.isAlive()) {
            try {
                currentRenderThread.join(100); // Wait up to 100ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 3. Start a new render thread
        keepRendering = true;
        currentRenderThread = new Thread(() -> renderLoop());
        currentRenderThread.start();
    }

    /**
     * The core rendering loop running on a background thread.
     * It calculates pixel colors and updates the UI in real-time.
     */
    private static void renderLoop() {
        // Retrieve current settings (resolution, samples, camera pos)
        RenderSettings settings = RenderSettings.getInstance();

        int width = settings.imageWidth;
        int height = settings.imageHeight;
        int samples = settings.samplesPerPixel;
        int depth = settings.maxDepth;

        // Get the singleton scene instance
        HittableList sceneList = Scene.getInstance().getWorld();

        // --- OPTIMIZATION: Build BVH Tree ---
        // Instead of passing the raw list to the renderer, we wrap it in a BVHNode.
        // This organizes objects into a tree structure for faster intersection tests.
        Hittable world;
        if (sceneList.objects.isEmpty()) {
            world = sceneList;
        } else {
            // Build the acceleration structure just before rendering
            long bvhStart = System.currentTimeMillis();
            world = new BVHNode(sceneList);
            System.out.println("BVH Build Time: " + (System.currentTimeMillis() - bvhStart) + "ms");
        }

        // Initialize camera with dynamic settings from the UI
        Camera cam = new Camera(
                settings.lookFrom,
                settings.lookAt,
                settings.vFov,
                (double)width / height
        );

        Renderer renderer = new Renderer(depth);

        // Access the image buffer directly from the UI panel
        BufferedImage image = frame.getRenderPanel().getImage();

        System.out.println("Render Started! (Samples: " + samples + ", Camera: " + settings.lookFrom + ")");

        // --- PIXEL PROCESSING LOOP ---
        for (int j = 0; j < height; j++) {
            // Check if we need to stop (e.g., user changed settings)
            if (!keepRendering) return;

            for (int i = 0; i < width; i++) {
                Vec3 pixelColor = new Vec3(0, 0, 0);

                // Anti-aliasing: Average multiple samples per pixel
                for (int s = 0; s < samples; s++) {
                    double u = (double) (i + Math.random()) / (width - 1);
                    double v = (double) ((height - 1 - j) + Math.random()) / (height - 1);

                    Ray r = cam.getRay(u, v);
                    pixelColor = pixelColor.add(renderer.rayColor(r, sceneList, depth));
                }

                // Convert mathematical color (0.0-1.0) to RGB integer
                int rgb = convertColor(pixelColor, samples);

                // Write pixel to the image buffer
                image.setRGB(i, j, rgb);
            }

            // Refresh the UI after finishing each row to show progress
            frame.getRenderPanel().repaint();
        }
        System.out.println("Render Finished.");
    }

    /**
     * Helper: Converts a Vec3 color to a Java integer RGB format.
     * Includes Gamma Correction (Gamma 2.0).
     */
    private static int convertColor(Vec3 pixelColor, int samplesPerPixel) {
        double r = pixelColor.x;
        double g = pixelColor.y;
        double b = pixelColor.z;

        // Scale color by the number of samples
        double scale = 1.0 / samplesPerPixel;

        // Apply Gamma 2.0 Correction (Square root)
        r = Math.sqrt(r * scale);
        g = Math.sqrt(g * scale);
        b = Math.sqrt(b * scale);

        // Clamp values to [0.0, 0.999] and map to [0, 255]
        int ir = (int) (256 * clamp(r, 0.0, 0.999));
        int ig = (int) (256 * clamp(g, 0.0, 0.999));
        int ib = (int) (256 * clamp(b, 0.0, 0.999));

        // Combine components into a single integer
        return (ir << 16) | (ig << 8) | ib;
    }

    /**
     * Helper: Clamps a value between a minimum and maximum.
     */
    private static double clamp(double x, double min, double max) {
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }
}