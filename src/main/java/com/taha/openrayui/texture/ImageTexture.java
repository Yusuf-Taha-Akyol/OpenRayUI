package com.taha.openrayui.texture;

import com.taha.openrayui.math.Vec3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Maps an image file onto a geometry using UV coordinates.
 * Handles texture loading, coordinate scaling, and wrapping (tiling).
 */
public class ImageTexture implements Texture {

    private transient BufferedImage image;
    private int width;
    private int height;
    private double scale; // Controls texture repetition frequency

    /**
     * Creates a new ImageTexture from a file path.
     * @param filename Absolute or relative path to the image file.
     */
    public ImageTexture(String filename) {
        this(filename, 1.0);
    }

    /**
     * Creates a new ImageTexture with a specific scale.
     * @param filename Path to the image file.
     * @param scale Scale factor (1.0 = normal, 2.0 = repeat twice).
     */
    public ImageTexture(String filename, double scale) {
        this.scale = scale;
        load(filename);
    }

    private void load(String filename) {
        if (filename == null) return;
        try {
            image = ImageIO.read(new File(filename));
            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
                System.out.println("Texture loaded successfully: " + filename + " (" + width + "x" + height + ")");
            }
        } catch (IOException e) {
            // Log error but don't crash; will render debug color later.
            System.err.println("Error loading texture: " + filename);
            image = null;
        }
    }

    public void setScale(double scale) { this.scale = scale; }
    public double getScale() { return scale; }

    @Override
    public Vec3 value(double u, double v, Vec3 p) {
        // If image failed to load, return standard "Error Magenta" color.
        if (image == null) return new Vec3(1, 0, 1);

        // 1. Apply Scale
        u *= scale;
        v *= scale;

        // 2. Wrap Coordinates (Infinite Tiling)
        // Uses Math.floor to correctly handle negative coordinates.
        // Example: 1.2 -> 0.2, -0.2 -> 0.8
        u = u - Math.floor(u);
        v = v - Math.floor(v);

        // 3. Flip V Coordinate
        // Image coordinates start top-left, but 3D UV space typically starts bottom-left.
        v = 1.0 - v;

        // 4. Map to Pixel Coordinates
        int x = (int)(u * (width - 1));
        int y = (int)(v * (height - 1));

        // 5. Sample Pixel Color
        int rgb = image.getRGB(x, y);

        // Convert integer RGB [0..255] to double Vec3 [0.0..1.0]
        double r = ((rgb >> 16) & 0xff) / 255.0;
        double g = ((rgb >> 8)  & 0xff) / 255.0;
        double b = ( rgb        & 0xff) / 255.0;

        return new Vec3(r, g, b);
    }

    // Custom deserialization to reload image if scene is saved/loaded
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Note: Logic to reload 'image' from 'filename' should be handled here or by the scene manager.
    }
}