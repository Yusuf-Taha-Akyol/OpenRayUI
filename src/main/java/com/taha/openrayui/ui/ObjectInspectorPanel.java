package com.taha.openrayui.ui;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.math.Vec3;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.DoubleConsumer;

/**
 * A panel that displays and edits properties of the selected object.
 * Currently supports 'Sphere' objects.
 */
public class ObjectInspectorPanel extends JPanel {

    private final Runnable onSceneUpdate; // Callback to trigger re-render on change
    private JPanel contentPanel;

    // We don't store the object permanently, we just inspect what is passed

    public ObjectInspectorPanel(Runnable onSceneUpdate) {
        this.onSceneUpdate = onSceneUpdate;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initial state: No selection
        JLabel emptyLabel = new JLabel("No object selected", SwingConstants.CENTER);
        emptyLabel.setForeground(Color.GRAY);
        add(emptyLabel, BorderLayout.CENTER);
    }

    /**
     * Updates the panel to show properties of the given object.
     * @param object The object selected in the Outliner.
     */
    public void inspect(Hittable object) {
        removeAll(); // Clear previous UI

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
     * Builds the UI controls specific to Sphere objects.
     */
    private void buildSphereUI(Sphere sphere) {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        addHeader("Sphere Properties");

        // --- NAME FIELD ---
        addLabel("Name:");
        JTextField nameField = new JTextField(sphere.getName());
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameField.addActionListener(e -> {
            sphere.setName(nameField.getText());
            // Note: The list might need a repaint to show the new name,
            // but the internal data is updated.
        });
        addComponent(nameField);

        // --- POSITION FIELDS ---
        addSeparator();
        addLabel("Position (X, Y, Z):");

        // X Coordinate
        addSmartField(sphere.getCenter().x, val -> {
            Vec3 c = sphere.getCenter();
            sphere.setCenter(new Vec3(val, c.y, c.z));
            onSceneUpdate.run();
        });

        // Y Coordinate
        addSmartField(sphere.getCenter().y, val -> {
            Vec3 c = sphere.getCenter();
            sphere.setCenter(new Vec3(c.x, val, c.z));
            onSceneUpdate.run();
        });

        // Z Coordinate
        addSmartField(sphere.getCenter().z, val -> {
            Vec3 c = sphere.getCenter();
            sphere.setCenter(new Vec3(c.x, c.y, val));
            onSceneUpdate.run();
        });

        // --- RADIUS FIELD ---
        addSeparator();
        addLabel("Radius:");
        addSmartField(sphere.getRadius(), val -> {
            sphere.setRadius(val);
            onSceneUpdate.run();
        });

        // Add the content panel to the top of the layout
        add(contentPanel, BorderLayout.NORTH);
    }

    // --- UI HELPER METHODS ---

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

    // Creates a text field that updates the value when Enter is pressed or focus is lost
    private void addSmartField(double val, DoubleConsumer action) {
        JTextField field = new JTextField(String.valueOf(val));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        Runnable update = () -> {
            try {
                double d = Double.parseDouble(field.getText());
                action.accept(d);
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        };

        field.addActionListener(e -> update.run());
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                update.run();
            }
        });

        contentPanel.add(field);
        contentPanel.add(Box.createVerticalStrut(5));
    }
}