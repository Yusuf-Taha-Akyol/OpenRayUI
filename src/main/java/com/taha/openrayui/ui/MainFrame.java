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

    public MainFrame(Runnable onRenderRequest) {
        // --- Window Settings ---
        setTitle("OpenRayUI - Java Ray Tracer Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(800, 600));

        setLayout(new BorderLayout());

        // --- 1. Render Area ---
        renderPanel = new RenderPanel(800, 450);
        RenderSettings.getInstance().imageWidth = 800;
        RenderSettings.getInstance().imageHeight = 450;

        // --- Mouse Control ---
        CameraInputHandler cameraController = new CameraInputHandler(
                onRenderRequest,
                onRenderRequest
        );
        renderPanel.addMouseListener(cameraController);
        renderPanel.addMouseMotionListener(cameraController);
        renderPanel.addMouseWheelListener(cameraController);

        add(new JScrollPane(renderPanel), BorderLayout.CENTER);

        // --- SAVE LOGIC (NEW) ---
        // This runnable defines what happens when "SAVE IMAGE" is clicked.
        Runnable onSaveRequest = () -> saveRenderedImage();

        // --- 2. Settings Panel ---
        // We pass both the Render trigger and the Save trigger
        settingsPanel = new SettingsPanel(onRenderRequest, onSaveRequest);
        add(settingsPanel, BorderLayout.EAST);

        // --- Finalize ---
        pack();
        setLocationRelativeTo(null);
    }

    public RenderPanel getRenderPanel() {
        return renderPanel;
    }

    /**
     * Opens a file chooser dialog and saves the current render as a PNG file.
     */
    private void saveRenderedImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Render Output");

        // Set default file name
        fileChooser.setSelectedFile(new File("render_output.png"));

        // Filter to only show PNG files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
        fileChooser.setFileFilter(filter);

        // Show "Save" dialog
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Ensure the file has a .png extension
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }

            try {
                // Get the image directly from the panel and write it to disk
                BufferedImage image = renderPanel.getImage();
                ImageIO.write(image, "png", fileToSave);

                JOptionPane.showMessageDialog(this,
                        "Image saved successfully!\n" + fileToSave.getAbsolutePath(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}
