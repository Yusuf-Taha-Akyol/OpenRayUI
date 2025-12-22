package com.taha.openrayui.ui;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.geometry.HittableList;
import com.taha.openrayui.model.Scene;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

/**
 * The main application window for OpenRayUI.
 * Acts as the central hub connecting the Rendering Engine, UI Panels, and User Inputs.
 */
public class MainFrame extends JFrame {

    // --- UI Components ---
    private final RenderPanel renderPanel;           // Displays the ray-traced image & Gizmos
    private final SettingsPanel settingsPanel;       // Global render settings (Quality, Camera)
    private final OutlinerPanel outlinerPanel;       // List of objects in the scene
    private final ObjectInspectorPanel inspectorPanel; // Editor for selected object properties

    // --- Controllers ---
    private final CameraInputHandler cameraController;

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

        // =================================================================================
        // 3. INITIALIZE ALL PANELS FIRST (To avoid "variable not initialized" errors)
        // =================================================================================

        // A. Settings Panel
        settingsPanel = new SettingsPanel(onRenderRequest, this::saveRenderedImage);

        // B. Render Viewport
        renderPanel = new RenderPanel(800, 450);
        renderPanel.setLayout(new GridBagLayout()); // Use GridBag for overlaying HUD buttons

        // C. Outliner Panel (CRITICAL: Must be initialized BEFORE controllers!)
        outlinerPanel = new OutlinerPanel(onRenderRequest);

        // D. Inspector Panel
        inspectorPanel = new ObjectInspectorPanel(onRenderRequest);

        // Configure Global Settings
        RenderSettings.getInstance().imageWidth = 800;
        RenderSettings.getInstance().imageHeight = 450;

        // =================================================================================
        // 4. SETUP CONTROLLERS (Now all panels exist)
        // =================================================================================

        // A. Gizmo Controller (Handles Object Manipulation)
        // We can safely pass 'outlinerPanel' now because it was created in step 3.C
        GizmoController gizmoController = new GizmoController(
                renderPanel, outlinerPanel, onRenderRequest, onRenderRequest
        );

        // Define what happens when the Camera moves (Sync UI & Redraw)
        Runnable onCameraMove = () -> {
            settingsPanel.updateCameraFields(); // Sync Text Fields
            onRenderRequest.run();              // Re-render scene
            renderPanel.repaint();              // Redraw Gizmo arrows at new position
        };

        // B. Camera Controller (Handles Navigation)
        // Wraps Gizmo logic to prioritize Object Moving over Camera Moving
        cameraController = new CameraInputHandler(onCameraMove, onCameraMove) {
            @Override
            public void mousePressed(MouseEvent e) {
                // Try Gizmo first
                gizmoController.mousePressed(e);

                // If Gizmo didn't consume the click, allow Camera orbit
                if (!gizmoController.isInteracting()) {
                    super.mousePressed(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                gizmoController.mouseReleased(e);
                super.mouseReleased(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (gizmoController.isInteracting()) {
                    gizmoController.mouseDragged(e); // Move Object
                } else {
                    super.mouseDragged(e); // Move Camera
                }
            }
        };

        // Attach Controller to RenderPanel
        renderPanel.addMouseListener(cameraController);
        renderPanel.addMouseMotionListener(cameraController);
        renderPanel.addMouseWheelListener(cameraController);

        // --- 5. SETUP LAYOUT (Place panels on screen) ---

        // CENTER: Render View (with HUD Buttons)
        setupZoomButtons(); // Add buttons to renderPanel
        add(new JScrollPane(renderPanel), BorderLayout.CENTER);

        // WEST: Outliner (Already created, just adding it)
        add(outlinerPanel, BorderLayout.WEST);

        // EAST: Tabbed Properties
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 0));

        tabbedPane.addTab("Render", settingsPanel);
        tabbedPane.addTab("Object", inspectorPanel);

        add(tabbedPane, BorderLayout.EAST);

        // --- 6. EVENT WIRING ---
        // Link Outliner selection to Inspector and RenderPanel
        outlinerPanel.getList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Hittable selectedObj = outlinerPanel.getList().getSelectedValue();

                // Show properties
                inspectorPanel.inspect(selectedObj);

                // Show Gizmo
                renderPanel.setSelectedObject(selectedObj);

                // Auto-switch tab
                if (selectedObj != null) {
                    tabbedPane.setSelectedIndex(1);
                }
            }
        });

        // --- 7. Finalize Window ---
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Creates floating zoom buttons on the bottom-right of the render panel.
     */
    private void setupZoomButtons() {
        JPanel hudPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        hudPanel.setOpaque(false); // Transparent background

        JButton zoomInBtn = createHudButton("+");
        zoomInBtn.addActionListener(e -> cameraController.zoomIn());

        JButton zoomOutBtn = createHudButton("-");
        zoomOutBtn.addActionListener(e -> cameraController.zoomOut());

        hudPanel.add(zoomInBtn);
        hudPanel.add(zoomOutBtn);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.LAST_LINE_END;
        gbc.insets = new Insets(0, 0, 10, 10);

        renderPanel.add(hudPanel, gbc);
    }

    private JButton createHudButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(0, 0, 0, 150));
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

    private JMenuBar createMenuBar(Runnable onRenderRequest) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("New Project");
        newItem.addActionListener(e -> {
            Scene.getInstance().clear();
            onRenderRequest.run();
        });

        JMenuItem openItem = new JMenuItem("Open Project...");
        openItem.addActionListener(e -> openProject(onRenderRequest));

        JMenuItem saveItem = new JMenuItem("Save Project...");
        saveItem.addActionListener(e -> saveProject());

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