package com.taha.openrayui.ui.components;

import com.taha.openrayui.math.Vec3;
import com.taha.openrayui.core.RenderSettings;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Locale;
import java.util.function.DoubleConsumer;

public class SettingsPanel extends JPanel {

    // Class-level references to update them later
    private JTextField camXField;
    private JTextField camYField;
    private JTextField camZField;

    public SettingsPanel(Runnable onRenderTrigger, Runnable onSaveTrigger) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // --- STYLING ---
        setBorder(new CompoundBorder(
                new TitledBorder("Control Panel"),
                new EmptyBorder(10, 10, 10, 10)
        ));
        setPreferredSize(new Dimension(280, 0));

        // --- SAMPLE COUNT ---
        addLabel("Quality (Sample):");
        JSpinner sampleSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 10));
        sampleSpinner.addChangeListener(e -> {
            RenderSettings.getInstance().samplesPerPixel = (int) sampleSpinner.getValue();
        });
        addComponent(sampleSpinner);

        // --- DEPTH ---
        addLabel("Max Bounces (Depth):");
        JSpinner depthSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 100, 5));
        depthSpinner.addChangeListener(e -> {
            RenderSettings.getInstance().maxDepth = (int) depthSpinner.getValue();
        });
        addComponent(depthSpinner);

        addSeparator();
        addLabel("Camera Position (X, Y, Z):");

        // --- CAMERA INPUTS ---
        // We create specific fields for X, Y, Z and store references
        camXField = createSmartTextField(0.0, val -> updateCameraX(val));
        camYField = createSmartTextField(0.0, val -> updateCameraY(val));
        camZField = createSmartTextField(1.0, val -> updateCameraZ(val));

        // Add them to the panel
        addComponent(camXField);
        addComponent(camYField);
        addComponent(camZField);

        // Push buttons to the bottom
        add(Box.createVerticalGlue());
        addSeparator();

        // --- RENDER BUTTON ---
        JButton renderBtn = new JButton("RENDER SCENE");
        renderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        renderBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        renderBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        renderBtn.addActionListener(e -> onRenderTrigger.run());
        add(renderBtn);

        add(Box.createVerticalStrut(10));

        // --- SAVE BUTTON ---
        JButton saveBtn = new JButton("SAVE IMAGE");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        saveBtn.addActionListener(e -> onSaveTrigger.run());
        add(saveBtn);

        // Initial sync
        updateCameraFields();
    }

    /**
     * Updates the text fields with the current values from RenderSettings.
     * Called by MainFrame when the mouse moves the camera.
     */
    public void updateCameraFields() {
        Vec3 pos = RenderSettings.getInstance().lookFrom;

        // Using Locale.US to ensure dot (.) is used instead of comma (,)
        camXField.setText(String.format(Locale.US, "%.2f", pos.x));
        camYField.setText(String.format(Locale.US, "%.2f", pos.y));
        camZField.setText(String.format(Locale.US, "%.2f", pos.z));
    }

    // --- Helper Methods ---

    private void updateCameraX(double val) {
        Vec3 old = RenderSettings.getInstance().lookFrom;
        RenderSettings.getInstance().lookFrom = new Vec3(val, old.y, old.z);
    }

    private void updateCameraY(double val) {
        Vec3 old = RenderSettings.getInstance().lookFrom;
        RenderSettings.getInstance().lookFrom = new Vec3(old.x, val, old.z);
    }

    private void updateCameraZ(double val) {
        Vec3 old = RenderSettings.getInstance().lookFrom;
        RenderSettings.getInstance().lookFrom = new Vec3(old.x, old.y, val);
    }

    private void addLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label);
        add(Box.createVerticalStrut(5));
    }

    private void addComponent(JComponent comp) {
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        add(comp);
        add(Box.createVerticalStrut(15));
    }

    private void addSeparator() {
        add(Box.createVerticalStrut(10));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        add(sep);
        add(Box.createVerticalStrut(10));
    }

    // Modified to return the JTextField instead of adding it directly
    private JTextField createSmartTextField(double defaultValue, DoubleConsumer onUpdate) {
        JTextField field = new JTextField(String.valueOf(defaultValue));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        Runnable updateAction = () -> {
            try {
                double val = Double.parseDouble(field.getText());
                onUpdate.accept(val);
            } catch (NumberFormatException ex) {
                // Ignore invalid input
            }
        };

        field.addActionListener(e -> updateAction.run());
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                updateAction.run();
            }
        });

        return field;
    }
}