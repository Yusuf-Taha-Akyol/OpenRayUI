package com.taha.openrayui.ui.components;

import com.taha.openrayui.geometry.Box;
import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.geometry.Sphere;
import com.taha.openrayui.material.Lambertian;
import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.model.Scene;
import com.taha.openrayui.texture.CheckerTexture;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class OutlinerPanel extends JPanel {

    private final DefaultListModel<Hittable> model;
    private final JList<Hittable> list;
    private final Runnable onUpdate;

    public OutlinerPanel(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new TitledBorder("Scene Objects"), new EmptyBorder(10, 10, 10, 10)));
        setPreferredSize(new Dimension(200, 0));

        model = new DefaultListModel<>();
        list = new JList<>(model);

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Sphere) {
                    setText("Sphere " + (index + 1));
                } else if (value instanceof Box) {
                    setText("Box " + (index + 1));
                }
                return this;
            }
        });

        add(new JScrollPane(list), BorderLayout.CENTER);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));

        JButton addSphereBtn = new JButton("Sphere");
        addSphereBtn.addActionListener(e -> {
            Sphere s = new Sphere(new Vec3(0, 0, -1), 0.5, new Lambertian(new Vec3(0.5, 0.5, 0.5)));
            Scene.getInstance().getWorld().add(s);
            model.addElement(s);
            onUpdate.run();
        });

        // --- ADD BOX BUTTON (FIXED SCALE) ---
        JButton addBoxBtn = new JButton("Box");
        addBoxBtn.addActionListener(e -> {
            // Scale: 10.0 ensures roughly 3 tiles per unit (Visible Checker Pattern)
            CheckerTexture checker = new CheckerTexture(
                    new Vec3(0.2, 0.3, 0.1), // Dark Green
                    new Vec3(0.9, 0.9, 0.9), // White
                    10.0
            );

            // Create a Cube (1x1x1)
            Box b = new Box(
                    new Vec3(-0.5, -0.5, -2.5), // Min corner
                    new Vec3(0.5, 0.5, -1.5),   // Max corner
                    new Lambertian(checker)     // Assign texture
            );
            b.setName("Checker Cube");

            Scene.getInstance().getWorld().add(b);
            model.addElement(b);
            onUpdate.run();
        });

        JButton deleteBtn = new JButton("Del");
        deleteBtn.addActionListener(e -> {
            Hittable selected = list.getSelectedValue();
            if (selected != null) {
                Scene.getInstance().getWorld().remove(selected);
                model.removeElement(selected);
                onUpdate.run();
            }
        });

        buttonPanel.add(addSphereBtn);
        buttonPanel.add(addBoxBtn);
        buttonPanel.add(deleteBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // SYNC
        syncWithScene();
    }

    private void syncWithScene() {
        model.clear();
        HittableList world = Scene.getInstance().getWorld();
        for (Hittable obj : world.objects) {
            model.addElement(obj);
        }
    }

    public JList<Hittable> getList() {
        return list;
    }
}