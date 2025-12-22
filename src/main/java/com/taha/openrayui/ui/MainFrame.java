package com.taha.openrayui.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame {

    private final RenderPanel renderPanel;
    private final SettingsPanel settingsPanel;
    private final OutlinerPanel outlinerPanel;
    private final ObjectInspectorPanel inspectorPanel; // Reference to the new Inspector

    public MainFrame(Runnable onRenderRequest) {
        // --- WINDOW SETTINGS ---
        setTitle("OpenRayUI - Java Ray Tracer Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(1000, 600)); // Increased width for the new panels

        setLayout(new BorderLayout());

        // --- 1. CENTER: Render Area ---
        renderPanel = new RenderPanel(800, 450);
        RenderSettings.getInstance().imageWidth = 800;
        RenderSettings.getInstance().imageHeight = 450;

        // Setup Mouse Control (Orbit/Zoom)
        CameraInputHandler cameraController = new CameraInputHandler(onRenderRequest, onRenderRequest);
        renderPanel.addMouseListener(cameraController);
        renderPanel.addMouseMotionListener(cameraController);
        renderPanel.addMouseWheelListener(cameraController);

        add(new JScrollPane(renderPanel), BorderLayout.CENTER);

        // --- 2. WEST: Outliner Panel ---
        outlinerPanel = new OutlinerPanel();
        add(outlinerPanel, BorderLayout.WEST);

        // --- 3. EAST: Tabbed Pane (Settings & Inspector) ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 0)); // Fixed width for right panel

        // Tab 1: Render Settings
        settingsPanel = new SettingsPanel(onRenderRequest, () -> saveRenderedImage());
        tabbedPane.addTab("Render", settingsPanel);

        // Tab 2: Object Inspector
        // We pass 'onRenderRequest' so scene updates trigger a re-render
        inspectorPanel = new ObjectInspectorPanel(onRenderRequest);
        tabbedPane.addTab("Object", inspectorPanel);

        add(tabbedPane, BorderLayout.EAST);

        // --- LINK OUTLINER TO INSPECTOR ---
        // When an object is selected in the list, update the Inspector panel
        outlinerPanel.getList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // Send selected object to inspector
                inspectorPanel.inspect(outlinerPanel.getList().getSelectedValue());

                // Automatically switch to the 'Object' tab for better UX
                if (outlinerPanel.getList().getSelectedValue() != null) {
                    tabbedPane.setSelectedIndex(1);
                }
            }
        });

        // --- FINALIZE ---
        pack();
        setLocationRelativeTo(null);
    }

    public RenderPanel getRenderPanel() {
        return renderPanel;
    }

    /**
     * Opens a file chooser to save the current render as PNG.
     */
    private void saveRenderedImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Render Output");
        fileChooser.setSelectedFile(new File("render_output.png"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }
            try {
                ImageIO.write(renderPanel.getImage(), "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Saved: " + fileToSave.getName());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}