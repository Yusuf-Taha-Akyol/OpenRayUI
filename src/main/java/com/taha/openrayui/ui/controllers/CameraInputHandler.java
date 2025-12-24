package com.taha.openrayui.ui.controllers;

import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.core.RenderSettings;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Handles camera controls:
 * - Left Drag: Orbit (Rotate around target)
 * - Right Drag: Pan (Move camera and target together)
 * - Wheel / Buttons: Zoom (Move closer/further)
 */
public class CameraInputHandler extends MouseAdapter {

    private final Runnable onInteractiveRender; // Fast render
    private final Runnable onFinalRender;       // High quality render

    private int lastX, lastY;
    private int originalSamples;

    public CameraInputHandler(Runnable onInteractiveRender, Runnable onFinalRender) {
        this.onInteractiveRender = onInteractiveRender;
        this.onFinalRender = onFinalRender;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();

        // Save quality and switch to low-res for speed
        originalSamples = RenderSettings.getInstance().samplesPerPixel;
        RenderSettings.getInstance().samplesPerPixel = 1;
        RenderSettings.getInstance().maxDepth = 5;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Restore quality
        RenderSettings.getInstance().samplesPerPixel = originalSamples;
        RenderSettings.getInstance().maxDepth = 20;
        onFinalRender.run();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        RenderSettings settings = RenderSettings.getInstance();
        int deltaX = e.getX() - lastX;
        int deltaY = e.getY() - lastY;

        lastX = e.getX();
        lastY = e.getY();

        // --- ORBIT (Left Click) ---
        // Rotates around the current target object
        if (SwingUtilities.isLeftMouseButton(e)) {
            double sensitivity = 0.01;
            Vec3 offset = settings.lookFrom.sub(settings.lookAt);

            double radius = offset.length();
            double theta = Math.atan2(offset.x, offset.z);
            double phi = Math.acos(offset.y / radius);

            theta -= deltaX * sensitivity;
            phi   -= deltaY * sensitivity;

            // Prevent gimbal lock
            phi = Math.max(0.01, Math.min(Math.PI - 0.01, phi));

            double newX = radius * Math.sin(phi) * Math.sin(theta);
            double newY = radius * Math.cos(phi);
            double newZ = radius * Math.sin(phi) * Math.cos(theta);

            settings.lookFrom = settings.lookAt.add(new Vec3(newX, newY, newZ));

            onInteractiveRender.run();
        }

        // --- PAN (Right Click) ---
        // Moves the camera AND the target point (Trucking)
        else if (SwingUtilities.isRightMouseButton(e)) {
            double panSpeed = 0.005 * settings.lookFrom.sub(settings.lookAt).length(); // Scale speed by distance

            // Calculate camera basis vectors
            Vec3 forward = settings.lookAt.sub(settings.lookFrom).unitVector();
            Vec3 right = new Vec3(0, 1, 0).cross(forward).unitVector(); // Global Up (0,1,0)
            Vec3 up = forward.cross(right).unitVector();

            // Calculate movement vector based on mouse delta
            Vec3 movement = right.mul(-deltaX * panSpeed).add(up.mul(deltaY * panSpeed));

            // Move both 'Eye' and 'Target'
            settings.lookFrom = settings.lookFrom.add(movement);
            settings.lookAt = settings.lookAt.add(movement);

            onInteractiveRender.run();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // -1 for Zoom In, +1 for Zoom Out
        performZoom(e.getWheelRotation());
    }

    // --- PUBLIC ZOOM API (For UI Buttons) ---

    public void zoomIn() {
        performZoom(-1); // Negative means closer
    }

    public void zoomOut() {
        performZoom(1); // Positive means further
    }

    private void performZoom(double direction) {
        RenderSettings settings = RenderSettings.getInstance();
        double zoomFactor = (direction < 0) ? 0.9 : 1.1; // 10% zoom step

        Vec3 viewVector = settings.lookFrom.sub(settings.lookAt);
        double dist = viewVector.length();

        // Prevent zooming past the target (min distance 0.1)
        if (direction < 0 && dist < 0.2) return;

        // Apply zoom
        settings.lookFrom = settings.lookAt.add(viewVector.mul(zoomFactor));

        onFinalRender.run();
    }
}
