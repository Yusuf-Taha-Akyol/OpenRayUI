package com.taha.openrayui.ui.controllers;

import com.taha.openrayui.core.RenderSettings;
import com.taha.openrayui.geometry.Box; // Import Box
import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.ui.components.OutlinerPanel;
import com.taha.openrayui.ui.components.RenderPanel;
import com.taha.openrayui.utils.CameraHelper;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Handles mouse interactions with the Gizmo.
 * Supports moving and scaling for both Spheres and Boxes.
 */
public class GizmoController extends MouseAdapter {

    private final RenderPanel renderPanel;
    private final OutlinerPanel outlinerPanel;
    private final Runnable onSceneUpdate;
    private final Runnable onFinalUpdate;

    private int activeAxis = -1; // 0=X, 1=Y, 2=Z, 3=Scale, -1=None
    private Point lastMousePos;
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
        activeAxis = checkGizmoHit(e.getPoint());

        if (activeAxis != -1) {
            // Lower quality during interaction for speed
            RenderSettings.getInstance().samplesPerPixel = 1;
            RenderSettings.getInstance().maxDepth = 3;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (activeAxis != -1) {
            activeAxis = -1;
            // Restore quality (handled by MainFrame callbacks usually, but safe to trigger update)
            onFinalUpdate.run();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (activeAxis == -1) return;

        Hittable selectedObj = outlinerPanel.getList().getSelectedValue();
        if (selectedObj == null) return;

        Point currentPos = e.getPoint();
        int dx = currentPos.x - lastMousePos.x;
        int dy = currentPos.y - lastMousePos.y;

        // Sensitivity factor
        double sensitivity = 0.02;

        if (selectedObj instanceof Sphere) {
            handleSphereTransform((Sphere) selectedObj, dx, dy, sensitivity);
        } else if (selectedObj instanceof Box) {
            handleBoxTransform((Box) selectedObj, dx, dy, sensitivity);
        }

        onSceneUpdate.run();
        renderPanel.repaint();
        lastMousePos = currentPos;
    }

    // --- SPHERE LOGIC ---
    private void handleSphereTransform(Sphere sphere, int dx, int dy, double sensitivity) {
        if (activeAxis == 3) {
            // Scale (Radius)
            double scaleFactor = (dx - dy) * sensitivity * 0.5;
            double newRadius = Math.max(0.1, sphere.getRadius() + scaleFactor);
            sphere.setRadius(newRadius);
        } else {
            // Move
            Vec3 moveDelta = calculateMoveDelta(dx, dy, sensitivity);
            sphere.setCenter(sphere.getCenter().add(moveDelta));
        }
    }

    // --- BOX LOGIC (NEW) ---
    private void handleBoxTransform(Box box, int dx, int dy, double sensitivity) {
        if (activeAxis == 3) {
            // Scale (Uniform resize from center)
            double scaleFactor = (dx - dy) * sensitivity * 0.5;
            // Avoid negative size
            double delta = scaleFactor;

            Vec3 oldSize = box.getSize();
            Vec3 newSize = oldSize.add(new Vec3(delta, delta, delta));

            // Ensure minimum size of 0.1
            if (newSize.x < 0.1) newSize = new Vec3(0.1, newSize.y, newSize.z);
            if (newSize.y < 0.1) newSize = new Vec3(newSize.x, 0.1, newSize.z);
            if (newSize.z < 0.1) newSize = new Vec3(newSize.x, newSize.y, 0.1);

            box.setTransform(box.getCenter(), newSize);
        } else {
            // Move
            Vec3 moveDelta = calculateMoveDelta(dx, dy, sensitivity);
            box.setTransform(box.getCenter().add(moveDelta), box.getSize());
        }
    }

    private Vec3 calculateMoveDelta(int dx, int dy, double sensitivity) {
        if (activeAxis == 0) return new Vec3(dx * sensitivity, 0, 0); // X
        if (activeAxis == 1) return new Vec3(0, -dy * sensitivity, 0); // Y
        if (activeAxis == 2) return new Vec3(0, 0, (dx + dy) * sensitivity); // Z
        return new Vec3(0,0,0);
    }

    // --- HIT DETECTION ---
    private int checkGizmoHit(Point mouseP) {
        Hittable selectedObj = outlinerPanel.getList().getSelectedValue();
        if (selectedObj == null) return -1;

        Vec3 center;
        double len;

        // Determine center and gizmo size based on object type
        if (selectedObj instanceof Sphere) {
            Sphere s = (Sphere) selectedObj;
            center = s.getCenter();
            len = s.getRadius() * 1.5;
        } else if (selectedObj instanceof Box) {
            Box b = (Box) selectedObj;
            center = b.getCenter();
            Vec3 s = b.getSize();
            len = Math.max(s.x, Math.max(s.y, s.z)) * 0.8;
            if (len < 0.5) len = 0.5;
        } else {
            return -1;
        }

        int w = renderPanel.getWidth();
        int h = renderPanel.getHeight();

        Point pC = CameraHelper.worldToScreen(center, w, h);
        Point pX = CameraHelper.worldToScreen(center.add(new Vec3(len, 0, 0)), w, h);
        Point pY = CameraHelper.worldToScreen(center.add(new Vec3(0, len, 0)), w, h);
        Point pZ = CameraHelper.worldToScreen(center.add(new Vec3(0, 0, len)), w, h);

        if (pC == null) return -1;

        // Check Center (Scale)
        if (mouseP.distance(pC) < HIT_TOLERANCE * 1.5) return 3;

        // Check Axes (Move)
        if (pX != null && CameraHelper.distanceToSegment(mouseP, pC, pX) < HIT_TOLERANCE) return 0;
        if (pY != null && CameraHelper.distanceToSegment(mouseP, pC, pY) < HIT_TOLERANCE) return 1;
        if (pZ != null && CameraHelper.distanceToSegment(mouseP, pC, pZ) < HIT_TOLERANCE) return 2;

        return -1;
    }

    public boolean isInteracting() {
        return activeAxis != -1;
    }
}