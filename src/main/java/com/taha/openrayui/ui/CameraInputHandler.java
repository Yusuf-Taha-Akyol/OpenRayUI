package com.taha.openrayui.ui;

import com.taha.openrayui.math.Vec3;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Handles mouse input to rotate (orbit) and zoom the camera "Blender-style".
 */
public class CameraInputHandler extends MouseAdapter {

    private final Runnable onInteractiveRender; // Fast render (low quality) while dragging
    private final Runnable onFinalRender;       // High quality render when mouse released

    private int lastX, lastY;
    private int originalSamples; // To remember the quality setting before dragging

    public CameraInputHandler(Runnable onInteractiveRender, Runnable onFinalRender) {
        this.onInteractiveRender = onInteractiveRender;
        this.onFinalRender = onFinalRender;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();

        // Save user's quality setting and switch to "Preview Mode"
        originalSamples = RenderSettings.getInstance().samplesPerPixel;
        RenderSettings.getInstance().samplesPerPixel = 1; // Super fast!
        RenderSettings.getInstance().maxDepth = 5;        // Fewer bounces
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Restore quality settings and trigger final render
        RenderSettings.getInstance().samplesPerPixel = originalSamples;
        RenderSettings.getInstance().maxDepth = 20; // Or whatever default was
        onFinalRender.run();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        RenderSettings settings = RenderSettings.getInstance();

        int deltaX = e.getX() - lastX;
        int deltaY = e.getY() - lastY;

        lastX = e.getX();
        lastY = e.getY();

        // ORBIT LOGIC
        // We rotate the 'lookFrom' point around the 'lookAt' point.
        if (SwingUtilities.isLeftMouseButton(e)) {
            double sensitivity = 0.01;

            // Calculate vector from Target to Camera
            Vec3 offset = settings.lookFrom.sub(settings.lookAt);

            // Convert to Spherical Coordinates logic (simplified)
            double radius = offset.length();
            double theta = Math.atan2(offset.x, offset.z); // Horizontal angle
            double phi = Math.acos(offset.y / radius);     // Vertical angle

            theta -= deltaX * sensitivity;
            phi   -= deltaY * sensitivity;

            // Clamp vertical angle to avoid flipping upside down (Gimbal Lock prevention)
            phi = Math.max(0.01, Math.min(Math.PI - 0.01, phi));

            // Convert back to Cartesian
            double newX = radius * Math.sin(phi) * Math.sin(theta);
            double newY = radius * Math.cos(phi);
            double newZ = radius * Math.sin(phi) * Math.cos(theta);

            settings.lookFrom = settings.lookAt.add(new Vec3(newX, newY, newZ));

            // Trigger fast update
            onInteractiveRender.run();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        RenderSettings settings = RenderSettings.getInstance();

        // ZOOM LOGIC
        double zoomFactor = 1.1;
        Vec3 direction = settings.lookFrom.sub(settings.lookAt);

        if (e.getWheelRotation() < 0) {
            // Zoom In
            settings.lookFrom = settings.lookAt.add(direction.div(zoomFactor));
        } else {
            // Zoom Out
            settings.lookFrom = settings.lookAt.add(direction.mul(zoomFactor));
        }

        // Trigger render (we can keep quality high for zoom, or lower it if slow)
        onFinalRender.run();
    }
}
