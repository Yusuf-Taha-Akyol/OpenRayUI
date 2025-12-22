package com.taha.openrayui.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Handles exporting the rendered buffer to image files (PNG, JPG, etc.).
 */
public class ImageExporter {

    /**
     * Saves the given BufferedImage to a PNG file.
     * @param image The image to save.
     * @param file The destination file.
     * @throws IOException If writing fails.
     */
    public static void saveImage(BufferedImage image, File file) throws IOException {
        if (!file.getAbsolutePath().toLowerCase().endsWith(".png")) {
            file = new File(file.getAbsolutePath() + ".png");
        }
        ImageIO.write(image, "png", file);
    }
}
