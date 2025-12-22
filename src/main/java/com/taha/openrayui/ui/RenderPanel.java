package com.taha.openrayui.ui;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.math.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The primary viewport panel used to display the rendered image.
 * It also handles the drawing of "Gizmos" (Transformation Arrows) overlaying the 3D objects.
 */
public class RenderPanel extends JPanel {

    private BufferedImage image;
    private Hittable selectedObject; // Reference to the currently selected scene object

    public RenderPanel(int width, int height) {
        // Initialize the image buffer with RGB color model
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(width, height));
    }

    /**
     * Sets the object to be highlighted with the Gizmo.
     * Triggers a repaint to update the overlay immediately.
     */
    public void setSelectedObject(Hittable obj) {
        this.selectedObject = obj;
        repaint();
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Draw the Base Render (The Ray Traced Image)
        if (image != null) {
            // Draw image scaled to fill the entire panel
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }

        // 2. Draw UI Overlay (Transformation Gizmo)
        // We check if the selected object is a Sphere to draw appropriate helpers.
        if (selectedObject != null && selectedObject instanceof Sphere) {
            drawGizmo((Graphics2D) g, (Sphere) selectedObject);
        }
    }

    /**
     * Renders the 3D axis arrows (X, Y, Z) on top of the object.
     */
    private void drawGizmo(Graphics2D g2, Sphere sphere) {
        // Enable Anti-Aliasing for smooth lines
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2)); // Set line thickness

        Vec3 center = sphere.getCenter();

        // Dynamic Sizing:
        // The arrow length is proportional to the object's radius (150% of radius).
        // This ensures the gizmo isn't too large for small objects or too small for huge ones.
        double axisLength = sphere.getRadius() * 1.5;

        // Calculate world coordinates for the tips of the axes
        Vec3 xEnd = center.add(new Vec3(axisLength, 0, 0));
        Vec3 yEnd = center.add(new Vec3(0, axisLength, 0));
        Vec3 zEnd = center.add(new Vec3(0, 0, axisLength));

        // Project 3D points to 2D screen coordinates
        Point pCenter = CameraHelper.worldToScreen(center, getWidth(), getHeight());
        Point pX = CameraHelper.worldToScreen(xEnd, getWidth(), getHeight());
        Point pY = CameraHelper.worldToScreen(yEnd, getWidth(), getHeight());
        Point pZ = CameraHelper.worldToScreen(zEnd, getWidth(), getHeight());

        // Abort if the center of the object is behind the camera
        if (pCenter == null) return;

        // --- Draw X Axis (Red) ---
        if (pX != null) {
            g2.setColor(Color.RED);
            g2.drawLine(pCenter.x, pCenter.y, pX.x, pX.y);
            // Draw a small square handle at the tip
            g2.fillRect(pX.x - 3, pX.y - 3, 6, 6);
            g2.drawString("X", pX.x + 8, pX.y);
        }

        // --- Draw Y Axis (Green) ---
        if (pY != null) {
            g2.setColor(Color.GREEN);
            g2.drawLine(pCenter.x, pCenter.y, pY.x, pY.y);
            g2.fillRect(pY.x - 3, pY.y - 3, 6, 6);
            g2.drawString("Y", pY.x + 8, pY.y);
        }

        // --- Draw Z Axis (Blue) ---
        if (pZ != null) {
            g2.setColor(Color.BLUE);
            g2.drawLine(pCenter.x, pCenter.y, pZ.x, pZ.y);
            g2.fillRect(pZ.x - 3, pZ.y - 3, 6, 6);
            g2.drawString("Z", pZ.x + 8, pZ.y);
        }

        // Draw the pivot point (Center Yellow Dot)
        g2.setColor(Color.YELLOW);
        g2.fillOval(pCenter.x - 4, pCenter.y - 4, 8, 8);
    }
}