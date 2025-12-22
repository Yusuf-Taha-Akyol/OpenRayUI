package com.taha.openrayui.ui.components;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.material.Dielectric;
import com.taha.openrayui.material.Lambertian;
import com.taha.openrayui.material.Material;
import com.taha.openrayui.material.Metal;
import com.taha.openrayui.math.Vec3;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.DoubleConsumer;
import java.util.function.Consumer;

/**
 * A robust property editor panel for scene objects.
 * Allows users to modify position, size, and material properties (Color, Type, etc.) in real-time.
 */
public class ObjectInspectorPanel extends JPanel {

    // Callback to trigger a re-render whenever a property changes
    private final Runnable onSceneUpdate;
    private JPanel contentPanel;

    public ObjectInspectorPanel(Runnable onSceneUpdate) {
        this.onSceneUpdate = onSceneUpdate;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initial state: Display a message when no object is selected
        add(new JLabel("No object selected", SwingConstants.CENTER), BorderLayout.CENTER);
    }

    /**
     * Inspects the given object and builds the corresponding UI.
     * @param object The selected scene object (e.g., Sphere).
     */
    public void inspect(Hittable object) {
        removeAll(); // Clear previous UI elements

        if (object == null) {
            add(new JLabel("No object selected", SwingConstants.CENTER));
        } else if (object instanceof Sphere) {
            buildSphereUI((Sphere) object);
        } else {
            add(new JLabel("Unknown Object Type", SwingConstants.CENTER));
        }

        revalidate();
        repaint();
    }

    /**
     * Builds the specific UI controls for a Sphere object.
     */
    private void buildSphereUI(Sphere sphere) {
        // Main container for the properties, stacked vertically
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // --- SECTION: BASIC PROPERTIES ---
        addHeader("Sphere Properties");

        // 1. Name Field
        addLabel("Name:");
        JTextField nameField = new JTextField(sphere.getName());
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameField.addActionListener(e -> sphere.setName(nameField.getText()));
        addComponent(nameField);

        // 2. Position Fields (X, Y, Z)
        addSeparator();
        addLabel("Position (X, Y, Z):");

        // Update X
        addSmartField(sphere.getCenter().x, val -> {
            sphere.setCenter(new Vec3(val, sphere.getCenter().y, sphere.getCenter().z));
            onSceneUpdate.run();
        });
        // Update Y
        addSmartField(sphere.getCenter().y, val -> {
            sphere.setCenter(new Vec3(sphere.getCenter().x, val, sphere.getCenter().z));
            onSceneUpdate.run();
        });
        // Update Z
        addSmartField(sphere.getCenter().z, val -> {
            sphere.setCenter(new Vec3(sphere.getCenter().x, sphere.getCenter().y, val));
            onSceneUpdate.run();
        });

        // 3. Radius Field
        addSeparator();
        addLabel("Radius:");
        addSmartField(sphere.getRadius(), val -> {
            sphere.setRadius(val);
            onSceneUpdate.run();
        });

        // --- SECTION: MATERIAL PROPERTIES ---
        addSeparator();
        addHeader("Material");

        // 4. Material Type Dropdown
        String[] types = {"Lambertian (Matte)", "Metal (Shiny)", "Dielectric (Glass)"};
        JComboBox<String> typeBox = new JComboBox<>(types);

        // Select the current material type in the dropdown
        if (sphere.getMaterial() instanceof Lambertian) typeBox.setSelectedIndex(0);
        else if (sphere.getMaterial() instanceof Metal) typeBox.setSelectedIndex(1);
        else if (sphere.getMaterial() instanceof Dielectric) typeBox.setSelectedIndex(2);

        // Handle Material Change
        typeBox.addActionListener(e -> {
            int idx = typeBox.getSelectedIndex();
            Material newMat;

            // Create a new default material based on selection
            if (idx == 0) newMat = new Lambertian(new Vec3(0.5, 0.5, 0.5)); // Grey Matte
            else if (idx == 1) newMat = new Metal(new Vec3(0.8, 0.8, 0.8), 0.1); // Shiny Metal
            else newMat = new Dielectric(1.5); // Standard Glass

            sphere.setMaterial(newMat);

            // Rebuild the UI to show specific properties for the new material
            inspect(sphere);
            onSceneUpdate.run();
        });
        addComponent(typeBox);

        // 5. Dynamic Material Properties (Color, Fuzz, Refraction)
        Material mat = sphere.getMaterial();

        if (mat instanceof Lambertian) {
            Lambertian lamb = (Lambertian) mat;
            // Color Picker for Matte
            addColorPicker("Color:", lamb.getAlbedo(), c -> {
                lamb.setAlbedo(c);
                onSceneUpdate.run();
            });
        }
        else if (mat instanceof Metal) {
            Metal met = (Metal) mat;
            // Color Picker for Metal
            addColorPicker("Color:", met.getAlbedo(), c -> {
                met.setAlbedo(c);
                onSceneUpdate.run();
            });
            // Fuzziness Slider/Field
            addLabel("Fuzziness (0.0 = Mirror, 1.0 = Matte):");
            addSmartField(met.getFuzz(), val -> {
                met.setFuzz(val);
                onSceneUpdate.run();
            });
        }
        else if (mat instanceof Dielectric) {
            Dielectric diel = (Dielectric) mat;
            // Refraction Index Field
            addLabel("Refraction Index (1.5 = Glass, 2.4 = Diamond):");
            addSmartField(diel.getIr(), val -> {
                diel.setIr(val);
                onSceneUpdate.run();
            });
        }

        // Add the content panel to the top of the layout
        add(contentPanel, BorderLayout.NORTH);
    }

    // --- UI HELPER METHODS ---

    /**
     * Adds a button that opens a color chooser dialog.
     */
    private void addColorPicker(String label, Vec3 colorVec, Consumer<Vec3> onPick) {
        addLabel(label);

        // Convert Vec3 (0.0-1.0) to Java AWT Color (0-255)
        Color initialColor = new Color(
                (float) Math.min(colorVec.x, 1.0),
                (float) Math.min(colorVec.y, 1.0),
                (float) Math.min(colorVec.z, 1.0)
        );

        JButton colorBtn = new JButton("Change Color");
        colorBtn.setBackground(initialColor);
        // Ensure text is visible based on background brightness
        colorBtn.setForeground(isBright(initialColor) ? Color.BLACK : Color.WHITE);

        colorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Select Material Color", initialColor);
            if (newColor != null) {
                colorBtn.setBackground(newColor);
                colorBtn.setForeground(isBright(newColor) ? Color.BLACK : Color.WHITE);

                // Convert back to Vec3 and trigger callback
                onPick.accept(new Vec3(
                        newColor.getRed() / 255.0,
                        newColor.getGreen() / 255.0,
                        newColor.getBlue() / 255.0
                ));
            }
        });

        addComponent(colorBtn);
    }

    // Determines if a color is bright to set contrasting text color
    private boolean isBright(Color c) {
        return (c.getRed() + c.getGreen() + c.getBlue()) / 3 > 128;
    }

    private void addHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(label);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    private void addLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(label);
        contentPanel.add(Box.createVerticalStrut(5));
    }

    private void addComponent(JComponent comp) {
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(comp);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    private void addSeparator() {
        contentPanel.add(Box.createVerticalStrut(5));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        contentPanel.add(sep);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    /**
     * Creates a text field that triggers an action when Enter is pressed or focus is lost.
     */
    private void addSmartField(double val, DoubleConsumer action) {
        JTextField field = new JTextField(String.valueOf(val));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        Runnable update = () -> {
            try {
                action.accept(Double.parseDouble(field.getText()));
            } catch (NumberFormatException ignored) {
                // Ignore invalid input (keep old value)
            }
        };

        field.addActionListener(e -> update.run());
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { update.run(); }
        });

        contentPanel.add(field);
        contentPanel.add(Box.createVerticalStrut(5));
    }
}