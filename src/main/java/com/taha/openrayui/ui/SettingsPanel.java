package com.taha.openrayui.ui;

import com.taha.openrayui.math.Vec3;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {

    // Render'ı yeniden başlatmak için ana programa sinyal göndereceğiz
    private final Runnable onRenderTrigger;

    public SettingsPanel(Runnable onRenderTrigger) {
        this.onRenderTrigger = onRenderTrigger;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(250, 0)); // Genişliği sabitle

        addHeader("Render Ayarları");

        // --- Sample Count (Kalite) ---
        addLabel("Kalite (Sample):");
        JSpinner sampleSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 10));
        sampleSpinner.addChangeListener(e -> {
            RenderSettings.getInstance().samplesPerPixel = (int) sampleSpinner.getValue();
        });
        addComponent(sampleSpinner);

        // --- Derinlik (Bounces) ---
        addLabel("Işık Sekmesi (Depth):");
        JSpinner depthSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 100, 5));
        depthSpinner.addChangeListener(e -> {
            RenderSettings.getInstance().maxDepth = (int) depthSpinner.getValue();
        });
        addComponent(depthSpinner);

        addSeparator();
        addHeader("Kamera Pozisyonu");

        // --- Kamera X, Y, Z ---
        // Basit olması için 3 tane ayrı kutu yapıyoruz
        addLabel("Konum X:");
        addSmartTextField(0.0, val -> RenderSettings.getInstance().lookFrom =
                new Vec3(val, RenderSettings.getInstance().lookFrom.y, RenderSettings.getInstance().lookFrom.z));

        addLabel("Konum Y:");
        addSmartTextField(0.0, val -> RenderSettings.getInstance().lookFrom =
                new Vec3(RenderSettings.getInstance().lookFrom.x, val, RenderSettings.getInstance().lookFrom.z));

        addLabel("Konum Z:");
        addSmartTextField(1.0, val -> RenderSettings.getInstance().lookFrom =
                new Vec3(RenderSettings.getInstance().lookFrom.x, RenderSettings.getInstance().lookFrom.y, val));

        addSeparator();

        // --- Render Butonu ---
        JButton renderBtn = new JButton("RENDER AL");
        renderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        renderBtn.setBackground(new Color(70, 130, 180)); // Çelik Mavisi
        renderBtn.setForeground(Color.WHITE);
        renderBtn.setFont(new Font("Arial", Font.BOLD, 14));
        renderBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        renderBtn.addActionListener(e -> {
            // Butona basılınca App.java'ya "Başlat" sinyali gönder
            onRenderTrigger.run();
        });

        add(Box.createVerticalStrut(20));
        add(renderBtn);
    }

    // --- Yardımcı Metotlar (Kod tekrarını önlemek için) ---

    private void addHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label);
        add(Box.createVerticalStrut(10));
    }

    private void addLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label);
        add(Box.createVerticalStrut(2));
    }

    private void addComponent(JComponent comp) {
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        add(comp);
        add(Box.createVerticalStrut(10));
    }

    private void addSeparator() {
        add(Box.createVerticalStrut(10));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        add(sep);
        add(Box.createVerticalStrut(10));
    }

    // Kullanıcı sayı girip Enter'a basınca ayarı güncelleyen akıllı kutu
    // Kullanıcı sayı girip Enter'a basınca VEYA kutudan çıkınca güncelleyen akıllı metod
    private void addSmartTextField(double defaultValue, java.util.function.DoubleConsumer onUpdate) {
        JTextField field = new JTextField(String.valueOf(defaultValue));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bu küçük yardımcı fonksiyon, değeri güvenli bir şekilde günceller
        Runnable updateAction = () -> {
            try {
                double val = Double.parseDouble(field.getText());
                onUpdate.accept(val);
                System.out.println("Ayar güncellendi: " + val);
            } catch (NumberFormatException ex) {
                System.err.println("Geçersiz sayı, eski değer korunuyor.");
            }
        };

        // 1. Enter tuşuna basılınca kaydet
        field.addActionListener(e -> updateAction.run());

        // 2. Kutudan fareyle başka yere tıklayınca (Focus Lost) kaydet <-- YENİ ÖZELLİK
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                updateAction.run();
            }
        });

        add(field);
        add(Box.createVerticalStrut(5));
    }
}