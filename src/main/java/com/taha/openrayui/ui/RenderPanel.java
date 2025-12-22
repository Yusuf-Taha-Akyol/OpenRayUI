package com.taha.openrayui.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RenderPanel extends JPanel {
    private BufferedImage image;

    public RenderPanel(int width, int height) {
        // Başlangıçta siyah boş bir resim oluştur
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(width, height));
    }

    // Dışarıdan gelen güncellenmiş resmi buraya atayacağız
    public void updateImage(BufferedImage newImage) {
        this.image = newImage;
        repaint(); // Java'ya "Ekranı yeniden boya!" emri verir
    }

    // Resmi güvenli şekilde almak için (Render motoru buna boyayacak)
    public BufferedImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            // Resmi panelin sol üst köşesine (0,0) çiz
            g.drawImage(image, 0, 0, this);
        }
    }
}
