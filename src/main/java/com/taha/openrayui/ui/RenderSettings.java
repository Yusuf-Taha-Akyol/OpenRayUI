package com.taha.openrayui.ui;

import com.taha.openrayui.math.Vec3;

/**
 * Kullanıcının değiştirebileceği tüm ayarları tutan model sınıfı.
 */
public class RenderSettings {
    // Render Kalitesi
    public int imageWidth = 400;
    public int imageHeight = 225;
    public int samplesPerPixel = 10;
    public int maxDepth = 20;

    // Kamera Ayarları
    public Vec3 lookFrom = new Vec3(0, 0, 1);  // Kameranın yeri
    public Vec3 lookAt = new Vec3(0, 0, -1);   // Baktığı yer
    public double vFov = 90.0;                 // Görüş açısı

    // Singleton Pattern (Uygulamanın her yerinden aynı ayarlara ulaşmak için)
    private static RenderSettings instance;

    private RenderSettings() {}

    public static RenderSettings getInstance() {
        if (instance == null) {
            instance = new RenderSettings();
        }
        return instance;
    }
}
