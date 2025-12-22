package com.taha.openrayui;

import com.taha.openrayui.core.Camera;
import com.taha.openrayui.core.Renderer;
import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.math.Ray;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.model.Scene;
import com.taha.openrayui.ui.MainFrame;
import com.taha.openrayui.ui.RenderSettings;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class App {

    // DEĞİŞİKLİK 1: frame artık sınıfın bir parçası (static field)
    private static MainFrame frame;

    private static Thread currentRenderThread;
    private static volatile boolean keepRendering = true;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // DEĞİŞİKLİK 2: Parametre vermiyoruz, çünkü frame artık static
            frame = new MainFrame(() -> startNewRender());
            frame.setVisible(true);

            // İlk render'ı başlat
            startNewRender();
        });
    }

    // DEĞİŞİKLİK 3: Parametreden 'frame' silindi
    private static void startNewRender() {
        // Eski thread'i durdur
        keepRendering = false;
        if (currentRenderThread != null && currentRenderThread.isAlive()) {
            try {
                currentRenderThread.join(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Yeni thread başlat
        keepRendering = true;
        currentRenderThread = new Thread(() -> renderLoop()); // Buradan da parametre kalktı
        currentRenderThread.start();
    }

    // DEĞİŞİKLİK 4: Parametreden 'frame' silindi
    private static void renderLoop() {
        RenderSettings settings = RenderSettings.getInstance();

        int width = settings.imageWidth;
        int height = settings.imageHeight;
        int samples = settings.samplesPerPixel;
        int depth = settings.maxDepth;

        HittableList world = Scene.createWorld();

        Camera cam = new Camera(
                settings.lookFrom,
                settings.lookAt,
                settings.vFov,
                (double)width / height
        );

        Renderer renderer = new Renderer(depth);

        // Static frame değişkenini kullanıyoruz
        BufferedImage image = frame.getRenderPanel().getImage();

        System.out.println("Render Başladı! (Sample: " + samples + ")");

        for (int j = 0; j < height; j++) {
            if (!keepRendering) return;

            for (int i = 0; i < width; i++) {
                Vec3 pixelColor = new Vec3(0, 0, 0);

                for (int s = 0; s < samples; s++) {
                    double u = (double) (i + Math.random()) / (width - 1);
                    double v = (double) ((height - 1 - j) + Math.random()) / (height - 1);

                    Ray r = cam.getRay(u, v);
                    pixelColor = pixelColor.add(renderer.rayColor(r, world, depth));
                }

                int rgb = convertColor(pixelColor, samples);
                image.setRGB(i, j, rgb);
            }
            // Static frame değişkenini kullanıyoruz
            frame.getRenderPanel().repaint();
        }
        System.out.println("Render Tamamlandı.");
    }

    private static int convertColor(Vec3 pixelColor, int samplesPerPixel) {
        double r = pixelColor.x;
        double g = pixelColor.y;
        double b = pixelColor.z;
        double scale = 1.0 / samplesPerPixel;
        r = Math.sqrt(r * scale);
        g = Math.sqrt(g * scale);
        b = Math.sqrt(b * scale);
        int ir = (int) (256 * clamp(r, 0.0, 0.999));
        int ig = (int) (256 * clamp(g, 0.0, 0.999));
        int ib = (int) (256 * clamp(b, 0.0, 0.999));
        return (ir << 16) | (ig << 8) | ib;
    }

    private static double clamp(double x, double min, double max) {
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }
}