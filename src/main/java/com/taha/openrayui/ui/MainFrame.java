package com.taha.openrayui.ui;

import com.taha.openrayui.geometry.Hittable;
import com.taha.openrayui.io.ImageExporter;
import com.taha.openrayui.io.SceneSerializer;
import com.taha.openrayui.model.Scene;
import com.taha.openrayui.ui.components.ObjectInspectorPanel;
import com.taha.openrayui.ui.components.OutlinerPanel;
import com.taha.openrayui.ui.components.RenderPanel;
import com.taha.openrayui.ui.components.SettingsPanel;
import com.taha.openrayui.ui.controllers.CameraInputHandler;
import com.taha.openrayui.ui.controllers.GizmoController;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * The main application window.
 * Refactored: File I/O logic has been moved to the 'io' package.
 */
public class MainFrame extends JFrame {

    private final RenderPanel renderPanel;
    private final SettingsPanel settingsPanel;
    private final OutlinerPanel outlinerPanel;
    private final ObjectInspectorPanel inspectorPanel;
    private final CameraInputHandler cameraController;

    public MainFrame(Runnable onRenderRequest) {
        // --- 1. Window Config ---
        setTitle("OpenRayUI - Java Ray Tracer Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(1000, 600));

        // --- 2. Menu ---
        setJMenuBar(createMenuBar(onRenderRequest));

        setLayout(new BorderLayout());

        // --- 3. Initialize Panels ---
        settingsPanel = new SettingsPanel(onRenderRequest, this::saveRenderedImage);
        renderPanel = new RenderPanel(800, 450);
        renderPanel.setLayout(new GridBagLayout());
        outlinerPanel = new OutlinerPanel(onRenderRequest);
        inspectorPanel = new ObjectInspectorPanel(onRenderRequest);

        RenderSettings.getInstance().imageWidth = 800;
        RenderSettings.getInstance().imageHeight = 450;

        // --- 4. Controllers ---
        GizmoController gizmoController = new GizmoController(
                renderPanel, outlinerPanel, onRenderRequest, onRenderRequest
        );

        Runnable onCameraMove = () -> {
            settingsPanel.updateCameraFields();
            onRenderRequest.run();
            renderPanel.repaint();
        };

        cameraController = new CameraInputHandler(onCameraMove, onCameraMove) {
            @Override
            public void mousePressed(MouseEvent e) {
                gizmoController.mousePressed(e);
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
                    gizmoController.mouseDragged(e);
                } else {
                    super.mouseDragged(e);
                }
            }
        };

        renderPanel.addMouseListener(cameraController);
        renderPanel.addMouseMotionListener(cameraController);
        renderPanel.addMouseWheelListener(cameraController);

        // --- 5. Layout ---
        setupZoomButtons();
        add(new JScrollPane(renderPanel), BorderLayout.CENTER);
        add(outlinerPanel, BorderLayout.WEST);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 0));
        tabbedPane.addTab("Render", settingsPanel);
        tabbedPane.addTab("Object", inspectorPanel);
        add(tabbedPane, BorderLayout.EAST);

        // --- 6. Events ---
        outlinerPanel.getList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Hittable selectedObj = outlinerPanel.getList().getSelectedValue();
                inspectorPanel.inspect(selectedObj);
                renderPanel.setSelectedObject(selectedObj);
                if (selectedObj != null) tabbedPane.setSelectedIndex(1);
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    // --- ZOOM BUTTONS ---
    private void setupZoomButtons() {
        JPanel hudPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        hudPanel.setOpaque(false);

        JButton zoomInBtn = createHudButton("+");
        zoomInBtn.addActionListener(e -> cameraController.zoomIn());

        JButton zoomOutBtn = createHudButton("-");
        zoomOutBtn.addActionListener(e -> cameraController.zoomOut());

        hudPanel.add(zoomInBtn);
        hudPanel.add(zoomOutBtn);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
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

    public RenderPanel getRenderPanel() { return renderPanel; }

    // --- MENU BAR ---
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

    // --- I/O DELEGATION (REFACTORED) ---

    private void saveProject() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Project File");
        fileChooser.setSelectedFile(new File("my_scene.ray"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Ray Tracing Project (.ray)", "ray"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                // Delegate to SceneSerializer
                SceneSerializer.save(fileChooser.getSelectedFile());
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
            try {
                // Delegate to SceneSerializer
                SceneSerializer.load(fileChooser.getSelectedFile());
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
            try {
                // Delegate to ImageExporter
                ImageExporter.saveImage(renderPanel.getImage(), fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Image exported successfully!");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}