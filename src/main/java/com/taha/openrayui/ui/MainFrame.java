package com.taha.openrayui.ui;

import com.taha.openrayui.ui.RenderPanel;
import com.taha.openrayui.ui.SettingsPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final RenderPanel renderPanel;
    private final SettingsPanel settingsPanel; // <-- Yeni

    // Constructor'a 'Runnable' parametresi ekledik!
    // App.java'dan buraya "Render'ı Başlat" fonksiyonunu göndereceğiz.
    public MainFrame(Runnable onRenderRequest) {
        setTitle("OpenRayUI - Java Ray Tracer Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ... (Layout kodları aynı) ...
        setLayout(new BorderLayout());

        // 1. Render Alanı
        renderPanel = new RenderPanel(400, 225);
        add(renderPanel, BorderLayout.CENTER);

        // 2. Ayarlar Paneli (DEĞİŞTİ)
        // Artık SettingsPanel kullanıyoruz
        settingsPanel = new SettingsPanel(onRenderRequest);
        add(settingsPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    public RenderPanel getRenderPanel() {
        return renderPanel;
    }
}
