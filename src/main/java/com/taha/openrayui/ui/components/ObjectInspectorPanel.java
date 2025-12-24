package com.taha.openrayui.ui.components;

import com.taha.openrayui.geometry.Box; // Bizim Kutu Geometrimiz
import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.material.*;
import com.taha.openrayui.math.Vec3;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Locale;

/**
 * Panel to view and edit properties of the selected object.
 * Fixes: Uses Getters/Setters for private material fields.
 * Fixes: Resolves 'Box' name conflict using full package name for javax.swing.Box.
 */
public class ObjectInspectorPanel extends JPanel {

    private final Runnable onUpdate;
    private Hittable currentObject;

    // Container for dynamic fields (Position, Radius, Size etc.)
    private final JPanel dynamicPanel;

    // Material UI components
    private JComboBox<String> materialCombo;

    public ObjectInspectorPanel(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new CompoundBorder(new TitledBorder("Object Properties"), new EmptyBorder(10, 10, 10, 10)));

        dynamicPanel = new JPanel();
        dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));

        add(dynamicPanel);

        // ÇAKIŞMA ÇÖZÜMÜ: Swing Box için tam yol belirtiyoruz
        add(javax.swing.Box.createVerticalGlue());
    }

    /**
     * Refreshes the inspector UI for the given object.
     */
    public void inspect(Hittable obj) {
        this.currentObject = obj;
        dynamicPanel.removeAll(); // Clear old fields

        if (obj == null) {
            dynamicPanel.add(new JLabel("No object selected"));
        }
        else if (obj instanceof Sphere) {
            setupSphereUI((Sphere) obj);
            setupMaterialUI(obj);
        }
        else if (obj instanceof Box) {
            setupBoxUI((Box) obj);
            setupMaterialUI(obj);
        }

        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }

    // --- SPHERE UI LOGIC ---
    private void setupSphereUI(Sphere sphere) {
        dynamicPanel.add(new JLabel("Type: Sphere"));
        dynamicPanel.add(javax.swing.Box.createVerticalStrut(10));

        // Center Position
        dynamicPanel.add(new JLabel("Center (X, Y, Z):"));
        addVec3Field(sphere.getCenter(), val -> {
            sphere.setCenter(val);
            onUpdate.run();
        });

        // Radius
        dynamicPanel.add(new JLabel("Radius:"));
        addDoubleField(sphere.getRadius(), val -> {
            sphere.setRadius(val);
            onUpdate.run();
        });
    }

    // --- BOX UI LOGIC ---
    private void setupBoxUI(Box box) {
        dynamicPanel.add(new JLabel("Type: Box"));
        dynamicPanel.add(javax.swing.Box.createVerticalStrut(10));

        // Center Position
        dynamicPanel.add(new JLabel("Center (X, Y, Z):"));
        addVec3Field(box.getCenter(), newCenter -> {
            // Move the box while keeping its size
            box.setTransform(newCenter, box.getSize());
            onUpdate.run();
        });

        // Size Dimensions
        dynamicPanel.add(new JLabel("Size (W, H, D):"));
        addVec3Field(box.getSize(), newSize -> {
            // Resize the box relative to its center
            box.setTransform(box.getCenter(), newSize);
            onUpdate.run();
        });
    }

    // --- MATERIAL UI LOGIC ---
    private void setupMaterialUI(Hittable obj) {
        dynamicPanel.add(new JSeparator());
        dynamicPanel.add(javax.swing.Box.createVerticalStrut(10));
        dynamicPanel.add(new JLabel("Material:"));

        Material mat = obj.getMaterial();
        String[] matTypes = {"Lambertian (Matte)", "Metal", "Dielectric (Glass)"};
        materialCombo = new JComboBox<>(matTypes);

        // Select current material type in combobox
        if (mat instanceof Lambertian) materialCombo.setSelectedIndex(0);
        else if (mat instanceof Metal) materialCombo.setSelectedIndex(1);
        else if (mat instanceof Dielectric) materialCombo.setSelectedIndex(2);

        materialCombo.addActionListener(e -> {
            int idx = materialCombo.getSelectedIndex();
            Material newMat = new Lambertian(new Vec3(0.5, 0.5, 0.5)); // Default

            if (idx == 0) newMat = new Lambertian(new Vec3(0.5, 0.5, 0.5));
            else if (idx == 1) newMat = new Metal(new Vec3(0.8, 0.8, 0.8), 0.0);
            else if (idx == 2) newMat = new Dielectric(1.5);

            obj.setMaterial(newMat);
            inspect(obj); // Refresh UI
            onUpdate.run();
        });

        dynamicPanel.add(materialCombo);
        dynamicPanel.add(javax.swing.Box.createVerticalStrut(5));

        if (mat instanceof Lambertian) {
            Lambertian l = (Lambertian) mat;
            dynamicPanel.add(new JLabel("Color (R, G, B):"));

            addVec3Field(l.getAlbedoColor(), col -> {
                l.setAlbedo(col);
                onUpdate.run();
            });
        }
        else if (mat instanceof Metal) {
            Metal m = (Metal) mat;
            dynamicPanel.add(new JLabel("Color (R, G, B):"));
            addVec3Field(m.getAlbedo(), col -> { m.setAlbedo(col); onUpdate.run(); });

            dynamicPanel.add(new JLabel("Fuzziness (0.0 - 1.0):"));
            addDoubleField(m.getFuzz(), f -> { m.setFuzz(f); onUpdate.run(); });
        }
        else if (mat instanceof Dielectric) {
            Dielectric d = (Dielectric) mat;
            dynamicPanel.add(new JLabel("Refraction Index (1.5 = Glass):"));
            addDoubleField(d.getIr(), ir -> { d.setIr(ir); onUpdate.run(); });
        }
    }

    // --- HELPER METHODS ---

    private void addVec3Field(Vec3 value, java.util.function.Consumer<Vec3> onCommit) {
        JPanel p = new JPanel(new GridLayout(1, 3, 5, 0));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JTextField tx = createSmartField(value.x, v -> onCommit.accept(new Vec3(v, value.y, value.z)));
        JTextField ty = createSmartField(value.y, v -> onCommit.accept(new Vec3(value.x, v, value.z)));
        JTextField tz = createSmartField(value.z, v -> onCommit.accept(new Vec3(value.x, value.y, v)));

        p.add(tx); p.add(ty); p.add(tz);
        dynamicPanel.add(p);
        dynamicPanel.add(javax.swing.Box.createVerticalStrut(5));
    }

    private void addDoubleField(double value, java.util.function.Consumer<Double> onCommit) {
        JTextField t = createSmartField(value, onCommit);
        dynamicPanel.add(t);
        dynamicPanel.add(javax.swing.Box.createVerticalStrut(5));
    }

    private JTextField createSmartField(double val, java.util.function.Consumer<Double> onCommit) {
        JTextField field = new JTextField(String.format(Locale.US, "%.2f", val));
        field.addActionListener(e -> {
            try { onCommit.accept(Double.parseDouble(field.getText())); } catch (Exception ex) {}
        });
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                try { onCommit.accept(Double.parseDouble(field.getText())); } catch (Exception ex) {}
            }
        });
        return field;
    }
}