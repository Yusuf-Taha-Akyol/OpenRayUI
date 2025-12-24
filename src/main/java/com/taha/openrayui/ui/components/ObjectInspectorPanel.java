package com.taha.openrayui.ui.components;

import com.taha.openrayui.geometry.Box;
import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.material.Dielectric;
import com.taha.openrayui.material.Lambertian;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.material.Metal;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.texture.ImageTexture;
import com.taha.openrayui.texture.Texture;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjectInspectorPanel extends JPanel {

    private final JPanel dynamicPanel;
    private final Runnable onUpdate;
    private Hittable currentObject;
    private JTextField nameField;
    private JComboBox<String> materialCombo;

    public ObjectInspectorPanel(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new TitledBorder("Object Inspector"), new EmptyBorder(10, 10, 10, 10)));
        setPreferredSize(new Dimension(280, 0)); // Slightly wider for ease

        dynamicPanel = new JPanel();
        dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));

        add(new JScrollPane(dynamicPanel), BorderLayout.CENTER);
        dynamicPanel.add(new JLabel("Select an object."));
    }

    public void inspect(Hittable obj) {
        this.currentObject = obj;
        dynamicPanel.removeAll();

        if (obj == null) {
            dynamicPanel.add(new JLabel("No selection."));
            revalidate(); repaint(); return;
        }

        // --- Common ---
        dynamicPanel.add(new JLabel("Name:"));
        nameField = new JTextField(obj.getName());
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameField.addActionListener(e -> { obj.setName(nameField.getText()); onUpdate.run(); });
        dynamicPanel.add(nameField);
        dynamicPanel.add(javax.swing.Box.createVerticalStrut(10));

        // --- Geometry ---
        if (obj instanceof Sphere) {
            Sphere s = (Sphere) obj;
            dynamicPanel.add(new JLabel("Center:"));
            addVec3Field(s::getCenter, v -> { s.setCenter(v); onUpdate.run(); });
            dynamicPanel.add(new JLabel("Radius:"));
            addDoubleField(s::getRadius, r -> { s.setRadius(r); onUpdate.run(); });
        } else if (obj instanceof Box) {
            Box b = (Box) obj;
            dynamicPanel.add(new JLabel("Center:"));
            addVec3Field(b::getCenter, v -> { b.setTransform(v, b.getSize()); onUpdate.run(); });
            dynamicPanel.add(new JLabel("Size (W/H/D):"));
            addVec3Field(b::getSize, v -> { b.setTransform(b.getCenter(), v); onUpdate.run(); });
        }

        // --- Material ---
        setupMaterialUI(obj);
        revalidate(); repaint();
    }

    private void setupMaterialUI(Hittable obj) {
        dynamicPanel.add(new JSeparator());
        dynamicPanel.add(javax.swing.Box.createVerticalStrut(10));
        dynamicPanel.add(new JLabel("Material Type:"));

        Material mat = obj.getMaterial();
        String[] types = {"Lambertian (Standard)", "Metal", "Dielectric (Glass)"};
        materialCombo = new JComboBox<>(types);

        if (mat instanceof Lambertian) materialCombo.setSelectedIndex(0);
        else if (mat instanceof Metal) materialCombo.setSelectedIndex(1);
        else if (mat instanceof Dielectric) materialCombo.setSelectedIndex(2);

        materialCombo.addActionListener(e -> {
            int idx = materialCombo.getSelectedIndex();
            Material newMat = (idx == 0) ? new Lambertian(new Vec3(0.5,0.5,0.5)) :
                    (idx == 1) ? new Metal(new Vec3(0.8,0.8,0.8), 0.0) :
                            new Dielectric(1.5);
            obj.setMaterial(newMat);
            inspect(obj); onUpdate.run();
        });
        dynamicPanel.add(materialCombo);
        dynamicPanel.add(javax.swing.Box.createVerticalStrut(10));

        if (mat instanceof Lambertian) {
            Lambertian l = (Lambertian) mat;

            // 1. Color Tint
            dynamicPanel.add(new JLabel("Color Tint (Multiplier):"));
            dynamicPanel.add(new JLabel("<html><i style='color:gray'>White (1,1,1) = No Tint</i></html>"));
            addVec3Field(l::getColor, c -> { l.setColor(c); onUpdate.run(); });
            dynamicPanel.add(javax.swing.Box.createVerticalStrut(5));

            // 2. Texture Loading
            JButton loadBtn = new JButton("Load Texture Image...");
            loadBtn.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    l.setTexture(new ImageTexture(fc.getSelectedFile().getAbsolutePath()));
                    l.setColor(new Vec3(1,1,1)); // Auto-reset tint to white
                    inspect(obj); onUpdate.run();
                }
            });
            dynamicPanel.add(loadBtn);

            // 3. Scale (Only if ImageTexture)
            if (l.getTexture() instanceof ImageTexture) {
                ImageTexture img = (ImageTexture) l.getTexture();
                dynamicPanel.add(javax.swing.Box.createVerticalStrut(5));
                dynamicPanel.add(new JLabel("Texture Scale (Tiling):"));
                addDoubleField(img::getScale, s -> { img.setScale(s); onUpdate.run(); });
            }
        }
        else if (mat instanceof Metal) {
            Metal m = (Metal) mat;
            dynamicPanel.add(new JLabel("Base Color:"));
            addVec3Field(m::getAlbedo, c -> { m.setAlbedo(c); onUpdate.run(); });
            dynamicPanel.add(new JLabel("Roughness (0-1):"));
            addDoubleField(m::getFuzz, f -> { m.setFuzz(f); onUpdate.run(); });
        }
        else if (mat instanceof Dielectric) {
            Dielectric d = (Dielectric) mat;
            dynamicPanel.add(new JLabel("IOR (1.5 = Glass):"));
            addDoubleField(d::getIr, i -> { d.setIr(i); onUpdate.run(); });
        }
    }

    // --- Helpers ---
    private void addVec3Field(Supplier<Vec3> get, Consumer<Vec3> set) {
        JPanel p = new JPanel(new GridLayout(1, 3, 5, 0));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        Vec3 v = get.get();
        JTextField tx = new JTextField(String.format("%.2f", v.x));
        JTextField ty = new JTextField(String.format("%.2f", v.y));
        JTextField tz = new JTextField(String.format("%.2f", v.z));

        java.awt.event.ActionListener l = e -> {
            try { set.accept(new Vec3(Double.parseDouble(tx.getText()), Double.parseDouble(ty.getText()), Double.parseDouble(tz.getText()))); }
            catch (Exception ex) {}
        };
        tx.addActionListener(l); ty.addActionListener(l); tz.addActionListener(l);
        p.add(tx); p.add(ty); p.add(tz);
        dynamicPanel.add(p);
    }

    private void addDoubleField(Supplier<Double> get, Consumer<Double> set) {
        JTextField tf = new JTextField(String.format("%.2f", get.get()));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        tf.addActionListener(e -> { try { set.accept(Double.parseDouble(tf.getText())); } catch (Exception ex) {} });
        dynamicPanel.add(tf);
    }
}