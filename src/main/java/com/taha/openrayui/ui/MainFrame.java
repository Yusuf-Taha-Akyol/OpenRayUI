package com.taha.openrayui.ui;

import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.model.Scene;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

/**
 * The main application window for OpenRayUI.
 * It acts as the central hub connecting the Rendering Engine, UI Panels, and User Inputs.
 * * Layout Structure:
 * - NORTH: Menu Bar (File Operations)
 * - CENTER: Render Panel (3D Viewport with HUD Zoom Buttons)
 * - WEST: Outliner Panel (Scene Graph)
 * - EAST: Settings & Inspector Tabs (Properties)
 */
public class MainFrame extends JFrame {

    // --- UI Components ---
    private final RenderPanel renderPanel;           // Displays the ray-traced image
    private final SettingsPanel settingsPanel;       // Global render settings (Quality, Camera)
    private final OutlinerPanel outlinerPanel;       // List of objects in the scene
    private final ObjectInspectorPanel inspectorPanel; // Editor for selected object properties

    // --- Controllers ---
    private final CameraInputHandler cameraController; // Handles mouse inputs for camera movement

    /**
     * Constructs the main window and initializes the UI layout.
     * @param onRenderRequest A callback to trigger a new render frame (passed to child panels).
     */
    public MainFrame(Runnable onRenderRequest) {
        // --- 1. Window Configuration ---
        setTitle("OpenRayUI - Java Ray Tracer Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(1000, 600));

        // --- 2. Menu Bar Setup ---
        setJMenuBar(createMenuBar(onRenderRequest));

        // Use BorderLayout to organize the main panels
        setLayout(new BorderLayout());

        // --- 3. Initialize Panels (Order Matters!) ---

        // A. Settings Panel (Right Side)
        // We initialize this FIRST because the camera controller needs to update its fields.
        settingsPanel = new SettingsPanel(onRenderRequest, this::saveRenderedImage);

        // B. Render Viewport (Center)
        renderPanel = new RenderPanel(800, 450);
        renderPanel.setLayout(new GridBagLayout()); // Use GridBag for overlaying HUD buttons

        RenderSettings.getInstance().imageWidth = 800;
        RenderSettings.getInstance().imageHeight = 450;

        // --- 4. Camera Controller & Sync Logic ---
        // We define a wrapper action that:
        // 1. Updates the UI text fields in SettingsPanel (Two-way binding)
        // 2. Triggers a new render
        Runnable onCameraMove = () -> {
            settingsPanel.updateCameraFields(); // Sync UI with internal camera state
            onRenderRequest.run();              // Re-render scene
        };

        // Initialize controller with the sync action
        cameraController = new CameraInputHandler(onCameraMove, onCameraMove);

        // Attach listeners to the render panel for mouse interaction
        renderPanel.addMouseListener(cameraController);
        renderPanel.addMouseMotionListener(cameraController);
        renderPanel.addMouseWheelListener(cameraController);

        // --- 5. HUD: Zoom Buttons ---
        // Add floating buttons (+) and (-) to the render panel
        setupZoomButtons();

        // Add RenderPanel to a ScrollPane (Center)
        add(new JScrollPane(renderPanel), BorderLayout.CENTER);

        // --- 6. WEST: Scene Outliner ---
        outlinerPanel = new OutlinerPanel(onRenderRequest);
        add(outlinerPanel, BorderLayout.WEST);

        // --- 7. EAST: Tabbed Properties ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 0)); // Fixed width sidebar

        // Tab 1: Global Render Settings (Already created above)
        tabbedPane.addTab("Render", settingsPanel);

        // Tab 2: Object Inspector
        inspectorPanel = new ObjectInspectorPanel(onRenderRequest);
        tabbedPane.addTab("Object", inspectorPanel);

        add(tabbedPane, BorderLayout.EAST);

        // --- 8. Event Wiring ---
        // Link the Outliner selection to the Inspector
        outlinerPanel.getList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // Pass selected object to inspector
                inspectorPanel.inspect(outlinerPanel.getList().getSelectedValue());

                // Auto-switch to Object tab if something is selected
                if (outlinerPanel.getList().getSelectedValue() != null) {
                    tabbedPane.setSelectedIndex(1);
                }
            }
        });

        // --- 9. Finalize Window ---
        pack(); // Adjust size to fit components
        setLocationRelativeTo(null); // Center on screen
    }

    /**
     * Creates floating zoom buttons on the bottom-right of the render panel.
     * Uses GridBagLayout to position them as an overlay (HUD).
     */
    private void setupZoomButtons() {
        JPanel hudPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        hudPanel.setOpaque(false); // Transparent background

        // Zoom In Button (+)
        JButton zoomInBtn = createHudButton("+");
        zoomInBtn.addActionListener(e -> cameraController.zoomIn());

        // Zoom Out Button (-)
        JButton zoomOutBtn = createHudButton("-");
        zoomOutBtn.addActionListener(e -> cameraController.zoomOut());

        hudPanel.add(zoomInBtn);
        hudPanel.add(zoomOutBtn);

        // Positioning logic (Bottom-Right Corner)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Push to right edge
        gbc.weighty = 1.0; // Push to bottom edge
        gbc.anchor = GridBagConstraints.LAST_LINE_END; // Anchor to bottom-right
        gbc.insets = new Insets(0, 0, 10, 10); // Padding

        renderPanel.add(hudPanel, gbc);
    }

    /**
     * Helper to style the HUD buttons (Semi-transparent, Monospaced font).
     */
    private JButton createHudButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public RenderPanel getRenderPanel() {
        return renderPanel;
    }

    // ============================================================================================
    // MENU BAR & FILE I/O OPERATIONS
    // ============================================================================================

    /**
     * Creates the top menu bar with File operations.
     */
    private JMenuBar createMenuBar(Runnable onRenderRequest) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        // NEW PROJECT: Clears the scene
        JMenuItem newItem = new JMenuItem("New Project");
        newItem.addActionListener(e -> {
            Scene.getInstance().clear();
            onRenderRequest.run();
        });

        // OPEN PROJECT: Loads .ray file
        JMenuItem openItem = new JMenuItem("Open Project...");
        openItem.addActionListener(e -> openProject(onRenderRequest));

        // SAVE PROJECT: Saves .ray file
        JMenuItem saveItem = new JMenuItem("Save Project...");
        saveItem.addActionListener(e -> saveProject());

        // EXPORT IMAGE: Saves .png
        JMenuItem saveImgItem = new JMenuItem("Export Image (PNG)...");
        saveImgItem.addActionListener(e -> saveRenderedImage());

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
     * Serializes the current scene to a file using Java Serialization.
     */
    private void saveProject() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Project File");
        fileChooser.setSelectedFile(new File("my_scene.ray"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Ray Tracing Project (.ray)", "ray"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".ray")) {
                file = new File(file.getAbsolutePath() + ".ray");
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(Scene.getInstance().getWorld());
                JOptionPane.showMessageDialog(this, "Project saved successfully!");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving project: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Deserializes a scene from a file and updates the application state.
     */
    private void openProject(Runnable onRenderRequest) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Project File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Ray Tracing Project (.ray)", "ray"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                HittableList loadedWorld = (HittableList) ois.readObject();
                Scene.getInstance().loadSceneFromList(loadedWorld);
                onRenderRequest.run();
                JOptionPane.showMessageDialog(this, "Project loaded successfully!");
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading project: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Exports the current render buffer to a PNG image.
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
                ImageIO.write(renderPanel.getImage(), "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Image exported: " + fileToSave.getName());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}