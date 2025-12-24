package com.taha.openrayui.ui.components;

import com.taha.openrayui.geometry.Box; // Import Box
import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.utils.CameraHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RenderPanel extends JPanel {

    private BufferedImage image;
    private Hittable selectedObject;

    public RenderPanel(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(width, height));
    }

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

        // 1. Draw Render
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }

        // 2. Draw Gizmo
        if (selectedObject != null) {
            if (selectedObject instanceof Sphere) {
                drawGizmo((Graphics2D) g, (Sphere) selectedObject);
            } else if (selectedObject instanceof Box) {
                drawGizmoBox((Graphics2D) g, (Box) selectedObject);
            }
        }
    }

    // --- SPHERE GIZMO ---
    private void drawGizmo(Graphics2D g2, Sphere sphere) {
        drawGenericGizmo(g2, sphere.getCenter(), sphere.getRadius() * 1.5);
    }

    // --- BOX GIZMO ---
    private void drawGizmoBox(Graphics2D g2, Box box) {
        Vec3 size = box.getSize();
        // Use the maximum dimension to determine arrow length
        double maxDim = Math.max(size.x, Math.max(size.y, size.z));
        drawGenericGizmo(g2, box.getCenter(), maxDim * 0.8);
    }

    /**
     * Shared logic to draw the X, Y, Z arrows.
     */
    private void drawGenericGizmo(Graphics2D g2, Vec3 center, double axisLength) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));

        // Ensure minimum visibility for small objects
        if (axisLength < 0.5) axisLength = 0.5;

        Vec3 xEnd = center.add(new Vec3(axisLength, 0, 0));
        Vec3 yEnd = center.add(new Vec3(0, axisLength, 0));
        Vec3 zEnd = center.add(new Vec3(0, 0, axisLength));

        Point pCenter = CameraHelper.worldToScreen(center, getWidth(), getHeight());
        Point pX = CameraHelper.worldToScreen(xEnd, getWidth(), getHeight());
        Point pY = CameraHelper.worldToScreen(yEnd, getWidth(), getHeight());
        Point pZ = CameraHelper.worldToScreen(zEnd, getWidth(), getHeight());

        if (pCenter == null) return;

        // X Axis (Red)
        if (pX != null) {
            g2.setColor(Color.RED);
            g2.drawLine(pCenter.x, pCenter.y, pX.x, pX.y);
            g2.fillRect(pX.x - 3, pX.y - 3, 6, 6);
            g2.drawString("X", pX.x + 8, pX.y);
        }

        // Y Axis (Green)
        if (pY != null) {
            g2.setColor(Color.GREEN);
            g2.drawLine(pCenter.x, pCenter.y, pY.x, pY.y);
            g2.fillRect(pY.x - 3, pY.y - 3, 6, 6);
            g2.drawString("Y", pY.x + 8, pY.y);
        }

        // Z Axis (Blue)
        if (pZ != null) {
            g2.setColor(Color.BLUE);
            g2.drawLine(pCenter.x, pCenter.y, pZ.x, pZ.y);
            g2.fillRect(pZ.x - 3, pZ.y - 3, 6, 6);
            g2.drawString("Z", pZ.x + 8, pZ.y);
        }

        // Center Pivot (Yellow)
        g2.setColor(Color.YELLOW);
        g2.fillOval(pCenter.x - 4, pCenter.y - 4, 8, 8);
    }
}