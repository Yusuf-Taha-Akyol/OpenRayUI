package com.taha.openrayui.ui;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.model.Scene;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Displays a list of all objects in the scene.
 * Allows the user to select an object to edit in the Inspector.
 */
public class OutlinerPanel extends JPanel {

    private final JList<Hittable> objectList;

    public OutlinerPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 0)); // Fixed width for sidebar

        // --- STYLING ---
        setBorder(new CompoundBorder(
                new TitledBorder("Scene Outliner"),
                new EmptyBorder(5, 5, 5, 5)
        ));

        // --- LIST COMPONENT ---
        // Connect directly to the Scene's singleton UI list model
        objectList = new JList<>(Scene.getInstance().getUiListModel());
        objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add to ScrollPane to handle many objects
        add(new JScrollPane(objectList), BorderLayout.CENTER);

        // --- BUTTONS (Placeholder for future Add/Remove functionality) ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton addBtn = new JButton("+");
        JButton removeBtn = new JButton("-");

        buttonPanel.add(addBtn);
        buttonPanel.add(removeBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Exposes the JList component so MainFrame can add selection listeners.
     */
    public JList<Hittable> getList() {
        return objectList;
    }
}