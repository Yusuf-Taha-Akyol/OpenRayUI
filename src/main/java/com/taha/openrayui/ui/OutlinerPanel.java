package com.taha.openrayui.ui;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.material.Lambertian;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.model.Scene;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Sidebar panel displaying the list of scene objects.
 * Allows adding new objects and removing selected ones.
 */
public class OutlinerPanel extends JPanel {

    private final JList<Hittable> objectList;

    // Callback to trigger a re-render when the scene structure changes
    private Runnable onSceneChange;

    /**
     * Default constructor.
     * Note: Use setOnSceneChange() to attach the render trigger later.
     */
    public OutlinerPanel() {
        this(null);
    }

    public OutlinerPanel(Runnable onSceneChange) {
        this.onSceneChange = onSceneChange;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 0));

        // --- STYLING ---
        setBorder(new CompoundBorder(
                new TitledBorder("Scene Outliner"),
                new EmptyBorder(5, 5, 5, 5)
        ));

        // --- LIST COMPONENT ---
        objectList = new JList<>(Scene.getInstance().getUiListModel());
        objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(objectList), BorderLayout.CENTER);

        // --- BUTTONS ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton addBtn = new JButton("+");
        JButton removeBtn = new JButton("-");

        // Add listeners
        addBtn.addActionListener(e -> addNewSphere());
        removeBtn.addActionListener(e -> removeSelectedObject());

        buttonPanel.add(addBtn);
        buttonPanel.add(removeBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets the callback to run when objects are added or removed.
     */
    public void setOnSceneChange(Runnable onSceneChange) {
        this.onSceneChange = onSceneChange;
    }

    public JList<Hittable> getList() {
        return objectList;
    }

    /**
     * Creates a new default sphere and adds it to the scene.
     */
    private void addNewSphere() {
        // Create a default grey sphere at origin
        Sphere newSphere = new Sphere(
                new Vec3(0, 0, 0),
                0.5,
                new Lambertian(new Vec3(0.5, 0.5, 0.5))
        );
        newSphere.setName("New Sphere");

        // Add to singleton scene
        Scene.getInstance().addObject(newSphere);

        // Select the new object in the UI
        objectList.setSelectedValue(newSphere, true);

        // Trigger render update
        if (onSceneChange != null) onSceneChange.run();
    }

    /**
     * Removes the currently selected object from the scene.
     */
    private void removeSelectedObject() {
        Hittable selected = objectList.getSelectedValue();
        if (selected != null) {
            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete '" + selected.getName() + "'?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                Scene.getInstance().removeObject(selected);
                objectList.clearSelection(); // Clear selection to prevent errors

                // Trigger render update
                if (onSceneChange != null) onSceneChange.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select an object to delete.");
        }
    }
}