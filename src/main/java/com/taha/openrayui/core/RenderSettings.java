package com.taha.openrayui.core;

import com.taha.openrayui.math.Vec3;

/**
 * A Singleton configuration class acting as the "Single Source of Truth" for rendering parameters.
 * It ensures that both the Ray Tracing Engine and the UI Helpers (Gizmos) use the exact same values.
 */
public class RenderSettings {
    public int imageWidth = 400;
    public int imageHeight = 225;
    public int samplesPerPixel = 10;
    public int maxDepth = 20;

    public Vec3 lookFrom = new Vec3(0, 0, 1);
    public Vec3 lookAt = new Vec3(0, 0, -1);
    public double vFov = 20.0;

    private static RenderSettings instance;

    private RenderSettings() {}

    public static RenderSettings getInstance() {
        if (instance == null) {
            instance = new RenderSettings();
        }
        return instance;
    }
}
