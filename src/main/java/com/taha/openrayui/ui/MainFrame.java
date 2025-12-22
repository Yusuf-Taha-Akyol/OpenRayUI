package com.taha.openrayui.ui;

import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.model.Scene;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

/**
 * The primary window of the OpenRayUI application.
 * acts as the central hub connecting the Render Engine, UI Panels, and Project Management.
 * * Layout Overview:
 * - NORTH: Menu Bar (File Operations)
 * - CENTER: Render Panel (3D Viewport)
 * - WEST: Outliner Panel (Scene Graph/List)
 * - EAST: Settings & Inspector Tabs (Properties)
 */
public class MainFrame extends JFrame {

    // --- UI Components ---
    private final RenderPanel renderPanel;         // Displays the ray-traced image
    private final SettingsPanel settingsPanel;     // Controls render quality & camera
    private final OutlinerPanel outlinerPanel;     // Lists objects in the scene
    private final ObjectInspectorPanel inspectorPanel; // Edits selected object properties

    /**
     * Constructs the main application window and initializes all sub-systems.
     * * @param onRenderRequest A callback runnable that triggers a new render frame.
     * Passed down to child panels so they can request updates.
     */
    public MainFrame(Runnable onRenderRequest) {
        // --- 1. Window Configuration ---
        setTitle("OpenRayUI - Java Ray Tracer Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        // Set a minimum size to ensure all panels remain usable
        setMinimumSize(new Dimension(1000, 600));

        // --- 2. Menu System Setup ---
        // Creates the top menu bar for file operations (New, Save, Open)
        setJMenuBar(createMenuBar(onRenderRequest));

        // Use BorderLayout to manage the main sections of the UI
        setLayout(new BorderLayout());

        // --- 3. CENTER: Render Viewport ---
        // The main area where the ray tracing happens
        renderPanel = new RenderPanel(800, 450);
        RenderSettings.getInstance().imageWidth = 800;
        RenderSettings.getInstance().imageHeight = 450;

        // Attach mouse controllers for "Orbit" and "Zoom" functionality
        CameraInputHandler cameraController = new CameraInputHandler(onRenderRequest, onRenderRequest);
        renderPanel.addMouseListener(cameraController);
        renderPanel.addMouseMotionListener(cameraController);
        renderPanel.addMouseWheelListener(cameraController);

        // Add to a ScrollPane to handle cases where the image is larger than the window
        add(new JScrollPane(renderPanel), BorderLayout.CENTER);

        // --- 4. WEST: Scene Outliner ---
        // The sidebar listing all objects. We pass 'onRenderRequest' so adding/removing objects updates the view.
        outlinerPanel = new OutlinerPanel(onRenderRequest);
        add(outlinerPanel, BorderLayout.WEST);

        // --- 5. EAST: Properties & Settings ---
        // A TabbedPane allows switching between global Render Settings and specific Object Properties
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 0)); // Fixed width for the sidebar

        // Tab 1: Global Render Settings (Quality, Camera Coords)
        settingsPanel = new SettingsPanel(onRenderRequest, this::saveRenderedImage);
        tabbedPane.addTab("Render", settingsPanel);

        // Tab 2: Object Inspector (Material, Position, Color)
        inspectorPanel = new ObjectInspectorPanel(onRenderRequest);
        tabbedPane.addTab("Object", inspectorPanel);

        add(tabbedPane, BorderLayout.EAST);

        // --- 6. Event Wiring (Inter-Panel Communication) ---
        // CRITICAL: Connect the Outliner selection to the Inspector panel.
        // When a user selects an object in the list (West), show its properties in the Inspector (East).
        outlinerPanel.getList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // Pass the selected object to the inspector
                inspectorPanel.inspect(outlinerPanel.getList().getSelectedValue());

                // Automatically switch focus to the 'Object' tab if an object is selected
                if (outlinerPanel.getList().getSelectedValue() != null) {
                    tabbedPane.setSelectedIndex(1);
                }
            }
        });

        // --- 7. Finalize Window ---
        pack(); // Adjust window size to fit components
        setLocationRelativeTo(null); // Center window on screen
    }

    /**
     * Exposes the RenderPanel for the main application loop to update the image.
     */
    public RenderPanel getRenderPanel() {
        return renderPanel;
    }

    // ============================================================================================
    // MENU BAR & FILE OPERATIONS
    // ============================================================================================

    /**
     * Creates the main menu bar with "File" options.
     */
    private JMenuBar createMenuBar(Runnable onRenderRequest) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        // Option: New Project (Clears the scene)
        JMenuItem newItem = new JMenuItem("New Project");
        newItem.addActionListener(e -> {
            Scene.getInstance().clear(); // Wipe data
            onRenderRequest.run();       // Refresh view
        });

        // Option: Open Project (Loads .ray file)
        JMenuItem openItem = new JMenuItem("Open Project...");
        openItem.addActionListener(e -> openProject(onRenderRequest));

        // Option: Save Project (Saves .ray file)
        JMenuItem saveItem = new JMenuItem("Save Project...");
        saveItem.addActionListener(e -> saveProject());

        // Option: Export Image (Saves .png)
        JMenuItem saveImgItem = new JMenuItem("Export Image (PNG)...");
        saveImgItem.addActionListener(e -> saveRenderedImage());

        // Assemble the menu
        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(saveImgItem);

        menuBar.add(fileMenu);
        return menuBar;
    }

    /**
     * Serializes the current Scene (HittableList) to a file (.ray).
     * This allows the user to save their work and resume later.
     */
    private void saveProject() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Project File");
        fileChooser.setSelectedFile(new File("my_scene.ray"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Ray Tracing Project (.ray)", "ray"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // Ensure correct extension
            if (!file.getName().endsWith(".ray")) {
                file = new File(file.getAbsolutePath() + ".ray");
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                // Serialize only the World object (HittableList)
                // Note: All objects inside (Sphere, Material, Vec3) must implement Serializable.
                oos.writeObject(Scene.getInstance().getWorld());
                JOptionPane.showMessageDialog(this, "Project saved successfully!");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving project: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Deserializes a Scene from a file and replaces the current world.
     */
    private void openProject(Runnable onRenderRequest) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Project File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Ray Tracing Project (.ray)", "ray"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                // Read the list from file
                HittableList loadedWorld = (HittableList) ois.readObject();

                // Update the Singleton Scene with new data
                Scene.getInstance().loadSceneFromList(loadedWorld);

                // Trigger a re-render to show the loaded scene
                onRenderRequest.run();
                JOptionPane.showMessageDialog(this, "Project loaded successfully!");

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading project: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Exports the currently rendered buffer to a PNG image file.
     */
    private void saveRenderedImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Render Output");
        fileChooser.setSelectedFile(new File("render_output.png"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }
            try {
                // Write the BufferedImage from RenderPanel to disk
                ImageIO.write(renderPanel.getImage(), "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Image exported: " + fileToSave.getName());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}