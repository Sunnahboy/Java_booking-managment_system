package main.java.com.hallbooking.part3_scheduler.gui;

import main.java.com.hallbooking.common.models.Hall;
import main.java.com.hallbooking.part3_scheduler.HallManager;
import main.java.com.hallbooking.part3_scheduler.SchedulerManager;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class HallManagementScreen extends JFrame {
    private HallManager hallManager;
    private JTable hallTable;
    private DefaultTableModel tableModel;
    private JTextField idField, locationField;
    private JComboBox<Hall.HallType> typeComboBox;
    private JButton addButton, updateButton, deleteButton, refreshButton, backButton;
    private JFrame parentFrame; // Reference to parent dashboard (Scheduler Dashboard)

    // Custom colors
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;

    // Constructor with parent frame
    public HallManagementScreen(HallManager hallManager, JFrame parentFrame) {
        this.hallManager = hallManager;
        this.parentFrame = parentFrame; // Assign parent frame (Scheduler Dashboard)
        initComponents();
        loadHalls();

        // Hide the parent frame when this screen opens
        parentFrame.setVisible(false);
    }

    private void initComponents() {
        setTitle("Hall Management");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        idField = createStyledTextField();
        typeComboBox = new JComboBox<>(Hall.HallType.values());
        locationField = createStyledTextField();

        addComponentToInputPanel(inputPanel, new JLabel("Hall ID:"), 0, 0);
        addComponentToInputPanel(inputPanel, idField, 1, 0);
        addComponentToInputPanel(inputPanel, new JLabel("Type:"), 0, 1);
        addComponentToInputPanel(inputPanel, typeComboBox, 1, 1);
        addComponentToInputPanel(inputPanel, new JLabel("Location:"), 0, 2);
        addComponentToInputPanel(inputPanel, locationField, 1, 2);

        // Table
        String[] columnNames = {"ID", "Type", "Capacity", "Rate", "Location"};
        tableModel = new DefaultTableModel(columnNames, 0);
        hallTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(hallTable);
        styleTable(hallTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        addButton = createStyledButton("Add Hall");
        updateButton = createStyledButton("Update Hall");
        deleteButton = createStyledButton("Delete Hall");
        refreshButton = createStyledButton("Refresh");
        backButton = createStyledButton("Back to Dashboard");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        // Layout
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> addHall());
        updateButton.addActionListener(e -> updateHall());
        deleteButton.addActionListener(e -> deleteHall());
        refreshButton.addActionListener(e -> refreshHalls());
        backButton.addActionListener(e -> goBack()); // Handle back button logic
        hallTable.getSelectionModel().addListSelectionListener(e -> fillInputFields());
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(200, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        return button;
    }

    private void addComponentToInputPanel(JPanel panel, JComponent component, int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(component, gbc);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(BUTTON_HOVER_COLOR);
        table.setSelectionForeground(TEXT_COLOR);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        // Style the table header
        JTableHeader header = table.getTableHeader();
        header.setBackground(BUTTON_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // Create a custom renderer for the header
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(BUTTON_COLOR);
                label.setForeground(TEXT_COLOR);
                label.setFont(new Font("Arial", Font.BOLD, 14));
                label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });

        // Add alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 250));
                }
                return c;
            }
        });
    }

    private void loadHalls() {
        tableModel.setRowCount(0);
        for (Hall hall : hallManager.getHalls()) {
            tableModel.addRow(new Object[]{
                    hall.getId(),
                    hall.getType(),
                    hall.getCapacity(),
                    hall.getRate(),
                    hall.getLocation()
            });
        }
    }

    private void refreshHalls() {
        loadHalls();
        clearInputFields();
        JOptionPane.showMessageDialog(this, "Hall list refreshed.");
    }

    private void addHall() {
        try {
            String id = idField.getText();
            Hall.HallType type = (Hall.HallType) typeComboBox.getSelectedItem();
            String location = locationField.getText();

            Hall newHall = new Hall(id, type, 0, null, location);
            hallManager.addHall(newHall);
            loadHalls();
            clearInputFields();
            JOptionPane.showMessageDialog(this, "Hall added successfully.");
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateHall() {
        try {
            String id = idField.getText();
            Hall.HallType type = (Hall.HallType) typeComboBox.getSelectedItem();
            String newLocation = locationField.getText();

            Hall existingHall = SchedulerManager.getHallById(id);
            if (existingHall != null) {
                Hall updatedHall = new Hall(
                        id,
                        type,
                        existingHall.getCapacity(),
                        existingHall.getRate(),
                        newLocation
                );
                hallManager.updateHall(updatedHall);
                loadHalls();
                clearInputFields();
                JOptionPane.showMessageDialog(this, "Hall updated successfully.");
            } else {
                throw new HallNotFoundException(id);
            }
        } catch (HallNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteHall() {
        String hallId = idField.getText();
        try {
            hallManager.deleteHall(hallId);
            loadHalls();
            clearInputFields();
            JOptionPane.showMessageDialog(this, "Hall deleted successfully.");
        } catch (HallNotFoundException | InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillInputFields() {
        int selectedRow = hallTable.getSelectedRow();
        if (selectedRow != -1) {
            idField.setText((String) tableModel.getValueAt(selectedRow, 0));
            typeComboBox.setSelectedItem(tableModel.getValueAt(selectedRow, 1));
            locationField.setText((String) tableModel.getValueAt(selectedRow, 4));
        }
    }

    private void clearInputFields() {
        idField.setText("");
        typeComboBox.setSelectedIndex(0);
        locationField.setText("");
    }

    private void goBack() {
        this.dispose();
        parentFrame.setVisible(true); // Reopen the parent frame (Scheduler Dashboard)
    }

    @Override
    public void dispose() {
        super.dispose();
        parentFrame.setVisible(true); // Ensure the dashboard is visible when this window is closed
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            JFrame parentFrame = new JFrame(); // For testing purposes, create a dummy parent frame
            parentFrame.setVisible(false); // Hide the dummy frame
            new HallManagementScreen(new HallManager(), parentFrame).setVisible(true);
        });
    }
}
