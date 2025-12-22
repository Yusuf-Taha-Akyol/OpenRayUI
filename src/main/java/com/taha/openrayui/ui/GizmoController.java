package com.taha.openrayui.ui;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.math.Vec3;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Handles interactions with the Transformation Gizmo.
 * - Left Drag on Arrows -> Moves the object (X, Y, Z).
 * - Left Drag on Center -> Scales the object (Radius).
 */
public class GizmoController extends MouseAdapter {

    private final RenderPanel renderPanel;
    private final OutlinerPanel outlinerPanel;
    private final Runnable onSceneUpdate; // Fast update (Wireframe/Low res)
    private final Runnable onFinalUpdate; // Slow update (High res)

    // Interaction States
    private int activeAxis = -1; // 0=X, 1=Y, 2=Z, 3=Center(Scale), -1=None
    private Point lastMousePos;

    // Threshold in pixels to detect a click on lines
    private static final double HIT_TOLERANCE = 10.0;

    public GizmoController(RenderPanel renderPanel, OutlinerPanel outlinerPanel,
                           Runnable onSceneUpdate, Runnable onFinalUpdate) {
        this.renderPanel = renderPanel;
        this.outlinerPanel = outlinerPanel;
        this.onSceneUpdate = onSceneUpdate;
        this.onFinalUpdate = onFinalUpdate;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMousePos = e.getPoint();

        // Determine which part of the Gizmo was clicked
        activeAxis = checkGizmoHit(e.getPoint());

        if (activeAxis != -1) {
            // If we hit the gizmo, lower quality for fast editing
            RenderSettings.getInstance().samplesPerPixel = 1;
            RenderSettings.getInstance().maxDepth = 3;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (activeAxis != -1) {
            activeAxis = -1;
            // Restore quality
            RenderSettings.getInstance().samplesPerPixel = 10; // Or whatever default
            RenderSettings.getInstance().maxDepth = 50;
            onFinalUpdate.run();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (activeAxis == -1) return; // Not interacting with gizmo

        Hittable selectedObj = outlinerPanel.getList().getSelectedValue();
        if (!(selectedObj instanceof Sphere)) return;

        Sphere sphere = (Sphere) selectedObj;
        Point currentPos = e.getPoint();

        // Calculate mouse delta
        int dx = currentPos.x - lastMousePos.x;
        int dy = currentPos.y - lastMousePos.y;

        // Sensitivity factor (adjust based on distance to camera ideally)
        double sensitivity = 0.02;

        // Apply Transformation based on Active Axis
        if (activeAxis == 3) {
            // --- SCALING (Center) ---
            // Dragging Right/Up increases size
            double scaleFactor = (dx - dy) * sensitivity * 0.5;
            double newRadius = Math.max(0.1, sphere.getRadius() + scaleFactor);
            sphere.setRadius(newRadius);

        } else {
            // --- TRANSLATION (Arrows) ---
            Vec3 currentCenter = sphere.getCenter();
            Vec3 moveDelta = new Vec3(0,0,0);

            if (activeAxis == 0) { // X Axis (Red)
                // We project mouse movement loosely to X
                moveDelta = new Vec3(dx * sensitivity, 0, 0);
            }
            else if (activeAxis == 1) { // Y Axis (Green)
                // Screen Y is inverted, so -dy
                moveDelta = new Vec3(0, -dy * sensitivity, 0);
            }
            else if (activeAxis == 2) { // Z Axis (Blue)
                // Z mapping is tricky in 2D, approximations:
                moveDelta = new Vec3(0, 0, (dx + dy) * sensitivity);
            }

            sphere.setCenter(currentCenter.add(moveDelta));
        }

        // Update View
        onSceneUpdate.run(); // Re-render
        renderPanel.repaint(); // Re-draw gizmo
        lastMousePos = currentPos;
    }

    /**
     * Checks if the mouse click hits any part of the gizmo.
     * @return 0=X, 1=Y, 2=Z, 3=Center, -1=None
     */
    private int checkGizmoHit(Point mouseP) {
        Hittable selectedObj = outlinerPanel.getList().getSelectedValue();
        if (selectedObj == null || !(selectedObj instanceof Sphere)) return -1;

        Sphere sphere = (Sphere) selectedObj;
        Vec3 center = sphere.getCenter();
        double len = sphere.getRadius() * 1.5;

        // Project Gizmo points to screen
        Point pC = CameraHelper.worldToScreen(center, renderPanel.getWidth(), renderPanel.getHeight());
        Point pX = CameraHelper.worldToScreen(center.add(new Vec3(len, 0, 0)), renderPanel.getWidth(), renderPanel.getHeight());
        Point pY = CameraHelper.worldToScreen(center.add(new Vec3(0, len, 0)), renderPanel.getWidth(), renderPanel.getHeight());
        Point pZ = CameraHelper.worldToScreen(center.add(new Vec3(0, 0, len)), renderPanel.getWidth(), renderPanel.getHeight());

        if (pC == null) return -1;

        // 1. Check Center (Scaling)
        if (mouseP.distance(pC) < HIT_TOLERANCE * 1.5) {
            return 3; // Center hit
        }

        // 2. Check Axes (Translation)
        if (pX != null && CameraHelper.distanceToSegment(mouseP, pC, pX) < HIT_TOLERANCE) return 0;
        if (pY != null && CameraHelper.distanceToSegment(mouseP, pC, pY) < HIT_TOLERANCE) return 1;
        if (pZ != null && CameraHelper.distanceToSegment(mouseP, pC, pZ) < HIT_TOLERANCE) return 2;

        return -1; // No hit
    }

    // Helper to allow CameraController to know if we consumed the event
    public boolean isInteracting() {
        return activeAxis != -1;
    }
}