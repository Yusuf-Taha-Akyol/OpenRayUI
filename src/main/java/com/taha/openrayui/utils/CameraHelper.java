package com.taha.openrayui.utils;

import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.core.RenderSettings;

import java.awt.*;

/**
 * Utility class handling the mathematical projection from 3D World Space to 2D Screen Space.
 * Essential for rendering UI overlays (Gizmos, Labels) on top of the 3D scene.
 */
public class CameraHelper {

    /**
     * Projects a 3D point onto the 2D screen coordinates.
     * * @param worldPoint The 3D position vector of the object.
     * @param panelWidth The actual width of the Swing panel (for final scaling).
     * @param panelHeight The actual height of the Swing panel (for final scaling).
     * @return A java.awt.Point representing pixel coordinates, or null if the point is behind the camera.
     */
    public static Point worldToScreen(Vec3 worldPoint, int panelWidth, int panelHeight) {
        RenderSettings settings = RenderSettings.getInstance();

        Vec3 lookFrom = settings.lookFrom;
        Vec3 lookAt = settings.lookAt;
        Vec3 vup = new Vec3(0, 1, 0); // Global "Up" vector

        // 1. Retrieve Field of View from settings to match the render engine
        double vfov = settings.vFov;

        // 2. Construct Camera Basis Vectors (Orthonormal Basis)
        Vec3 w = lookFrom.sub(lookAt).unitVector(); // Z-axis (pointing backwards)
        Vec3 u = vup.cross(w).unitVector();         // X-axis (pointing right)
        Vec3 v = w.cross(u);                        // Y-axis (pointing up)

        // 3. Calculate Vector from Camera to Point
        Vec3 relativePos = worldPoint.sub(lookFrom);

        // Calculate depth (distance along camera Z-axis)
        // We negate because 'w' points towards the camera, but we look forward.
        double distZ = relativePos.dot(w) * -1;

        // Culling: If point is behind or too close to the camera, do not render.
        if (distZ <= 0.1) return null;

        double distX = relativePos.dot(u); // Horizontal distance relative to camera
        double distY = relativePos.dot(v); // Vertical distance relative to camera

        // 4. Perspective Projection Math
        double theta = Math.toRadians(vfov);
        double h = Math.tan(theta / 2);

        // Aspect Ratio Calculation:
        // We MUST use the image resolution ratio, NOT the panel size ratio.
        // This prevents the gizmo from "drifting" when the window is resized/stretched.
        double aspectRatio = (double) settings.imageWidth / settings.imageHeight;

        double viewportHeight = 2.0 * h;
        double viewportWidth = viewportHeight * aspectRatio;

        // Calculate Normalized Device Coordinates (NDC)
        // Range: -0.5 to +0.5 (0,0 is screen center)
        double ndcX = distX / (viewportWidth * distZ);
        double ndcY = distY / (viewportHeight * distZ);

        // 5. Map NDC to Screen Pixels (Rasterization)
        // Note: Y-axis is inverted because Java2D coordinates start from top-left.
        int screenX = (int) ((ndcX + 0.5) * panelWidth);
        int screenY = (int) ((0.5 - ndcY) * panelHeight);

        return new Point(screenX, screenY);
    }

    /**
     * Calculates the minimum distance from a point (p) to a line segment (v1 to v2).
     * Used to detect if the mouse is hovering over a gizmo arrow.
     */
    public static double distanceToSegment(Point p, Point v1, Point v2) {
        double l2 = (v1.x - v2.x) * (v1.x - v2.x) + (v1.y - v2.y) * (v1.y - v2.y);
        if (l2 == 0) return p.distance(v1); // v1 and v2 are the same point

        // Consider the line extending the segment, parameterized as v1 + t (v2 - v1).
        // We find projection of point p onto the line.
        // It falls where t = [(p-v1) . (v2-v1)] / |v2-v1|^2
        double t = ((p.x - v1.x) * (v2.x - v1.x) + (p.y - v1.y) * (v2.y - v1.y)) / l2;

        // Clamp t to the segment [0, 1]
        t = Math.max(0, Math.min(1, t));

        // Projection point
        double projX = v1.x + t * (v2.x - v1.x);
        double projY = v1.y + t * (v2.y - v1.y);

        return Math.sqrt(Math.pow(p.x - projX, 2) + Math.pow(p.y - projY, 2));
    }
}