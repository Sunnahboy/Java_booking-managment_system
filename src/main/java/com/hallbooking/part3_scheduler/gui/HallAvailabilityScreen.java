package main.java.com.hallbooking.part3_scheduler.gui;

import main.java.com.hallbooking.part3_scheduler.HallManager;
import main.java.com.hallbooking.common.models.Hall;
import main.java.com.hallbooking.common.exceptions.CustomExceptions.*;
import main.java.com.hallbooking.common.utils.DateTimeUtils;
import main.java.com.hallbooking.part3_scheduler.SchedulerManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import main.java.com.hallbooking.common.models.madeCalender;

public class HallAvailabilityScreen extends JFrame {
    private HallManager hallManager;
    private JComboBox<String> hallIdComboBox;
    private madeCalender startDatePicker, endDatePicker;
    private JComboBox<String> startTimeComboBox, endTimeComboBox;
    private JTable availabilityTable;
    private DefaultTableModel tableModel;
    private JButton setAvailabilityButton, backButton;
    private JFrame parentFrame;  // Reference to parent frame (SchedulerDashboard)

    // Custom colors
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(50, 50, 50);

    // Constructor with parent frame reference
    public HallAvailabilityScreen(HallManager hallManager, JFrame parentFrame) {
        this.hallManager = hallManager;
        this.parentFrame = parentFrame;  // Assign parent frame (SchedulerDashboard)
        initComponents();
        loadAvailabilityData();

        // Hide the parent frame when this screen opens
        parentFrame.setVisible(false);
    }

    private void initComponents() {
        setTitle("Set Hall Availability");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel inputPanel = createInputPanel();
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        setAvailabilityButton.addActionListener(e -> setHallAvailability());
        backButton.addActionListener(e -> goBack());
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        List<Hall> halls = hallManager.getHalls();
        String[] hallIds = halls.stream().map(Hall::getId).toArray(String[]::new);
        hallIdComboBox = new JComboBox<>(hallIds);

        // Initialize your date pickers
        startDatePicker = new madeCalender();
        endDatePicker = new madeCalender();

        String[] timeOptions1 = {"08:00"};
        String[] timeOptions2 = {"18:00"};

        startTimeComboBox = new JComboBox<>(timeOptions1);
        endTimeComboBox = new JComboBox<>(timeOptions2);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(createStyledLabel("Hall ID:"), gbc);
        gbc.gridx = 1; panel.add(hallIdComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(createStyledLabel("Start Date:"), gbc);
        gbc.gridx = 1; panel.add(startDatePicker, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(createStyledLabel("Start Time:"), gbc);
        gbc.gridx = 1; panel.add(startTimeComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(createStyledLabel("End Date:"), gbc);
        gbc.gridx = 1; panel.add(endDatePicker, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(createStyledLabel("End Time:"), gbc);
        gbc.gridx = 1; panel.add(endTimeComboBox, gbc);

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(LABEL_COLOR);
        return label;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        String[] columnNames = {"Hall ID", "Start Date/Time", "End Date/Time"};
        tableModel = new DefaultTableModel(columnNames, 0);
        availabilityTable = new JTable(tableModel);
        styleTable(availabilityTable);

        JScrollPane scrollPane = new JScrollPane(availabilityTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BACKGROUND_COLOR);

        setAvailabilityButton = createStyledButton("Set Availability");
        backButton = createStyledButton("Back to Dashboard");

        panel.add(setAvailabilityButton);
        panel.add(backButton);

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(200, 40));
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

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(BUTTON_HOVER_COLOR);
        table.setSelectionForeground(TEXT_COLOR);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        JTableHeader header = table.getTableHeader();
        header.setBackground(BUTTON_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

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

    private void loadAvailabilityData() {
        tableModel.setRowCount(0); // Clear existing data

        try (BufferedReader reader = new BufferedReader(new FileReader("data/hallAvailability.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                tableModel.addRow(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setHallAvailability() {
        String hallId = (String) hallIdComboBox.getSelectedItem();
        LocalDate startDate = startDatePicker.getSelectedDate();
        LocalDate endDate = endDatePicker.getSelectedDate();
        String startTime = (String) startTimeComboBox.getSelectedItem();
        String endTime = (String) endTimeComboBox.getSelectedItem();

        // Validate dates and times
        if (startDate == null || endDate == null || startTime == null || endTime == null) {
            JOptionPane.showMessageDialog(this, "Please select valid dates and times.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.parse(startDate.toString() + " " + startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime endDateTime = LocalDateTime.parse(endDate.toString() + " " + endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        try {
            hallManager.setHallAvailability(hallId, startDateTime, endDateTime);
            JOptionPane.showMessageDialog(this, "Hall availability set successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAvailabilityData(); // Reload data to reflect changes
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error setting hall availability: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Return to parent frame and close the current window
    private void goBack() {
        dispose();
        parentFrame.setVisible(true); // Reopen the parent frame (Scheduler Dashboard)
    }

    @Override
    public void dispose() {
        super.dispose();
        parentFrame.setVisible(true); // Ensure the parent frame is visible when this window is closed
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HallManager hallManager = new HallManager(); // Assume this is correctly initialized
            JFrame parentFrame = new JFrame(); // Dummy parent frame for testing
            parentFrame.setVisible(false); // Hide the dummy frame
            new HallAvailabilityScreen(hallManager, parentFrame).setVisible(true);
        });
    }
}
